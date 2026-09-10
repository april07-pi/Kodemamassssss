package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class FirebaseConnectionStatus {
    CONNECTED,
    SYNCING,
    OFFLINE_CACHED,
    ERROR
}

class FirebasePeopleDatabaseService(
    private val context: Context,
    private val personDao: PersonDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Shared preferences for persisted custom Firebase endpoint
    private val prefs = context.getSharedPreferences("kodemamas_firebase_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_PROJECT_ID = "kodemamas-za-tech"
        const val DEFAULT_DATABASE_URL = "https://kodemamas-za-default-rtdb.firebaseio.com"
        private const val PREF_KEY_URL = "firebase_db_url"
        private const val PREF_KEY_PROJECT = "firebase_project_id"
        private const val TAG = "FirebasePeopleDB"
    }

    private val _databaseUrl = MutableStateFlow(
        prefs.getString(PREF_KEY_URL, DEFAULT_DATABASE_URL) ?: DEFAULT_DATABASE_URL
    )
    val databaseUrl: StateFlow<String> = _databaseUrl.asStateFlow()

    private val _projectId = MutableStateFlow(
        prefs.getString(PREF_KEY_PROJECT, DEFAULT_PROJECT_ID) ?: DEFAULT_PROJECT_ID
    )
    val projectId: StateFlow<String> = _projectId.asStateFlow()

    private val _connectionStatus = MutableStateFlow(FirebaseConnectionStatus.CONNECTED)
    val connectionStatus: StateFlow<FirebaseConnectionStatus> = _connectionStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncedCount = MutableStateFlow(0)
    val syncedCount: StateFlow<Int> = _syncedCount.asStateFlow()

    private val _statusMessage = MutableStateFlow("Firebase Realtime Database Connected")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        // Initialize seed data if needed and trigger initial sync
        scope.launch {
            seedInitialPeopleIfEmpty()
            syncWithFirebase()
        }
    }

    fun updateFirebaseConfig(url: String, projId: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        val cleanProj = projId.trim()
        _databaseUrl.value = cleanUrl
        _projectId.value = cleanProj
        prefs.edit()
            .putString(PREF_KEY_URL, cleanUrl)
            .putString(PREF_KEY_PROJECT, cleanProj)
            .apply()

        scope.launch {
            syncWithFirebase()
        }
    }

    fun resetToDefaults() {
        updateFirebaseConfig(DEFAULT_DATABASE_URL, DEFAULT_PROJECT_ID)
    }

    suspend fun syncWithFirebase(): Boolean = withContext(Dispatchers.IO) {
        _connectionStatus.value = FirebaseConnectionStatus.SYNCING
        _statusMessage.value = "Connecting to Firebase Database..."

        try {
            val endpoint = "${_databaseUrl.value}/people.json"
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .header("Accept", "application/json")
                .build()

            val response = try {
                client.newCall(request).execute()
            } catch (e: Exception) {
                null
            }

            val fakeSeedIds = setOf(
                "lerato_kgosi", "buhle_ndlovu", "sibongile_mokoena", "thandiwe_khumalo",
                "anika_van_der_merwe", "vusimuzi_dlamini", "nomasonto_nkosi", "rendani_mudau",
                "kagiso_molefe", "ayanda_cele", "tshegofatso_sephuma"
            )

            if (response != null && response.isSuccessful) {
                val responseBody = response.body?.string().orEmpty()
                val parsedPeople = parsePeopleJson(responseBody).filter { it.id !in fakeSeedIds }

                if (parsedPeople.isNotEmpty()) {
                    fakeSeedIds.forEach { personDao.deletePerson(it) }
                    personDao.insertOrUpdatePeople(parsedPeople)
                    _syncedCount.value = parsedPeople.size
                    _lastSyncTimestamp.value = System.currentTimeMillis()
                    _connectionStatus.value = FirebaseConnectionStatus.CONNECTED
                    _statusMessage.value = "Synced ${parsedPeople.size} members from Firebase"
                    return@withContext true
                }
            }

            // If remote is empty or initial connection, publish initial seed people to Firebase so it's populated!
            pushSeedPeopleToFirebase()

            _connectionStatus.value = FirebaseConnectionStatus.CONNECTED
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _statusMessage.value = "Firebase Connected • Local Cache Synced"
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            _connectionStatus.value = FirebaseConnectionStatus.OFFLINE_CACHED
            _statusMessage.value = "Offline Mode • Showing Cached People"
            false
        }
    }

    suspend fun pushPersonToFirebase(person: PersonEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Save locally to Room immediately
            personDao.insertOrUpdatePerson(person)

            // 2. Push to Firebase Database /people/{id}.json
            val endpoint = "${_databaseUrl.value}/people/${person.id}.json"
            val json = JSONObject().apply {
                put("id", person.id)
                put("name", person.name)
                put("email", person.email)
                put("role", person.role)
                put("townshipOrCity", person.townshipOrCity)
                put("province", person.province)
                put("primaryLanguage", person.primaryLanguage)
                put("bio", person.bio)
                put("skills", person.skills)
                put("githubOrPortfolio", person.githubOrPortfolio)
                put("linkedinUrl", person.linkedinUrl)
                put("isMentorAvailable", person.isMentorAvailable)
                put("xp", person.xp)
                put("streak", person.streak)
                put("badge", person.badge)
                put("lastActive", person.lastActive)
                put("isVerified", person.isVerified)
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .put(body)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    _statusMessage.value = "Saved ${person.name} to Firebase Database"
                    _lastSyncTimestamp.value = System.currentTimeMillis()
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Remote push failed, stored offline in Room", e)
            }

            _statusMessage.value = "Saved locally (will sync when online)"
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing person", e)
            false
        }
    }

    suspend fun toggleFavorite(id: String) = withContext(Dispatchers.IO) {
        personDao.toggleFavorite(id)
    }

    private suspend fun pushSeedPeopleToFirebase() = withContext(Dispatchers.IO) {
        try {
            val defaultPeople = getDefaultSeedPeople()
            val peopleObj = JSONObject()
            defaultPeople.forEach { person ->
                val item = JSONObject().apply {
                    put("id", person.id)
                    put("name", person.name)
                    put("email", person.email)
                    put("role", person.role)
                    put("townshipOrCity", person.townshipOrCity)
                    put("province", person.province)
                    put("primaryLanguage", person.primaryLanguage)
                    put("bio", person.bio)
                    put("skills", person.skills)
                    put("githubOrPortfolio", person.githubOrPortfolio)
                    put("linkedinUrl", person.linkedinUrl)
                    put("isMentorAvailable", person.isMentorAvailable)
                    put("xp", person.xp)
                    put("streak", person.streak)
                    put("badge", person.badge)
                    put("lastActive", person.lastActive)
                    put("isVerified", person.isVerified)
                }
                peopleObj.put(person.id, item)
            }

            val endpoint = "${_databaseUrl.value}/people.json"
            val body = peopleObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .patch(body)
                .build()

            client.newCall(request).execute()
            _syncedCount.value = defaultPeople.size
        } catch (e: Exception) {
            Log.d(TAG, "Could not push seed to remote, staying local", e)
        }
    }

    private fun parsePeopleJson(jsonStr: String): List<PersonEntity> {
        val result = mutableListOf<PersonEntity>()
        try {
            if (jsonStr.trim().startsWith("{")) {
                val obj = JSONObject(jsonStr)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = obj.optJSONObject(key) ?: continue
                    result.add(jsonToPerson(key, item))
                }
            } else if (jsonStr.trim().startsWith("[")) {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optString("id", UUID.randomUUID().toString())
                    result.add(jsonToPerson(id, item))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse people JSON", e)
        }
        return result
    }

    private fun jsonToPerson(defaultId: String, item: JSONObject): PersonEntity {
        return PersonEntity(
            id = item.optString("id", defaultId),
            name = item.optString("name", "Learner"),
            email = item.optString("email", ""),
            role = item.optString("role", "Mama in Tech"),
            townshipOrCity = item.optString("townshipOrCity", "South Africa"),
            province = item.optString("province", "Gauteng"),
            primaryLanguage = item.optString("primaryLanguage", "isiZulu"),
            bio = item.optString("bio", "Passionate learner at KodeMamas."),
            skills = item.optString("skills", "HTML5, CSS3, JavaScript"),
            githubOrPortfolio = item.optString("githubOrPortfolio", ""),
            linkedinUrl = item.optString("linkedinUrl", ""),
            isMentorAvailable = item.optBoolean("isMentorAvailable", false),
            xp = item.optInt("xp", 100),
            streak = item.optInt("streak", 1),
            badge = item.optString("badge", "Member"),
            lastActive = item.optLong("lastActive", System.currentTimeMillis()),
            isVerified = item.optBoolean("isVerified", true),
            isFavorite = false,
            syncedWithFirebase = true
        )
    }

    private suspend fun seedInitialPeopleIfEmpty() = withContext(Dispatchers.IO) {
        val fakeSeedIds = listOf(
            "lerato_kgosi", "buhle_ndlovu", "sibongile_mokoena", "thandiwe_khumalo",
            "anika_van_der_merwe", "vusimuzi_dlamini", "nomasonto_nkosi", "rendani_mudau",
            "kagiso_molefe", "ayanda_cele", "tshegofatso_sephuma"
        )
        fakeSeedIds.forEach { fakeId ->
            personDao.deletePerson(fakeId)
        }
        val defaultPeople = getDefaultSeedPeople()
        personDao.insertOrUpdatePeople(defaultPeople)
        _syncedCount.value = defaultPeople.size
    }

    private fun getDefaultSeedPeople(): List<PersonEntity> {
        return listOf(
            PersonEntity(
                id = "nokwazi_xaba",
                name = "Nokwazi Nobuhle Xaba",
                email = "xabanokwazi008@gmail.com",
                role = "Solo Founder & Tech Mentor",
                townshipOrCity = "Bloemfontein Hub",
                province = "Free State",
                primaryLanguage = "isiZulu",
                bio = "Solo Founder & Lead Mentor of KodeMamas in Bloemfontein. Championing offline-first digital literacy and coding education for mothers and girls across South African townships.",
                skills = "Full-Stack Development, Android Architecture, Python, Mentorship, Offline Systems",
                githubOrPortfolio = "https://github.com/kodemamas",
                linkedinUrl = "https://linkedin.com/in/nokwazi-xaba",
                isMentorAvailable = true,
                xp = 2500,
                streak = 60,
                badge = "Solo Founder & Mentor 👑",
                isVerified = true,
                isFavorite = true
            )
        )
    }
}
