package com.dulumina.androtik.data.api

import com.dulumina.androtik.domain.model.IpAddress
import com.dulumina.androtik.domain.model.IpRoute
import com.dulumina.androtik.domain.model.NetworkInterface
import com.dulumina.androtik.domain.model.RouterInfo

class MikrotikClient(
    private val host: String,
    private val port: Int = 8728,
    private val useSsl: Boolean = false,
) {
    private val connection = MikrotikConnection(host, port, useSsl)

    val isConnected: Boolean get() = connection.isConnected

    suspend fun connect(username: String, password: String): Result<Unit> {
        return connection.connect()
            .mapCatching { connection.login(username, password).getOrThrow() }
    }

    suspend fun disconnect() {
        connection.disconnect()
    }

    suspend fun getSystemResources(): Result<RouterInfo> {
        return connection.executeCommand("/system/resource/print").map { rows ->
            if (rows.isNotEmpty()) {
                val row = rows[0]
                RouterInfo(
                    cpuLoad = row["cpu-load"] ?: "",
                    freeMemory = formatBytes(row["free-memory"]?.toLongOrNull() ?: 0),
                    totalMemory = formatBytes(row["total-memory"]?.toLongOrNull() ?: 0),
                    uptime = row["uptime"] ?: "",
                    boardName = row["board-name"] ?: "",
                    version = row["version"] ?: "",
                    cpuCount = row["cpu-count"] ?: "",
                    cpuFrequency = row["cpu-frequency"] ?: "",
                    freeHddSpace = formatBytes(row["free-hdd-space"]?.toLongOrNull() ?: 0),
                    totalHddSpace = formatBytes(row["total-hdd-space"]?.toLongOrNull() ?: 0),
                )
            } else RouterInfo()
        }
    }

    suspend fun getInterfaces(): Result<List<NetworkInterface>> {
        return connection.executeCommand("/interface/print").map { rows ->
            rows.map { row ->
                NetworkInterface(
                    name = row["name"] ?: "",
                    type = row["type"] ?: "",
                    macAddress = row["mac-address"] ?: "",
                    running = row["running"] == "true",
                    disabled = row["disabled"] == "true",
                    comment = row["comment"] ?: "",
                    mtu = row["mtu"] ?: "",
                    txRate = row["tx-current"] ?: "",
                    rxRate = row["rx-current"] ?: "",
                )
            }
        }
    }

    suspend fun getIpAddresses(): Result<List<IpAddress>> {
        return connection.executeCommand("/ip/address/print").map { rows ->
            rows.map { row ->
                IpAddress(
                    id = row[".id"] ?: "",
                    address = row["address"] ?: "",
                    network = row["network"] ?: "",
                    interfaceName = row["interface"] ?: "",
                    disabled = row["disabled"] == "true",
                    dynamic = row["dynamic"] == "true",
                    comment = row["comment"] ?: "",
                )
            }
        }
    }

    suspend fun addIpAddress(address: String, interfaceName: String, comment: String = ""): Result<Unit> {
        val args = mutableMapOf("address" to address, "interface" to interfaceName)
        if (comment.isNotBlank()) args["comment"] = comment
        return connection.executeCommand("/ip/address/add", args).map { }
    }

    suspend fun removeIpAddress(id: String): Result<Unit> {
        return connection.executeCommand("/ip/address/remove", mapOf(".id" to id)).map { }
    }

    suspend fun getIpRoutes(): Result<List<IpRoute>> {
        return connection.executeCommand("/ip/route/print").map { rows ->
            rows.map { row ->
                IpRoute(
                    id = row[".id"] ?: "",
                    dstAddress = row["dst-address"] ?: "",
                    gateway = row["gateway"] ?: "",
                    distance = row["distance"] ?: "",
                    routingMark = row["routing-mark"] ?: "",
                    interfaceName = row["interface"] ?: "",
                    disabled = row["disabled"] == "true",
                    dynamic = row["dynamic"] == "true",
                    active = row["active"] == "true",
                    comment = row["comment"] ?: "",
                )
            }
        }
    }

    suspend fun execute(command: String, args: Map<String, String> = emptyMap()): Result<List<Map<String, String>>> {
        return connection.executeCommand(command, args)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> String.format("%.1f GiB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> String.format("%.1f MiB", bytes / 1_048_576.0)
            bytes >= 1_024 -> String.format("%.1f KiB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
