package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    const val MODEL_GEMINI_3_5_FLASH = "gemini-3.5-flash"
    const val MODEL_GEMINI_3_1_PRO = "gemini-3.1-pro-preview"
    const val MODEL_GEMINI_FLASH_LATEST = "gemini-flash-latest"

    private val client = OkHttpClient.Builder()
        .addInterceptor(SecurityUtils.SecurityHeadersInterceptor())
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var customApiKey: String? = null

    @Volatile
    private var activeModel: String = MODEL_GEMINI_3_5_FLASH

    @Volatile
    private var googleSearchGroundingEnabled: Boolean = true

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()?.ifEmpty { null }
    }

    fun getCustomApiKey(): String = customApiKey ?: ""

    fun setActiveModel(model: String) {
        if (model.isNotBlank()) {
            activeModel = model
        }
    }

    fun getActiveModel(): String = activeModel

    fun setGoogleSearchGrounding(enabled: Boolean) {
        googleSearchGroundingEnabled = enabled
    }

    fun isGoogleSearchGroundingEnabled(): Boolean = googleSearchGroundingEnabled

    @Volatile
    private var activeLanguageCode: String = "auto"

    fun setActiveLanguageCode(langCode: String) {
        activeLanguageCode = if (langCode.isBlank()) "auto" else langCode.trim().lowercase()
    }

    fun getActiveLanguageCode(): String = activeLanguageCode

    fun getLanguagePromptInstruction(langCode: String): String {
        val target = if (langCode.isBlank() || langCode == "auto") activeLanguageCode else langCode.trim().lowercase()
        return when (target) {
            "zu" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in isiZulu (zu). You MUST formulate your entire response in authentic, natural, respectful isiZulu. Remove the English barrier completely! Explain coding/technical concepts with culturally relatable South African analogies (e.g. spaza shops, stokvel, taxi routes, Ubuntu)."
            "xh" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in isiXhosa (xh). You MUST formulate your entire response in pure, authentic, fluent isiXhosa. Remove the English barrier completely! Explain coding/technical concepts in isiXhosa."
            "af" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Afrikaans (af). You MUST formulate your entire response in natural, fluent, idiomatically sound Afrikaans. Remove the English barrier completely!"
            "nso" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Sepedi / Northern Sotho (nso). You MUST formulate your entire response in natural, fluent Sepedi. Remove the English barrier completely!"
            "tn" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Setswana (tn). You MUST formulate your entire response in pure, natural Setswana. Remove the English barrier completely!"
            "st" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Sesotho (st). You MUST formulate your entire response in pure, natural Sesotho. Remove the English barrier completely!"
            "ts" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Xitsonga (ts). You MUST formulate your entire response in authentic, fluent Xitsonga. Remove the English barrier completely!"
            "ss" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in siSwati (ss). You MUST formulate your entire response in pure, authentic siSwati. Remove the English barrier completely!"
            "ve" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in Tshivenda (ve). You MUST formulate your entire response in pure, natural Tshivenda. Remove the English barrier completely!"
            "nr" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in isiNdebele (nr). You MUST formulate your entire response in authentic, natural isiNdebele. Remove the English barrier completely!"
            "sasl" -> "MANDATORY LANGUAGE DIRECTIVE: The user communicates in South African Sign Language (SASL). Provide visual descriptions, sign gloss notations in [BRACKETS], spatial handshape references, and visual diagrams to ensure full accessibility for deaf learners."
            "en" -> "The user communicates in English. Maintain an encouraging, township-friendly, and accessible tone."
            else -> "MANDATORY MULTILINGUAL DIRECTIVE: You have native-level fluency in all 12 official South African languages (isiZulu, isiXhosa, Afrikaans, Sepedi, Setswana, Sesotho, Xitsonga, siSwati, Tshivenda, isiNdebele, SASL, English). Detect the language used by the user, and ALWAYS respond in that EXACT same language with rich, authentic vocabulary, completely eliminating the English language barrier."
        }
    }

    fun getEffectiveApiKey(): String {
        customApiKey?.let { if (it.isNotBlank()) return it }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "GEMINI_API_KEY" && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        val envKey = System.getenv("GEMINI_API_KEY")
        if (!envKey.isNullOrBlank()) {
            return envKey
        }
        return ""
    }

    fun isLiveApiConfigured(): Boolean = getEffectiveApiKey().isNotBlank()

    data class GenerationOutput(
        val text: String,
        val modelUsed: String,
        val isOnline: Boolean,
        val isGoogleSearchGrounded: Boolean,
        val searchQueries: List<String> = emptyList(),
        val sources: List<WebSource> = emptyList()
    )

    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String = "",
        enableGoogleSearch: Boolean = true,
        isOnline: Boolean = true,
        languageCode: String = "auto"
    ): String = withContext(Dispatchers.IO) {
        val sanitizedPrompt = SecurityUtils.sanitizeInput(prompt, maxLength = 2500)
        if (sanitizedPrompt.isBlank()) {
            return@withContext "Please enter a valid prompt or question."
        }

        // Rate limiting: Maximum 15 AI requests per minute
        if (!SecurityUtils.RateLimiter.isAllowed("gemini_api", maxRequests = 15, windowMillis = 60_000L)) {
            val waitSec = SecurityUtils.RateLimiter.getCooldownSeconds("gemini_api", 60_000L)
            return@withContext "⚠️ Rate limit protection: Please wait ${waitSec}s before sending another AI question to preserve network quotas."
        }

        val apiKey = getEffectiveApiKey()
        val shouldSearchGoogle = enableGoogleSearch && googleSearchGroundingEnabled && isOnline
        val effectiveLang = if (languageCode.isNotBlank() && languageCode != "auto") languageCode else activeLanguageCode
        val langInstruction = getLanguagePromptInstruction(effectiveLang)
        val combinedSystemInstruction = if (systemInstruction.isNotBlank()) {
            "$systemInstruction\n\n$langInstruction"
        } else {
            langInstruction
        }

        // 1. If online and Google Search Grounding is requested, fetch live web database results in parallel
        var liveSearchResult: GoogleSearchResult? = null
        if (shouldSearchGoogle) {
            try {
                liveSearchResult = GoogleSearchDatabaseService.searchDatabase(sanitizedPrompt)
            } catch (_: Exception) {}
        }

        // 2. If API Key is present, invoke Gemini with Google Search Grounding Tool
        if (apiKey.isNotBlank()) {
            val candidateModels = listOf(activeModel, MODEL_GEMINI_3_5_FLASH, MODEL_GEMINI_3_1_PRO, MODEL_GEMINI_FLASH_LATEST).distinct()

            for (model in candidateModels) {
                // First attempt: with native Google Search Grounding tool
                if (shouldSearchGoogle) {
                    val groundedOutput = tryCallGeminiWithTool(model, apiKey, sanitizedPrompt, combinedSystemInstruction, liveSearchResult)
                    if (groundedOutput != null) {
                        return@withContext formatOutput(groundedOutput)
                    }
                }

                // Fallback attempt: standard Gemini call with injected search database context
                val standardOutput = tryCallGeminiStandard(model, apiKey, sanitizedPrompt, combinedSystemInstruction, liveSearchResult)
                if (standardOutput != null) {
                    return@withContext formatOutput(standardOutput)
                }
            }
        }

        // 3. If API Key is missing or live Gemini call failed, synthesize answer using live Google Search Database + intelligent local engine
        if (isOnline && shouldSearchGoogle && liveSearchResult != null) {
            return@withContext ComprehensiveLocalAiEngine.generateUnrestrictedAnswer(sanitizedPrompt, liveSearchResult, effectiveLang)
        }

        // 4. Offline or local AI answer
        return@withContext ComprehensiveLocalAiEngine.generateUnrestrictedAnswer(sanitizedPrompt, null, effectiveLang)
    }

    private fun tryCallGeminiWithTool(
        model: String,
        apiKey: String,
        prompt: String,
        systemInstruction: String,
        supplementalSearch: GoogleSearchResult?
    ): GenerationOutput? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()

        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestJson.put("contents", contentsArray)

        // Add Google Search Grounding Tool for Gemini API
        val toolsArray = JSONArray()
        val googleSearchTool = JSONObject()
        googleSearchTool.put("googleSearch", JSONObject())
        toolsArray.put(googleSearchTool)
        requestJson.put("tools", toolsArray)

        var finalSysInstruction = systemInstruction
        if (supplementalSearch != null && supplementalSearch.sources.isNotEmpty()) {
            finalSysInstruction += "\n\n" + supplementalSearch.toContextString()
        }

        if (finalSysInstruction.isNotEmpty()) {
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", finalSysInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", sysInstructionObj)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bodyString = response.body?.string() ?: return null
                parseGeminiResponse(bodyString, model, isGrounded = true, supplementalSearch = supplementalSearch)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun tryCallGeminiStandard(
        model: String,
        apiKey: String,
        prompt: String,
        systemInstruction: String,
        supplementalSearch: GoogleSearchResult?
    ): GenerationOutput? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()

        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestJson.put("contents", contentsArray)

        var finalSysInstruction = systemInstruction
        if (supplementalSearch != null && supplementalSearch.sources.isNotEmpty()) {
            finalSysInstruction += "\n\n" + supplementalSearch.toContextString()
        }

        if (finalSysInstruction.isNotEmpty()) {
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", finalSysInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", sysInstructionObj)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bodyString = response.body?.string() ?: return null
                parseGeminiResponse(bodyString, model, isGrounded = supplementalSearch != null, supplementalSearch = supplementalSearch)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseGeminiResponse(
        jsonString: String,
        model: String,
        isGrounded: Boolean,
        supplementalSearch: GoogleSearchResult?
    ): GenerationOutput? {
        val jsonResponse = JSONObject(jsonString)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null

        val answerText = parts.getJSONObject(0).optString("text")
        if (answerText.isBlank()) return null

        val searchQueries = mutableListOf<String>()
        val sources = mutableListOf<WebSource>()

        // Check for grounding metadata returned by Gemini Google Search tool
        val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
        if (groundingMetadata != null) {
            val webQueries = groundingMetadata.optJSONArray("webSearchQueries")
            if (webQueries != null) {
                for (i in 0 until webQueries.length()) {
                    val q = webQueries.optString(i)
                    if (q.isNotBlank()) searchQueries.add(q)
                }
            }

            val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
            if (groundingChunks != null) {
                for (i in 0 until groundingChunks.length()) {
                    val chunk = groundingChunks.optJSONObject(i)
                    val web = chunk?.optJSONObject("web")
                    if (web != null) {
                        val uri = web.optString("uri")
                        val title = web.optString("title")
                        if (uri.isNotBlank()) {
                            sources.add(
                                WebSource(
                                    title = if (title.isNotBlank()) title else uri,
                                    snippet = "Google Search verified grounding source",
                                    url = uri
                                )
                            )
                        }
                    }
                }
            }
        }

        // Add supplemental search sources if grounding metadata was empty
        if (sources.isEmpty() && supplementalSearch != null) {
            sources.addAll(supplementalSearch.sources)
            searchQueries.addAll(supplementalSearch.relatedQueries)
        }

        return GenerationOutput(
            text = answerText,
            modelUsed = model,
            isOnline = true,
            isGoogleSearchGrounded = isGrounded && (sources.isNotEmpty() || searchQueries.isNotEmpty()),
            searchQueries = searchQueries.distinct(),
            sources = sources.distinctBy { it.url }
        )
    }

    private fun formatOutput(output: GenerationOutput): String {
        if (!output.isGoogleSearchGrounded || output.sources.isEmpty()) {
            return output.text
        }

        val sb = StringBuilder(output.text.trim())
        sb.append("\n\n---\n")
        sb.append("🌐 **Grounded with Google Search Database & Gemini AI**\n")
        if (output.searchQueries.isNotEmpty()) {
            sb.append("🔍 *Search queries:* ")
            sb.append(output.searchQueries.take(4).joinToString(", ") { "\"$it\"" })
            sb.append("\n\n")
        }
        sb.append("📚 *Verified Web Sources:*\n")
        output.sources.take(4).forEach { src ->
            sb.append("• **${src.title}**: [${src.url}](${src.url})\n")
        }
        return sb.toString().trim()
    }
}
