package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel : ViewModel() {
    private val assistantService = GeminiAssistantService()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _answerText = MutableStateFlow("")
    val answerText: StateFlow<String> = _answerText.asStateFlow()

    fun askQuestion(question: String, voiceSynthesizer: VoiceSynthesizer) {
        viewModelScope.launch {
            _isProcessing.value = true
            _answerText.value = "Yanıt hazırlanıyor..."
            
            val result = assistantService.askQuestion(question)
            
            _isProcessing.value = false
            
            val responseText = result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Bilinmeyen bir hata oluştu."
            _answerText.value = responseText
            
            voiceSynthesizer.speak(responseText)
        }
    }
}
