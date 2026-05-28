package com.dulumina.androtik.domain.repository

import com.dulumina.androtik.domain.model.RouterProfile
import kotlinx.coroutines.flow.Flow

interface RouterRepository {
    fun getAllProfiles(): Flow<List<RouterProfile>>
    suspend fun getProfileById(id: Long): RouterProfile?
    suspend fun saveProfile(profile: RouterProfile): Long
    suspend fun updateProfile(profile: RouterProfile)
    suspend fun deleteProfile(profile: RouterProfile)
    suspend fun updateLastConnected(id: Long, timestamp: Long)
}
