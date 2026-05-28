package com.dulumina.androtik.ui.iproutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.IpRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IpRoutesUiState(
    val routes: List<IpRoute> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class IpRoutesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IpRoutesUiState())
    val uiState: StateFlow<IpRoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.getIpRoutes().fold(
                    onSuccess = { list ->
                        _uiState.value = _uiState.value.copy(routes = list, isLoading = false)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Not connected")
            }
        }
    }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            IpRoutesViewModel(sessionManager) as T
    }
}
