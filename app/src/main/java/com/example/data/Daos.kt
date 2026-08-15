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

    @Query("UPDATE user_profiles SET isPlus = :isPlus WHERE id = 1")
    suspend fun updatePlusStatus(isPlus: Boolean)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): Lesson?

    @Query("SELECT * FROM lessons")
    suspend fun getAllLessonsList(): List<Lesson>

    @Query("UPDATE lessons SET isDownloaded = :downloaded WHERE id = :lessonId")
    suspend fun updateDownloadedStatus(lessonId: String, downloaded: Boolean)

    @Query("UPDATE lessons SET isUnlocked = :unlocked WHERE id = :lessonId")
    suspend fun updateUnlockedStatus(lessonId: String, unlocked: Boolean)

    @Query("UPDATE lessons SET isUnlocked = 1")
    suspend fun unlockAllLessons()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)
}

@Dao
interface LessonStepDao {
    @Query("SELECT * FROM lesson_steps WHERE lessonId = :lessonId ORDER BY stepNumber ASC")
    fun getStepsForLesson(lessonId: String): Flow<List<LessonStep>>

    @Query("SELECT * FROM lesson_steps WHERE lessonId = :lessonId ORDER BY stepNumber ASC")
    suspend fun getStepsForLessonList(lessonId: String): List<LessonStep>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<LessonStep>)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId")
    fun getQuizForLesson(lessonId: String): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId")
    suspend fun getQuizForLessonList(lessonId: String): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestion>)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress")
    suspend fun getAllProgressSynchronous(): List<UserProgress>

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

@Dao
interface BuddyDao {
    @Query("SELECT * FROM buddies ORDER BY xp DESC")
    fun getAllBuddies(): Flow<List<Buddy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuddies(buddies: List<Buddy>)

    @Query("UPDATE buddies SET isConnected = :connected WHERE id = :buddyId")
    suspend fun updateConnectionStatus(buddyId: String, connected: Boolean)

    @Query("SELECT * FROM buddy_messages WHERE buddyId = :buddyId ORDER BY timestamp ASC")
    fun getMessagesForBuddy(buddyId: String): Flow<List<BuddyMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuddyMessage(message: BuddyMessage)

    @Query("DELETE FROM buddy_messages WHERE buddyId = :buddyId")
    suspend fun clearBuddyMessages(buddyId: String)
}

@Dao
interface BuildDao {
    @Query("SELECT * FROM problem_builds ORDER BY dateCreated DESC")
    fun getAllBuilds(): Flow<List<ProblemBuild>>

    @Query("SELECT * FROM problem_builds WHERE id = :id LIMIT 1")
    suspend fun getBuildById(id: String): ProblemBuild?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: ProblemBuild)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilds(builds: List<ProblemBuild>)

    @Query("UPDATE problem_builds SET buildProgressPercent = :progress WHERE id = :id")
    suspend fun updateBuildProgress(id: String, progress: Int)
}

