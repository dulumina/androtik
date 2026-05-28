package com.dulumina.androtik.domain.model

data class FirewallRule(
    val id: String = "",
    val chain: String = "",
    val action: String = "",
    val protocol: String = "",
    val srcAddress: String = "",
    val dstAddress: String = "",
    val srcPort: String = "",
    val dstPort: String = "",
    val inInterface: String = "",
    val outInterface: String = "",
    val disabled: Boolean = false,
    val dynamic: Boolean = false,
    val bytes: String = "",
    val packets: String = "",
    val comment: String = "",
)
