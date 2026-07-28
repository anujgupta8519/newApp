package com.example.spellmeaning

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Pulls a definition from Google's search results page for "define <word>".
 *
 * IMPORTANT CAVEATS (read this before shipping the app):
 * 1. Google has no official free "definition" API. This works by requesting Google's
 *    normal search HTML and parsing the dictionary card that Google sometimes shows.
 * 2. That HTML's class/attribute names change without notice, and Google actively
 *    discourages automated scraping of search results (see their Terms of Service).
 *    Heavy or repeated use from the same IP can trigger CAPTCHA pages instead of results,
 *    in which case this will return null and the app will tell the user to try again later.
 * 3. For a production app, the robust alternative is a real dictionary API
 *    (e.g. dictionaryapi.dev, Merriam-Webster, or Wordnik) instead of scraping Google.
 */
object GoogleMeaningFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMeaning(word: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode(word, "UTF-8")
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$query"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                parseDefinition(body)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDefinition(json: String): String? {
        return try {
            val root = org.json.JSONArray(json)
            if (root.length() == 0) return null

            val firstEntry = root.getJSONObject(0)
            val meanings = firstEntry.optJSONArray("meanings") ?: return null

            for (i in 0 until meanings.length()) {
                val meaning = meanings.getJSONObject(i)
                val definitions = meaning.optJSONArray("definitions") ?: continue
                if (definitions.length() == 0) continue

                val definitionObject = definitions.getJSONObject(0)
                val definition = definitionObject.optString("definition").takeIf { it.isNotBlank() }
                val example = definitionObject.optString("example").takeIf { it.isNotBlank() }

                if (definition != null) {
                    return if (example != null) {
                        "$definition\n\nExample: $example"
                    } else {
                        definition
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
