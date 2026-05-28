package com.dulumina.androtik.domain.model

data class RouterProfile(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 8728,
    val useSsl: Boolean = false,
    val username: String,
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastConnectedAt: Long? = null
)
