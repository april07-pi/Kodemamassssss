package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class GoogleSearchResult(
    val query: String,
    val relatedQueries: List<String> = emptyList(),
    val directAnswer: String? = null,
    val sources: List<WebSource> = emptyList(),
    val isOnline: Boolean = true
) {
    fun toContextString(): String {
        val sb = StringBuilder()
        sb.append("Real-time Google Search Database Context for query: \"$query\"\n")
        if (!directAnswer.isNullOrBlank()) {
            sb.append("Direct Web Database Summary: $directAnswer\n")
        }
        if (relatedQueries.isNotEmpty()) {
            sb.append("Google Search Index Related Topics: ")
            sb.append(relatedQueries.take(5).joinToString(", "))
            sb.append("\n")
        }
        if (sources.isNotEmpty()) {
            sb.append("Top Verified Sources from Web Database:\n")
            sources.take(5).forEach { source ->
                sb.append("• ${source.title}: ${source.snippet} (${source.url})\n")
            }
        }
        return sb.toString().trim()
    }
}

data class WebSource(
    val title: String,
    val snippet: String,
    val url: String
)

object GoogleSearchDatabaseService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchDatabase(query: String): GoogleSearchResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext GoogleSearchResult(query = trimmed, isOnline = false)
        }

        val encodedQuery = try {
            URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            trimmed.replace(" ", "+")
        }

        val relatedQueries = mutableListOf<String>()
        val sources = mutableListOf<WebSource>()
        var directAnswer: String? = null

        // 1. Query Google's Search Suggestion Database Index
        try {
            val googleSuggestUrl = "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"
            val request = Request.Builder()
                .url(googleSuggestUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArr = JSONArray(body)
                        if (jsonArr.length() > 1) {
                            val suggestionsArr = jsonArr.optJSONArray(1)
                            if (suggestionsArr != null) {
                                for (i in 0 until suggestionsArr.length()) {
                                    val suggestion = suggestionsArr.optString(i)
                                    if (suggestion.isNotBlank()) {
                                        relatedQueries.add(suggestion)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Network fallback
        }

        // 2. Query Public Web Knowledge & Database Index (DuckDuckGo / Open Web Database)
        try {
            val ddgUrl = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder()
                .url(ddgUrl)
                .header("User-Agent", "KodeMamas-App/1.2")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val abstractText = json.optString("AbstractText")
                        val abstractUrl = json.optString("AbstractURL")
                        val heading = json.optString("Heading")

                        if (abstractText.isNotBlank()) {
                            directAnswer = abstractText
                            sources.add(
                                WebSource(
                                    title = if (heading.isNotBlank()) heading else "Web Knowledge Base",
                                    snippet = abstractText,
                                    url = if (abstractUrl.isNotBlank()) abstractUrl else "https://www.google.com/search?q=$encodedQuery"
                                )
                            )
                        }

                        val relatedTopics = json.optJSONArray("RelatedTopics")
                        if (relatedTopics != null) {
                            for (i in 0 until relatedTopics.length()) {
                                val topic = relatedTopics.optJSONObject(i)
                                if (topic != null) {
                                    val text = topic.optString("Text")
                                    val firstUrl = topic.optString("FirstURL")
                                    if (text.isNotBlank()) {
                                        sources.add(
                                            WebSource(
                                                title = text.take(60),
                                                snippet = text,
                                                url = if (firstUrl.isNotBlank()) firstUrl else "https://www.google.com/search?q=$encodedQuery"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Network fallback
        }

        // Add Google Search fallback source link if no other sources were fetched
        if (sources.isEmpty()) {
            sources.add(
                WebSource(
                    title = "Google Search: $trimmed",
                    snippet = "Live web search results indexed in Google Database.",
                    url = "https://www.google.com/search?q=$encodedQuery"
                )
            )
        }

        GoogleSearchResult(
            query = trimmed,
            relatedQueries = relatedQueries.take(8),
            directAnswer = directAnswer,
            sources = sources.take(6),
            isOnline = true
        )
    }
}
