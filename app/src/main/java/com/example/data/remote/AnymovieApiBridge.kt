package com.example.data.remote

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Production Unified V1 REST API Bridge for https://anymovie.ai.studio/api/v1
 *
 * Strictly uses ONLY the production domain "https://anymovie.ai.studio" for
 * fetching movie, web series, and anime discovery metadata, images, seasons, and episodes.
 */
class AnymovieApiBridge(
    private val context: android.content.Context? = null,
    private val webViewProvider: () -> WebView?,
    private val onPlayerModeChanged: ((active: Boolean, landscape: Boolean) -> Unit)? = null
) {
    companion object {
        const val PRODUCTION_DOMAIN = "https://anymovie.ai.studio"
        const val PRODUCTION_API_BASE_URL = "https://anymovie.ai.studio/api/v1"

        private val responseCache = ConcurrentHashMap<String, CachedEntry>()
        private const val MEM_CACHE_TTL_MS = 15 * 60 * 1000L // 15 minutes in-memory
        private const val DISK_CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours disk persistence
    }

    private val diskCacheDir: java.io.File? by lazy {
        context?.cacheDir?.resolve("anymovie_api_cache")?.apply {
            if (!exists()) mkdirs()
        }
    }

    private data class CachedEntry(
        val body: String,
        val timestampMs: Long
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun setPlayerMode(active: Boolean, landscape: Boolean) {
        mainHandler.post {
            onPlayerModeChanged?.invoke(active, landscape)
        }
    }

    @JavascriptInterface
    fun fetchEndpoint(endpointPath: String, callbackId: String, forceRefresh: Boolean) {
        scope.launch {
            val normalizedPath = if (endpointPath.startsWith("/")) endpointPath else "/$endpointPath"
            val fullUrl = "$PRODUCTION_API_BASE_URL$normalizedPath"
            val safeCacheKey = "api_" + normalizedPath.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(140)

            if (!forceRefresh) {
                // 1. Instant RAM Cache hit (< 0.1ms)
                val memCached = responseCache[fullUrl]
                if (memCached != null && (System.currentTimeMillis() - memCached.timestampMs) < MEM_CACHE_TTL_MS) {
                    deliverSuccess(callbackId, memCached.body)
                    return@launch
                }

                // 2. Instant Disk Cache hit on app restart (< 2ms)
                val diskFile = diskCacheDir?.resolve(safeCacheKey)
                if (diskFile != null && diskFile.exists()) {
                    val age = System.currentTimeMillis() - diskFile.lastModified()
                    val diskBody = try { diskFile.readText() } catch (e: Exception) { null }
                    if (!diskBody.isNullOrBlank()) {
                        responseCache[fullUrl] = CachedEntry(diskBody, diskFile.lastModified())
                        deliverSuccess(callbackId, diskBody)
                        if (age < MEM_CACHE_TTL_MS) {
                            return@launch
                        }
                        // If file is older than 15 mins, continue in background to revalidate without holding the user
                    }
                }
            }

            var connection: HttpURLConnection? = null
            try {
                val url = URL(fullUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "ZorvenAndroid/1.0")
                }

                val statusCode = connection.responseCode
                val stream = if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseBody = stream?.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
                } ?: ""

                if (statusCode in 200..299 && responseBody.isNotBlank()) {
                    responseCache[fullUrl] = CachedEntry(
                        body = responseBody,
                        timestampMs = System.currentTimeMillis()
                    )
                    try {
                        diskCacheDir?.resolve(safeCacheKey)?.writeText(responseBody)
                    } catch (e: Exception) {}
                    deliverSuccess(callbackId, responseBody)
                } else {
                    val errorMsg = buildString {
                        append("Production API HTTP $statusCode from $fullUrl")
                        if (responseBody.isNotBlank()) {
                            append(": ")
                            append(responseBody.take(240))
                        }
                    }
                    deliverError(callbackId, errorMsg)
                }
            } catch (e: Exception) {
                // If network failed but we have a disk backup, fall back to disk
                val fallbackFile = diskCacheDir?.resolve(safeCacheKey)
                val fallbackText = try { fallbackFile?.readText() } catch (ex: Exception) { null }
                if (!fallbackText.isNullOrBlank()) {
                    deliverSuccess(callbackId, fallbackText)
                } else {
                    val errorMsg = "Production API Error ($fullUrl): ${e.localizedMessage ?: e.javaClass.simpleName}"
                    deliverError(callbackId, errorMsg)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun deliverSuccess(callbackId: String, rawJson: String) {
        val escapedId = JSONObject.quote(callbackId)
        val escapedJson = JSONObject.quote(rawJson)
        mainHandler.post {
            val wv = webViewProvider() ?: return@post
            wv.evaluateJavascript(
                "window.__onAnymovieApiResult && window.__onAnymovieApiResult($escapedId, true, $escapedJson);",
                null
            )
            wv.postInvalidate()
        }
    }

    private fun deliverError(callbackId: String, errorMessage: String) {
        val escapedId = JSONObject.quote(callbackId)
        val escapedError = JSONObject.quote(errorMessage)
        mainHandler.post {
            val wv = webViewProvider() ?: return@post
            wv.evaluateJavascript(
                "window.__onAnymovieApiResult && window.__onAnymovieApiResult($escapedId, false, $escapedError);",
                null
            )
            wv.postInvalidate()
        }
    }
}
