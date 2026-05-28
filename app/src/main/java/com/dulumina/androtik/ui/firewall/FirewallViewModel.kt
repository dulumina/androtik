package com.dulumina.androtik.ui.firewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.domain.model.FirewallRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FirewallUiState(
    val rules: List<FirewallRule> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class FirewallViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirewallUiState())
    val uiState: StateFlow<FirewallUiState> = _uiState.asStateFlow()

    private val tabs = listOf("filter", "nat", "mangle")

    init {
        loadRules()
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        loadRules()
    }

    fun loadRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val client = sessionManager.getClient()
            val type = tabs[_uiState.value.selectedTab]
            if (client != null && client.isConnected) {
                client.getFirewallRules(type).fold(
                    onSuccess = { list ->
                        _uiState.value = _uiState.value.copy(rules = list, isLoading = false)
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
            FirewallViewModel(sessionManager) as T
    }
}
