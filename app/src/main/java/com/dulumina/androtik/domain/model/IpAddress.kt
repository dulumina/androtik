package com.dulumina.androtik.domain.model

data class IpAddress(
    val id: String = "",
    val address: String = "",
    val network: String = "",
    val interfaceName: String = "",
    val disabled: Boolean = false,
    val dynamic: Boolean = false,
    val comment: String = "",
)
