package com.example

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiSummaryService {
    suspend fun summarizeTopic(topic: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                 return@withContext Result.failure(Exception("Lütfen ayarlardan Gemini API anahtarınızı (Secrets) giriniz."))
            }

            val systemPrompt = "Sen görme engelli bir öğrenciye sınav hazırlığı (Tarih, Coğrafya, Vatandaşlık) için çok kısa, net ve ardışık olarak sesli okunmaya uygun pedagojik özetler çıkaran bir asistansın. Çıktıda asla ekran okuyucunun kafasını karıştıracak gereksiz noktalama işaretleri (yıldız, tire, markdown vb.) veya emojiler kullanma."

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "$topic konusunu özetle")))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Gemini'den boş yanıt döndü."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
