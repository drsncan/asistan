package com.example

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>,
    val correctOptionId: String
)

data class Option(
    val id: String,
    val text: String
)

data class QuestionState(
    val question: Question? = null,
    val selectedOptionId: String? = null,
    val isAnswered: Boolean = false,
    val feedbackMessage: String = ""
)

data class FlashcardModel(
    val id: Int,
    val frontText: String,
    val backText: String
)

data class FlashcardState(
    val currentCard: FlashcardModel? = null,
    val isFlipped: Boolean = false
)
