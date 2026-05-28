package com.dulumina.androtik.ui.ipaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.IpAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IpAddressUiState(
    val addresses: List<IpAddress> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val isAdding: Boolean = false,
    val addError: String? = null,
)

class IpAddressViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IpAddressUiState())
    val uiState: StateFlow<IpAddressUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.getIpAddresses().fold(
                    onSuccess = { list ->
                        _uiState.value = _uiState.value.copy(
                            addresses = list, isLoading = false
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, error = e.message
                        )
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

    fun addAddress(address: String, interfaceName: String, comment: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdding = true, addError = null)
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.addIpAddress(address, interfaceName, comment).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isAdding = false, showAddDialog = false
                        )
                        loadAddresses()
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isAdding = false, addError = e.message
                        )
                    }
                )
            }
        }
    }

    fun removeAddress(id: String) {
        viewModelScope.launch {
            val client = sessionManager.getClient()
            if (client != null && client.isConnected) {
                client.removeIpAddress(id).fold(
                    onSuccess = { loadAddresses() },
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
            IpAddressViewModel(sessionManager) as T
    }
}
