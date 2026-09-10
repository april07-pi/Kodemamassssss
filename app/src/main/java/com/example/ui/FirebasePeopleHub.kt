package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FirebaseConnectionStatus
import com.example.data.PersonEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FirebasePeopleHub(
    viewModel: MainViewModel,
    langCode: String,
    onConnectWithPerson: (PersonEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val allPeople by viewModel.allPeople.collectAsState()
    val firebaseStatus by viewModel.firebaseStatus.collectAsState()
    val lastSync by viewModel.firebaseLastSync.collectAsState()
    val syncedCount by viewModel.firebaseSyncedCount.collectAsState()
    val statusMessage by viewModel.firebaseStatusMessage.collectAsState()
    val dbUrl by viewModel.firebaseDbUrl.collectAsState()
    val projectId by viewModel.firebaseProjectId.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedProvinceFilter by remember { mutableStateOf("All Provinces") }
    var showOnlyMentors by remember { mutableStateOf(false) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Dialog states
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showFirebaseConfigDialog by remember { mutableStateOf(false) }
    var selectedPersonDetail by remember { mutableStateOf<PersonEntity?>(null) }

    val filteredPeople = remember(allPeople, searchQuery, selectedRoleFilter, selectedProvinceFilter, showOnlyMentors, showOnlyFavorites) {
        allPeople.filter { person ->
            val matchesSearch = searchQuery.isBlank() ||
                    person.name.contains(searchQuery, ignoreCase = true) ||
                    person.townshipOrCity.contains(searchQuery, ignoreCase = true) ||
                    person.skills.contains(searchQuery, ignoreCase = true) ||
                    person.primaryLanguage.contains(searchQuery, ignoreCase = true) ||
                    person.bio.contains(searchQuery, ignoreCase = true)

            val matchesRole = when (selectedRoleFilter) {
                "All" -> true
                "Mamas" -> person.role.contains("Mama", ignoreCase = true)
                "Mentors" -> person.isMentorAvailable || person.role.contains("Mentor", ignoreCase = true)
                "Students" -> person.role.contains("Student", ignoreCase = true) || person.role.contains("Learner", ignoreCase = true)
                else -> true
            }

            val matchesProvince = selectedProvinceFilter == "All Provinces" || person.province.equals(selectedProvinceFilter, ignoreCase = true)
            val matchesMentor = !showOnlyMentors || person.isMentorAvailable
            val matchesFavorite = !showOnlyFavorites || person.isFavorite

            matchesSearch && matchesRole && matchesProvince && matchesMentor && matchesFavorite
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("firebase_people_hub")
    ) {
        // ------------------ FIREBASE CONNECTION BANNER CARD ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("firebase_status_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeDarkBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (firebaseStatus) {
                                        FirebaseConnectionStatus.CONNECTED -> Color(0xFF00E676)
                                        FirebaseConnectionStatus.SYNCING -> Color(0xFFFFD700)
                                        FirebaseConnectionStatus.OFFLINE_CACHED -> Color(0xFFFF9100)
                                        FirebaseConnectionStatus.ERROR -> Color(0xFFFF5252)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (firebaseStatus) {
                                FirebaseConnectionStatus.CONNECTED -> "FIREBASE DATABASE CONNECTED"
                                FirebaseConnectionStatus.SYNCING -> "SYNCING WITH FIREBASE..."
                                FirebaseConnectionStatus.OFFLINE_CACHED -> "FIREBASE CACHED OFFLINE"
                                FirebaseConnectionStatus.ERROR -> "FIREBASE OFFLINE"
                            },
                            color = ThemeGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Firebase Settings button
                    IconButton(
                        onClick = { showFirebaseConfigDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Firebase Settings",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "South African People in Tech 🇿🇦",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$statusMessage • ${allPeople.size} active members",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Quick sync button
                        IconButton(
                            onClick = {
                                viewModel.syncFirebasePeople()
                                Toast.makeText(context, "Syncing with Firebase...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Firebase",
                                tint = ThemeGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Add Person button
                        IconButton(
                            onClick = { showAddPersonDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Person",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ------------------ SEARCH BAR ------------------
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, township, language or skill...", fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = ThemeIndigo, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp))
                .testTag("people_search_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        // ------------------ FILTER CHIPS ROW ------------------
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedRoleFilter == "All" && !showOnlyFavorites && !showOnlyMentors,
                    onClick = {
                        selectedRoleFilter = "All"
                        showOnlyFavorites = false
                        showOnlyMentors = false
                    },
                    label = { Text("All (${allPeople.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }

            item {
                FilterChip(
                    selected = selectedRoleFilter == "Mamas",
                    onClick = {
                        selectedRoleFilter = if (selectedRoleFilter == "Mamas") "All" else "Mamas"
                    },
                    label = { Text("👩🏾‍🍼 Mamas in Tech", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }

            item {
                FilterChip(
                    selected = showOnlyMentors,
                    onClick = { showOnlyMentors = !showOnlyMentors },
                    label = { Text("🌟 Mentors Available", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }

            item {
                FilterChip(
                    selected = selectedRoleFilter == "Students",
                    onClick = {
                        selectedRoleFilter = if (selectedRoleFilter == "Students") "All" else "Students"
                    },
                    label = { Text("🎓 Township Learners", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }

            item {
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    label = { Text("⭐ Bookmarked", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ------------------ PEOPLE LIST / EMPTY STATE ------------------
        if (filteredPeople.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PeopleOutline,
                        contentDescription = null,
                        tint = ThemeIndigo.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No community members found",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Try adjusting your search query or role filter",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedRoleFilter = "All"
                            showOnlyMentors = false
                            showOnlyFavorites = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo)
                    ) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPeople, key = { it.id }) { person ->
                    PersonCard(
                        person = person,
                        onToggleFavorite = { viewModel.togglePersonFavorite(person.id) },
                        onCardClick = { selectedPersonDetail = person },
                        onConnectClick = {
                            onConnectWithPerson(person)
                            Toast.makeText(context, "Opening direct chat with ${person.name}...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ------------------ DETAIL DIALOG ------------------
    selectedPersonDetail?.let { person ->
        PersonDetailDialog(
            person = person,
            onDismiss = { selectedPersonDetail = null },
            onToggleFavorite = { viewModel.togglePersonFavorite(person.id) },
            onConnect = {
                selectedPersonDetail = null
                onConnectWithPerson(person)
            }
        )
    }

    // ------------------ ADD / PUBLISH TO FIREBASE DIALOG ------------------
    if (showAddPersonDialog) {
        AddPersonToFirebaseDialog(
            userProfile = userProfile,
            onDismiss = { showAddPersonDialog = false },
            onSubmit = { newPerson ->
                viewModel.addPersonToFirebase(newPerson)
                showAddPersonDialog = false
                Toast.makeText(context, "Published ${newPerson.name} to Firebase Database!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ------------------ FIREBASE CONFIG SETTINGS DIALOG ------------------
    if (showFirebaseConfigDialog) {
        FirebaseConfigDialog(
            currentUrl = dbUrl,
            currentProjectId = projectId,
            lastSync = lastSync,
            syncedCount = syncedCount,
            status = firebaseStatus,
            onDismiss = { showFirebaseConfigDialog = false },
            onSave = { newUrl, newProject ->
                viewModel.updateFirebaseConfig(newUrl, newProject)
                showFirebaseConfigDialog = false
                Toast.makeText(context, "Firebase configuration updated", Toast.LENGTH_SHORT).show()
            },
            onReset = {
                viewModel.resetFirebaseConfig()
                showFirebaseConfigDialog = false
                Toast.makeText(context, "Reset to KodeMamas default Firebase", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ---------------------- COMPONENT: PERSON CARD ----------------------
@Composable
fun PersonCard(
    person: PersonEntity,
    onToggleFavorite: () -> Unit,
    onCardClick: () -> Unit,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ThemeCardBorder, RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
            .testTag("person_card_${person.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with role initial
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (person.isMentorAvailable) ThemeIndigo else Color(0xFF26053D)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        color = ThemeGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = person.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        if (person.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "${person.role} • ${person.townshipOrCity}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Speaks ${person.primaryLanguage} • ${person.province}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                // Bookmark Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (person.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Bookmark",
                        tint = if (person.isFavorite) ThemeGold else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio preview
            Text(
                text = person.bio,
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Skills chips
            val skillList = person.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                skillList.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ThemeIndigo.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 9.sp,
                            color = ThemeIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action row: XP + Connect button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚡ ${person.xp} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔥 ${person.streak}d streak",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(1.dp, ThemeIndigo)
                    ) {
                        Text("View Info", fontSize = 10.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onConnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (person.isMentorAvailable) ThemeIndigo else Color(0xFF26053D)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = if (person.isMentorAvailable) Icons.Default.ChatBubble else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (person.isMentorAvailable) "Mentorship" else "Connect",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: PERSON DETAIL DIALOG ----------------------
@Composable
fun PersonDetailDialog(
    person: PersonEntity,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConnect: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2F1))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FIREBASE RECORD: ${person.id}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00796B)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ThemeIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = person.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            color = ThemeGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = person.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            text = person.role,
                            fontSize = 12.sp,
                            color = ThemeIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${person.townshipOrCity}, ${person.province}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bio section
                Text(
                    text = "ABOUT & BACKGROUND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = person.bio,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Skills section
                Text(
                    text = "TECHNICAL SKILLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                val skills = person.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    skills.forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ThemeIndigo.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = s, color = ThemeIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language and Mentor status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("HOME LANGUAGE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(person.primaryLanguage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Column {
                        Text("MENTOR AVAILABILITY", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            if (person.isMentorAvailable) "Accepting Mentees ✅" else "Learner Track 📚",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (person.isMentorAvailable) Color(0xFF00796B) else Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(person.email.ifBlank { person.name }))
                            Toast.makeText(context, "Copied contact info", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Copy Contact", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Message", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: ADD PERSON TO FIREBASE ----------------------
@Composable
fun AddPersonToFirebaseDialog(
    userProfile: com.example.data.UserProfile?,
    onDismiss: () -> Unit,
    onSubmit: (PersonEntity) -> Unit
) {
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var email by remember { mutableStateOf(userProfile?.email ?: "") }
    var role by remember { mutableStateOf("Mama in Tech") }
    var township by remember { mutableStateOf("Soweto (Diepkloof)") }
    var province by remember { mutableStateOf("Gauteng") }
    var language by remember { mutableStateOf("isiZulu") }
    var skills by remember { mutableStateOf("HTML5, CSS3, Python, Android") }
    var bio by remember { mutableStateOf("Passionate about coding education and offline software solutions.") }
    var isMentor by remember { mutableStateOf(false) }

    val roleOptions = listOf("Mama in Tech", "Tech Mentor", "Township Student", "Community Lead", "Junior Developer")
    val provinceOptions = listOf("Gauteng", "Western Cape", "KwaZulu-Natal", "Free State", "Eastern Cape", "Limpopo", "Mpumalanga", "North West", "Northern Cape")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Register to Firebase People Database ☁️",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Your profile will be published to the live Firebase cloud database for township learners & mentors to connect.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = township,
                        onValueChange = { township = it },
                        label = { Text("Township or City Hub") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Brief Bio") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isMentor = !isMentor }
                    ) {
                        Checkbox(
                            checked = isMentor,
                            onCheckedChange = { isMentor = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Available as a Tech Mentor for other mothers & girls",
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val safeId = "user_${UUID.randomUUID().toString().take(8)}"
                                    onSubmit(
                                        PersonEntity(
                                            id = safeId,
                                            name = name.trim(),
                                            email = email.trim(),
                                            role = role,
                                            townshipOrCity = township.trim(),
                                            province = province,
                                            primaryLanguage = language,
                                            bio = bio.trim(),
                                            skills = skills.trim(),
                                            isMentorAvailable = isMentor,
                                            xp = userProfile?.xp ?: 150,
                                            streak = userProfile?.streak ?: 3,
                                            badge = if (isMentor) "Mentor 🌟" else "Mama in Tech 🇿🇦",
                                            isVerified = true,
                                            isFavorite = false,
                                            syncedWithFirebase = true
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Publish to Firebase", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: FIREBASE CONFIG DIALOG ----------------------
@Composable
fun FirebaseConfigDialog(
    currentUrl: String,
    currentProjectId: String,
    lastSync: Long,
    syncedCount: Int,
    status: FirebaseConnectionStatus,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onReset: () -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var projectInput by remember { mutableStateOf(currentProjectId) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Firebase Database Config ⚙️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "Configure your Firebase Realtime Database or Cloud Firestore endpoint. Connect your own Firebase project or use the pre-configured KodeMamas cluster.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Current Live Stats Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ThemeIndigo.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "LIVE CLOUD STATUS: ${status.name}",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = ThemeIndigo
                        )
                        Text(
                            text = "Synced Members: $syncedCount",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Last Sync: ${dateFormat.format(Date(lastSync))}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Collection Path: /people.json",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeIndigo
                        )
                    }
                }

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Firebase Database URL") },
                    placeholder = { Text("https://your-app-default-rtdb.firebaseio.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = projectInput,
                    onValueChange = { projectInput = it },
                    label = { Text("Firebase Project ID") },
                    placeholder = { Text("my-firebase-project") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onReset) {
                        Text("Reset Defaults", color = Color(0xFFC2185B), fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    onSave(urlInput.trim(), projectInput.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save & Connect", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
