package com.dulumina.androtik.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dulumina.androtik.domain.model.RouterProfile

@Entity(tableName = "router_profiles")
data class RouterProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 8728,
    @ColumnInfo(name = "use_ssl") val useSsl: Boolean = false,
    val username: String,
    val password: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_connected_at") val lastConnectedAt: Long? = null
) {
    fun toDomain(): RouterProfile = RouterProfile(
        id = id, name = name, host = host, port = port, useSsl = useSsl,
        username = username, password = password, createdAt = createdAt,
        lastConnectedAt = lastConnectedAt
    )

    companion object {
        fun fromDomain(profile: RouterProfile): RouterProfileEntity = RouterProfileEntity(
            id = profile.id, name = profile.name, host = profile.host, port = profile.port,
            useSsl = profile.useSsl, username = profile.username, password = profile.password,
            createdAt = profile.createdAt, lastConnectedAt = profile.lastConnectedAt
        )
    }
}
