package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentDao {
    @Query("SELECT * FROM intent_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<IntentSession>>

    @Insert
    suspend fun insertSession(session: IntentSession)

    @Query("SELECT COUNT(*) FROM intent_sessions WHERE timestamp >= :since")
    fun getSessionsCountSince(since: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM intent_sessions WHERE timestamp >= :since AND userContinued = 0")
    fun getPreventedLaunchesSince(since: Long): Flow<Int>
}

@Database(entities = [IntentSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intentDao(): IntentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focus-intent-db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
