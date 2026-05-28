package com.dulumina.androtik.data.api

class SessionManager {
    private var currentClient: MikrotikClient? = null
    private var currentProfileId: Long = -1

    val isConnected: Boolean get() = currentClient?.isConnected == true
    val activeProfileId: Long get() = currentProfileId

    fun startSession(client: MikrotikClient, profileId: Long) {
        currentClient = client
        currentProfileId = profileId
    }

    fun getClient(): MikrotikClient? = currentClient

    suspend fun endSession() {
        currentClient?.disconnect()
        currentClient = null
        currentProfileId = -1
    }
}
