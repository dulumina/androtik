package com.dulumina.androtik.ui.interfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.NetworkInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InterfacesUiState(
    val interfaces: List<NetworkInterface> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class InterfacesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterfacesUiState())
    val uiState: StateFlow<InterfacesUiState> = _uiState.asStateFlow()

    init {
        loadInterfaces()
    }

    fun loadInterfaces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.getInterfaces().fold(
                    onSuccess = { interfaces ->
                        _uiState.value = _uiState.value.copy(
                            interfaces = interfaces,
                            isLoading = false
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load interfaces"
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

    class Factory(
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InterfacesViewModel(sessionManager) as T
        }
    }
}
