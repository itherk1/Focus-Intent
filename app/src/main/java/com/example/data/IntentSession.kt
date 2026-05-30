package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intent_sessions")
data class IntentSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val delayDurationSeconds: Int,
    val userContinued: Boolean
)
