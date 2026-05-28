package com.dulumina.androtik.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

class MikrotikConnection(
    private val host: String,
    private val port: Int,
    private val useSsl: Boolean = false,
    private val timeoutMs: Int = 5000
) {
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.isConnected == true && socket?.isClosed == false

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sock = if (useSsl) {
                SSLSocketFactory.getDefault().createSocket() as Socket
            } else {
                Socket()
            }
            sock.connect(InetSocketAddress(host, port), timeoutMs)
            sock.soTimeout = timeoutMs
            sock.keepAlive = true
            socket = sock
            inputStream = sock.getInputStream()
            outputStream = sock.getOutputStream()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(MikrotikProtocolException("Failed to connect to $host:$port", e))
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            socket = null
            inputStream = null
            outputStream = null
        }
    }

    fun readSentence(): List<String> {
        val input = inputStream ?: throw MikrotikProtocolException("Not connected")
        return MikrotikApiProtocol.readSentence(input)
    }

    fun writeSentence(words: List<String>) {
        val output = outputStream ?: throw MikrotikProtocolException("Not connected")
        MikrotikApiProtocol.writeSentence(output, words)
    }

    suspend fun executeCommand(
        command: String,
        args: Map<String, String> = emptyMap(),
        queries: List<String> = emptyList()
    ): Result<List<Map<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val words = mutableListOf(command)
            for ((key, value) in args) {
                words.add("=$key=$value")
            }
            for (query in queries) {
                words.add("?$query")
            }
            writeSentence(words)

            val results = mutableListOf<Map<String, String>>()
            while (true) {
                val sentence = readSentence()
                if (sentence.isEmpty()) continue
                when (val response = MikrotikApiProtocol.parseResponse(sentence)) {
                    is MikrotikResponse.Re -> results.add(response.attributes)
                    is MikrotikResponse.Done -> return@withContext Result.success(results)
                    is MikrotikResponse.Trap -> return@withContext Result.failure(
                        MikrotikProtocolException(response.message)
                    )
                    is MikrotikResponse.Fatal -> return@withContext Result.failure(
                        MikrotikProtocolException("Fatal: ${response.message}")
                    )
                    is MikrotikResponse.Unknown -> {}
                }
            }
        } catch (e: MikrotikProtocolException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(MikrotikProtocolException("Command failed: $command", e))
        }
    }

    suspend fun login(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            writeSentence(listOf("/login", "=name=$username", "=password=$password"))
            val sentence = readSentence()
            val response = MikrotikApiProtocol.parseResponse(sentence)

            return@withContext when (response) {
                is MikrotikResponse.Done -> Result.success(Unit)
                is MikrotikResponse.Trap -> {
                    if (response.message.contains("challenge", ignoreCase = true)) {
                        handleChallengeLogin(username, password, response.attributes["ret"] ?: "")
                    } else {
                        Result.failure(MikrotikProtocolException(response.message))
                    }
                }
                else -> Result.failure(MikrotikProtocolException("Login failed: unexpected response"))
            }
        } catch (e: Exception) {
            Result.failure(MikrotikProtocolException("Login failed", e))
        }
    }

    private suspend fun handleChallengeLogin(
        username: String,
        password: String,
        challenge: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val chalBytes = hexToBytes(challenge)
            val zeroByte = byteArrayOf(0x00)
            val passBytes = password.toByteArray(Charsets.UTF_8)
            val md5Input = zeroByte + passBytes + chalBytes

            val md = java.security.MessageDigest.getInstance("MD5")
            val hash = md.digest(md5Input)
            val hashHex = bytesToHex(hash)

            writeSentence(listOf("/login", "=name=$username", "=response=00$hashHex"))
            val sentence = readSentence()
            val response = MikrotikApiProtocol.parseResponse(sentence)

            return@withContext when (response) {
                is MikrotikResponse.Done -> Result.success(Unit)
                is MikrotikResponse.Trap -> Result.failure(
                    MikrotikProtocolException(response.message)
                )
                else -> Result.failure(MikrotikProtocolException("Challenge login failed"))
            }
        } catch (e: Exception) {
            Result.failure(MikrotikProtocolException("Challenge login failed", e))
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}
