package com.dulumina.androtik.domain.model

data class NetworkInterface(
    val name: String,
    val type: String,
    val macAddress: String = "",
    val running: Boolean = false,
    val disabled: Boolean = false,
    val comment: String = "",
    val mtu: String = "",
    val txRate: String = "",
    val rxRate: String = "",
)
