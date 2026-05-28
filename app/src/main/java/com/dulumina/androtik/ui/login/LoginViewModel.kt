package com.dulumina.androtik.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.MikrotikClient
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.RouterProfile
import com.dulumina.androtik.domain.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val profiles: List<RouterProfile> = emptyList(),
    val selectedProfile: RouterProfile? = null,
    val host: String = "",
    val port: String = "8728",
    val useSsl: Boolean = false,
    val username: String = "",
    val password: String = "",
    val profileName: String = "",
    val isConnecting: Boolean = false,
    val error: String? = null,
    val connected: Boolean = false,
)

class LoginViewModel(
    private val routerRepository: RouterRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routerRepository.getAllProfiles().collect { profiles ->
                _uiState.value = _uiState.value.copy(profiles = profiles)
            }
        }
    }

    fun selectProfile(profile: RouterProfile) {
        _uiState.value = _uiState.value.copy(
            selectedProfile = profile,
            host = profile.host,
            port = profile.port.toString(),
            useSsl = profile.useSsl,
            username = profile.username,
            password = profile.password,
            profileName = profile.name
        )
    }

    fun updateHost(value: String) {
        _uiState.value = _uiState.value.copy(host = value, error = null)
    }

    fun updatePort(value: String) {
        _uiState.value = _uiState.value.copy(port = value, error = null)
    }

    fun updateUseSsl(value: Boolean) {
        _uiState.value = _uiState.value.copy(useSsl = value, port = if (value) "8729" else "8728")
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun updateProfileName(value: String) {
        _uiState.value = _uiState.value.copy(profileName = value)
    }

    fun connect() {
        val state = _uiState.value
        if (state.host.isBlank()) {
            _uiState.value = state.copy(error = "Host is required")
            return
        }
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Username is required")
            return
        }
        val port = state.port.toIntOrNull() ?: if (state.useSsl) 8729 else 8728

        _uiState.value = state.copy(isConnecting = true, error = null)

        viewModelScope.launch {
            val client = MikrotikClient(
                host = state.host,
                port = port,
                useSsl = state.useSsl
            )
            val result = client.connect(state.username, state.password)
            result.fold(
                onSuccess = {
                    saveOrUpdateProfile(port)
                    sessionManager.startSession(client, state.selectedProfile?.id ?: -1)
                    _uiState.value = _uiState.value.copy(
                        isConnecting = false,
                        connected = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    client.disconnect()
                    _uiState.value = _uiState.value.copy(
                        isConnecting = false,
                        error = e.message ?: "Connection failed"
                    )
                }
            )
        }
    }

    private suspend fun saveOrUpdateProfile(port: Int) {
        val state = _uiState.value
        val name = state.profileName.ifBlank { state.host }
        val profile = RouterProfile(
            id = state.selectedProfile?.id ?: 0,
            name = name,
            host = state.host,
            port = port,
            useSsl = state.useSsl,
            username = state.username,
            password = state.password
        )
        if (profile.id == 0L) {
            routerRepository.saveProfile(profile)
        } else {
            routerRepository.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: RouterProfile) {
        viewModelScope.launch {
            routerRepository.deleteProfile(profile)
            if (_uiState.value.selectedProfile?.id == profile.id) {
                _uiState.value = _uiState.value.copy(selectedProfile = null)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetConnection() {
        _uiState.value = _uiState.value.copy(connected = false)
    }

    class Factory(
        private val routerRepository: RouterRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(routerRepository, sessionManager) as T
        }
    }
}
