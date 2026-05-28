package com.dulumina.androtik.domain.model

data class DhcpLease(
    val id: String = "",
    val address: String = "",
    val macAddress: String = "",
    val hostName: String = "",
    val clientId: String = "",
    val server: String = "",
    val status: String = "",
    val expiresAfter: String = "",
    val lastSeen: String = "",
    val active: Boolean = false,
    val dynamic: Boolean = false,
    val blocked: Boolean = false,
    val comment: String = "",
)
