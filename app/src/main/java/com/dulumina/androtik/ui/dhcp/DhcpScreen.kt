package com.dulumina.androtik.ui.dhcp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dulumina.androtik.domain.model.DhcpLease

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhcpScreen(
    viewModel: DhcpViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DHCP Leases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLeases() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.leases) { lease ->
                        DhcpLeaseCard(lease, onDelete = {
                            if (!lease.dynamic) viewModel.removeLease(lease.id)
                        })
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddDhcpLeaseDialog(
            isAdding = state.isAdding,
            error = state.addError,
            onConfirm = viewModel::addLease,
            onDismiss = viewModel::hideAddDialog
        )
    }
}

@Composable
private fun DhcpLeaseCard(lease: DhcpLease, onDelete: () -> Unit) {
    val statusColor = when {
        lease.blocked -> MaterialTheme.colorScheme.error
        lease.active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Computer, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lease.hostName.ifBlank { lease.macAddress },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = lease.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(lease.macAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (lease.server.isNotBlank()) {
                        Text(" · ${lease.server}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
                    Text(lease.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                    if (lease.dynamic) {
                        Text(" · dynamic",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                    if (lease.expiresAfter.isNotBlank()) {
                        Text(" · expires: ${lease.expiresAfter}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (lease.comment.isNotBlank()) {
                    Text(lease.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!lease.dynamic) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddDhcpLeaseDialog(
    isAdding: Boolean,
    error: String?,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var address by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Static Lease") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("IP Address") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = macAddress, onValueChange = { macAddress = it },
                    label = { Text("MAC Address") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = comment, onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(address, macAddress, comment) },
                enabled = !isAdding && address.isNotBlank() && macAddress.isNotBlank()) {
                if (isAdding) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
