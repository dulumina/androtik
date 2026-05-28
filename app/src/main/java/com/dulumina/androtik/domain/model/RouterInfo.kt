package com.dulumina.androtik.domain.model

data class RouterInfo(
    val cpuLoad: String = "",
    val freeMemory: String = "",
    val totalMemory: String = "",
    val uptime: String = "",
    val boardName: String = "",
    val version: String = "",
    val cpuCount: String = "",
    val cpuFrequency: String = "",
    val freeHddSpace: String = "",
    val totalHddSpace: String = "",
)
