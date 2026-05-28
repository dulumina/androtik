package com.dulumina.androtik.domain.model

data class IpRoute(
    val id: String = "",
    val dstAddress: String = "",
    val gateway: String = "",
    val distance: String = "",
    val routingMark: String = "",
    val interfaceName: String = "",
    val disabled: Boolean = false,
    val dynamic: Boolean = false,
    val active: Boolean = false,
    val comment: String = "",
)
