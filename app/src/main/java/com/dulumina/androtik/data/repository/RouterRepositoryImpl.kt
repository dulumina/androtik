package com.dulumina.androtik.data.repository

import com.dulumina.androtik.data.local.RouterProfileDao
import com.dulumina.androtik.data.local.RouterProfileEntity
import com.dulumina.androtik.domain.model.RouterProfile
import com.dulumina.androtik.domain.repository.RouterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RouterRepositoryImpl(private val dao: RouterProfileDao) : RouterRepository {

    override fun getAllProfiles(): Flow<List<RouterProfile>> {
        return dao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProfileById(id: Long): RouterProfile? {
        return dao.getProfileById(id)?.toDomain()
    }

    override suspend fun saveProfile(profile: RouterProfile): Long {
        return dao.insert(RouterProfileEntity.fromDomain(profile))
    }

    override suspend fun updateProfile(profile: RouterProfile) {
        dao.update(RouterProfileEntity.fromDomain(profile))
    }

    override suspend fun deleteProfile(profile: RouterProfile) {
        dao.delete(RouterProfileEntity.fromDomain(profile))
    }

    override suspend fun updateLastConnected(id: Long, timestamp: Long) {
        dao.updateLastConnected(id, timestamp)
    }
}
