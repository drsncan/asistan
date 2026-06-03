package com.example.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KpssDao {
    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getFlashcardsByTopic(topicId: Int): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    fun getQuestionsByTopic(topicId: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    fun getTopicsBySubject(subjectId: Int): Flow<List<TopicEntity>>
}
