package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        Lesson::class,
        LessonStep::class,
        QuizQuestion::class,
        UserProgress::class,
        CodingChallenge::class,
        DiscussionPost::class,
        MentorChat::class,
        PersonEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonStepDao(): LessonStepDao
    abstract fun quizDao(): QuizDao
    abstract fun progressDao(): ProgressDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun discussionDao(): DiscussionDao
    abstract fun chatDao(): ChatDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kodemamas_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
