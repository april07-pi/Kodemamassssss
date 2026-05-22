package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSynchronous(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET xp = xp + :xpGained, streak = :newStreak WHERE id = 1")
    suspend fun updateXpAndStreak(xpGained: Int, newStreak: Int)

    @Query("UPDATE user_profiles SET languageCode = :newLangCode WHERE id = 1")
    suspend fun updateLanguage(newLangCode: String)

    @Query("UPDATE user_profiles SET dataSavingMode = :enabled WHERE id = 1")
    suspend fun updateDataSaving(enabled: Boolean)

    @Query("UPDATE user_profiles SET hasDownloadedOffline = :enabled WHERE id = 1")
    suspend fun updateDownloadedOfflineStatus(enabled: Boolean)

    @Query("UPDATE user_profiles SET isPremium = :isPremium WHERE id = 1")
    suspend fun updatePremiumStatus(isPremium: Boolean)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): Lesson?

    @Query("UPDATE lessons SET isDownloaded = :downloaded WHERE id = :lessonId")
    suspend fun updateDownloadedStatus(lessonId: String, downloaded: Boolean)

    @Query("UPDATE lessons SET isUnlocked = :unlocked WHERE id = :lessonId")
    suspend fun updateUnlockedStatus(lessonId: String, unlocked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)
}

@Dao
interface LessonStepDao {
    @Query("SELECT * FROM lesson_steps WHERE lessonId = :lessonId ORDER BY stepNumber ASC")
    fun getStepsForLesson(lessonId: String): Flow<List<LessonStep>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<LessonStep>)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId")
    fun getQuizForLesson(lessonId: String): Flow<List<QuizQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestion>)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId LIMIT 1")
    fun getProgressForLesson(lessonId: String): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getProgressForLessonSynchronous(lessonId: String): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM coding_challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<CodingChallenge>>

    @Query("UPDATE coding_challenges SET isCompleted = :completed WHERE id = :id")
    suspend fun updateChallengeStatus(id: String, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<CodingChallenge>)
}

@Dao
interface DiscussionDao {
    @Query("SELECT * FROM discussion_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<DiscussionPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: DiscussionPost)

    @Query("UPDATE discussion_posts SET likes = likes + 1 WHERE id = :postId")
    suspend fun incrementLikes(postId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM mentor_chats WHERE isAi = 1 ORDER BY timestamp ASC")
    fun getAiChats(): Flow<List<MentorChat>>

    @Query("SELECT * FROM mentor_chats WHERE isAi = 0 ORDER BY timestamp ASC")
    fun getMentorChats(): Flow<List<MentorChat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MentorChat)

    @Query("DELETE FROM mentor_chats WHERE isAi = :isAi")
    suspend fun clearChats(isAi: Boolean)
}
