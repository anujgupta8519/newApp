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

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"

    suspend fun fetchMeaning(word: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode("define $word", "UTF-8")
            val url = "https://www.google.com/search?q=$query&hl=en"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val html = response.body?.string() ?: return@withContext null
                parseDefinition(html)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tries a few known selectors Google has used for its dictionary snippet.
     * Falls back through each one since Google changes markup periodically.
     */
    private fun parseDefinition(html: String): String? {
        val doc = Jsoup.parse(html)

        // Historically-seen selector for the definition text in Google's word-meaning card
        doc.select("div[data-dobid=dfn]").firstOrNull()?.text()?.let {
            if (it.isNotBlank()) return cleanup(it)
        }

        // Fallback: some layouts put the definition in a span near "noun"/"verb" labels
        doc.select("span").firstOrNull { el ->
            el.previousElementSibling()?.text()?.matches(Regex("(?i)noun|verb|adjective|adverb")) == true
        }?.text()?.let {
            if (it.isNotBlank()) return cleanup(it)
        }

        // Last resort: grab the first reasonably long text block on the results page,
        // which is often the featured snippet.
        doc.select("div.BNeawe.s3v9rd.AP7Wnd").firstOrNull()?.text()?.let {
            if (it.length > 15) return cleanup(it)
        }

        return null
    }

    private fun cleanup(text: String) = text.trim().replace(Regex("\\s+"), " ")
}
