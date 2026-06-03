package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionState())
    val uiState: StateFlow<QuestionState> = _uiState.asStateFlow()

    private val summaryService = GeminiSummaryService()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val mockFlashcards = listOf(
        FlashcardModel(1, "Türkiye'nin en yüksek dağı hangisidir?", "Ağrı Dağı"),
        FlashcardModel(2, "İstiklal Marşı'nın şairi kimdir?", "Mehmet Akif Ersoy"),
        FlashcardModel(3, "Türkiye Büyük Millet Meclisi hangi tarihte açılmıştır?", "23 Nisan 1920")
    )
    private var currentFlashcardIndex = 0

    private val _flashcardState = MutableStateFlow(FlashcardState(currentCard = mockFlashcards[currentFlashcardIndex]))
    val flashcardState: StateFlow<FlashcardState> = _flashcardState.asStateFlow()

    init {
        loadMockQuestion()
    }

    fun flipFlashcard() {
        _flashcardState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markAsCorrect() {
        currentFlashcardIndex = (currentFlashcardIndex + 1) % mockFlashcards.size
        _flashcardState.update { it.copy(currentCard = mockFlashcards[currentFlashcardIndex], isFlipped = false) }
    }

    fun markAsIncorrect() {
        currentFlashcardIndex = (currentFlashcardIndex + 1) % mockFlashcards.size
        _flashcardState.update { it.copy(currentCard = mockFlashcards[currentFlashcardIndex], isFlipped = false) }
    }

    private fun loadMockQuestion() {
        val mockQuestion = Question(
            id = "q1",
            text = "Aşağıdakilerden hangisi 1924 Anayasası'nın özelliklerinden biri değildir?",
            options = listOf(
                Option("A", "Devletin şekli Cumhuriyettir."),
                Option("B", "Meclis hükümeti sistemi ile parlamenter sistem arasında karma bir hükümet sistemi benimsenmiştir."),
                Option("C", "Çoğulcu demokrasi anlayışı benimsenmiştir."),
                Option("D", "Güçler ayrılığı ilkesi katı bir şekilde uygulanmıştır."),
                Option("E", "Yargı yetkisi bağımsız mahkemelerce kullanılır.")
            ),
            correctOptionId = "D"
        )
        
        _uiState.update { it.copy(question = mockQuestion) }
    }

    fun onOptionSelected(optionId: String) {
        val currentState = _uiState.value
        // Eğer zaten cevaplandıysa, tekrar tıklamayı engelle
        if (currentState.isAnswered) return

        val question = currentState.question ?: return
        val isCorrect = optionId == question.correctOptionId
        
        val feedback = if (isCorrect) {
            "Tebrikler, doğru cevap. Seçeneğiniz: $optionId şıkkı."
        } else {
            "Maalesef yanlış. Doğru cevap ${question.correctOptionId} şıkkı olmalıydı."
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedOptionId = optionId,
                    isAnswered = true,
                    feedbackMessage = feedback
                )
            }
        }
    }
    
    fun nextQuestion() {
        // İleriki aşamada sıradaki soruyu getirecek
        // Şimdilik sadece state'i temizleyip aynı soruyu baştan verelim veya farklı bir soru yükleyelim
    }

    fun createSummary(topic: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isSummarizing.value = true
            val result = summaryService.summarizeTopic(topic)
            _isSummarizing.value = false
            onResult(result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Bilinmeyen Hata")
        }
    }
}
