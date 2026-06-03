package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAssistantService {
    suspend fun askQuestion(question: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                 return@withContext Result.failure(Exception("Lütfen ayarlardan Gemini API anahtarınızı (Secrets) giriniz."))
            }

            val systemPrompt = "Sen 2026 KPSS'ye hazırlanan görme engelli bir öğrenciye Tarih, Coğrafya ve Vatandaşlık derslerinde rehberlik eden bir asistansın. Çıktıların 3-4 cümleyi geçmeyen, kısa, net ve ardışık olarak sesli okunmaya uygun pedagojik özetler olmalı. Markdown, yıldız veya emoji kullanma."

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = question)))),
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
