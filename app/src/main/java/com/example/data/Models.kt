package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Language(val code: String, val displayName: String, val localName: String) {
    ENGLISH("en", "English", "English"),
    ZULU("zu", "isiZulu", "isiZulu"),
    XHOSA("xh", "isiXhosa", "isiXhosa"),
    AFRIKAANS("af", "Afrikaans", "Afrikaans"),
    SEPEDI("nso", "Northern Sotho", "Sepedi"),
    TSWANA("tn", "Setswana", "Setswana"),
    SOTHO("st", "Southern Sotho", "Sesotho"),
    TSONGA("ts", "Xitsonga", "Xitsonga"),
    SWAZI("ss", "siSwati", "siSwati"),
    VENDA("ve", "Tshivenda", "Tshivenda"),
    NDEBELE("nr", "isiNdebele", "isiNdebele"),
    SIGN_LANGUAGE("sasl", "Sign Language (SASL)", "SASL")
}

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val role: String, // "Mama", "Student", "Girl", "Beginner"
    val languageCode: String = "en",
    val streak: Int = 1,
    val xp: Int = 0,
    val dataSavingMode: Boolean = false,
    val isPremium: Boolean = false,
    val hasDownloadedOffline: Boolean = false,
    val avatarRes: String = "mama_avatar"
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String,
    val title: String,
    val titleLocalized: String, // Translated title based on preferred language
    val category: String, // "HTML", "CSS", "JavaScript", "Python"
    val difficulty: String, // "Beginner", "Intermediate"
    val durationMinutes: Int,
    val isUnlocked: Boolean = false,
    val isDownloaded: Boolean = false,
    val orderIndex: Int
)

@Entity(tableName = "lesson_steps")
data class LessonStep(
    @PrimaryKey val id: String,
    val lessonId: String,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val descriptionLocalized: String, // localized string
    val codeSnippet: String = "",
    val completionRequirement: String = "", // "READ", "RUN_CODE"
    val answerHint: String = ""
)

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey val id: String,
    val lessonId: String,
    val question: String,
    val questionLocalized: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int, // 0 to 3 for options A, B, C, D
    val explanation: String
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val lessonId: String,
    val currentStepIndex: Int = 0,
    val isCompleted: Boolean = false,
    val quizCompleted: Boolean = false,
    val score: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "coding_challenges")
data class CodingChallenge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val instructions: String,
    val stubCode: String,
    val expectedOutput: String,
    val isCompleted: Boolean = false,
    val difficulty: String = "Beginner"
)

@Entity(tableName = "discussion_posts")
data class DiscussionPost(
    @PrimaryKey val id: String,
    val author: String,
    val role: String, // "Mama", "Student", "Mentor"
    val content: String,
    val timestamp: Long,
    val likes: Int = 0,
    val commentCount: Int = 0,
    val languageCode: String = "en"
)

@Entity(tableName = "mentor_chats")
data class MentorChat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isAi: Boolean, // True for AI assistant, False for 1-on-1 human mentor
    val isUser: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
