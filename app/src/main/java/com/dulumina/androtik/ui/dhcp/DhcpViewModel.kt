package com.dulumina.androtik.ui.dhcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.DhcpLease
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DhcpUiState(
    val leases: List<DhcpLease> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val isAdding: Boolean = false,
    val addError: String? = null,
)

class DhcpViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DhcpUiState())
    val uiState: StateFlow<DhcpUiState> = _uiState.asStateFlow()

    init {
        loadLeases()
    }

    fun loadLeases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.getDhcpLeases().fold(
                    onSuccess = { list ->
                        _uiState.value = _uiState.value.copy(leases = list, isLoading = false)
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

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, addError = null)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, addError = null)
    }

    fun addLease(address: String, macAddress: String, comment: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdding = true, addError = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.addDhcpLease(address, macAddress, comment).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isAdding = false, showAddDialog = false)
                        loadLeases()
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isAdding = false, addError = e.message)
                    }
                )
            }
        }
    }

    fun removeLease(id: String) {
        viewModelScope.launch {
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.removeDhcpLease(id).fold(
                    onSuccess = { loadLeases() },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                )
            }
        }
    }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DhcpViewModel(sessionManager) as T
    }
}
