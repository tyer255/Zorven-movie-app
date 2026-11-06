package com.example.data.remote

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = false)
data class StreamServerNode(
    val serverId: String,
    val serverName: String,
    val region: String,
    val protocol: String, // "HLS / MP4", "DASH 4K", "Direct Edge"
    val streamUrl: String,
    val qualityOptions: List<String>,
    val audioTracks: List<String>,
    val subtitleTracks: List<String>,
    val latencyMs: Int,
    val isCustomApiNode: Boolean = false
)

@JsonClass(generateAdapter = false)
data class CustomApiStreamResponse(
    val mediaId: String,
    val title: String?,
    val servers: List<StreamServerNode>?
)

interface StreamingApiService {
    @GET("streams/{mediaId}")
    suspend fun getMovieStreamServers(
        @Path("mediaId") mediaId: String,
        @Query("episodeId") episodeId: String? = null
    ): CustomApiStreamResponse
}

object StreamingApiClient {
    fun createService(baseUrl: String, apiKey: String): StreamingApiService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("Accept", "application/json")
            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
                requestBuilder.header("X-Stream-Api-Key", apiKey)
            }
            chain.proceed(requestBuilder.build())
        }

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(
                if (sanitizedUrl.startsWith("http://") || sanitizedUrl.startsWith("https://")) {
                    sanitizedUrl
                } else {
                    "https://api.zorven-stream.example.com/v1/"
                }
            )
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(StreamingApiService::class.java)
    }
}
