package com.dulumina.androtik.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.RouterInfo
import com.dulumina.androtik.domain.model.RouterProfile
import com.dulumina.androtik.domain.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val profileName: String = "",
    val routerInfo: RouterInfo = RouterInfo(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false,
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val routerRepository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val profileId = sessionManager.activeProfileId
            if (profileId >= 0) {
                val profile = routerRepository.getProfileById(profileId)
                _uiState.value = _uiState.value.copy(profileName = profile?.name ?: "")
            }

            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.getSystemResources().fold(
                    onSuccess = { info ->
                        _uiState.value = _uiState.value.copy(
                            routerInfo = info,
                            isLoading = false
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load system info"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not connected"
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    class Factory(
        private val sessionManager: SessionManager,
        private val routerRepository: RouterRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(sessionManager, routerRepository) as T
        }
    }
}
