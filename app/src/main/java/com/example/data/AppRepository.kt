package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: IntentDao) {
    val allSessions: Flow<List<IntentSession>> = dao.getAllSessions()
    
    suspend fun insertSession(session: IntentSession) {
        dao.insertSession(session)
    }
    
    fun getSessionsCountSince(since: Long): Flow<Int> = dao.getSessionsCountSince(since)
    fun getPreventedLaunchesSince(since: Long): Flow<Int> = dao.getPreventedLaunchesSince(since)
}
