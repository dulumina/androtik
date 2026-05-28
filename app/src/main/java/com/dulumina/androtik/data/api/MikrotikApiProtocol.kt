package com.dulumina.androtik.data.api

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object MikrotikApiProtocol {

    fun encodeLength(value: Int, os: OutputStream) {
        when {
            value < 0x80 -> os.write(value)
            value < 0x4000 -> {
                os.write(0x80 or (value shr 8))
                os.write(value and 0xFF)
            }
            value < 0x200000 -> {
                os.write(0xC0 or (value shr 16))
                os.write((value shr 8) and 0xFF)
                os.write(value and 0xFF)
            }
            value < 0x10000000 -> {
                os.write(0xE0 or (value shr 24))
                os.write((value shr 16) and 0xFF)
                os.write((value shr 8) and 0xFF)
                os.write(value and 0xFF)
            }
            else -> {
                os.write(0xF0)
                os.write((value shr 24) and 0xFF)
                os.write((value shr 16) and 0xFF)
                os.write((value shr 8) and 0xFF)
                os.write(value and 0xFF)
            }
        }
    }

    fun decodeLength(inputStream: InputStream): Int {
        val byte = inputStream.read()
        if (byte == -1) throw MikrotikProtocolException("Connection closed")
        return when {
            byte < 0x80 -> byte
            byte < 0xC0 -> {
                val b2 = inputStream.read()
                if (b2 == -1) throw MikrotikProtocolException("Connection closed")
                ((byte and 0x3F) shl 8) or b2
            }
            byte < 0xE0 -> {
                val b2 = inputStream.read()
                val b3 = inputStream.read()
                if (b2 == -1 || b3 == -1) throw MikrotikProtocolException("Connection closed")
                ((byte and 0x1F) shl 16) or (b2 shl 8) or b3
            }
            byte < 0xF0 -> {
                val values = inputStream.readNBytes(3)
                if (values.size < 3) throw MikrotikProtocolException("Connection closed")
                ((byte and 0x0F) shl 24) or ((values[0].toInt() and 0xFF) shl 16) or
                        ((values[1].toInt() and 0xFF) shl 8) or (values[2].toInt() and 0xFF)
            }
            else -> {
                val values = inputStream.readNBytes(4)
                if (values.size < 4) throw MikrotikProtocolException("Connection closed")
                ((values[0].toInt() and 0xFF) shl 24) or ((values[1].toInt() and 0xFF) shl 16) or
                        ((values[2].toInt() and 0xFF) shl 8) or (values[3].toInt() and 0xFF)
            }
        }
    }

    fun writeWord(os: OutputStream, word: String) {
        val bytes = word.toByteArray(Charsets.UTF_8)
        encodeLength(bytes.size, os)
        os.write(bytes)
    }

    fun readWord(inputStream: InputStream): String {
        val length = decodeLength(inputStream)
        if (length > 0) {
            val bytes = inputStream.readNBytes(length)
            return String(bytes, Charsets.UTF_8)
        }
        return ""
    }

    fun readSentence(inputStream: InputStream): List<String> {
        val words = mutableListOf<String>()
        while (true) {
            val word = readWord(inputStream)
            if (word.isEmpty()) break
            words.add(word)
        }
        return words
    }

    fun writeSentence(os: OutputStream, words: List<String>) {
        for (word in words) {
            writeWord(os, word)
        }
        os.write(0x00)
        os.flush()
    }

    fun encodeSentence(vararg args: String): ByteArray {
        val baos = ByteArrayOutputStream()
        for (arg in args) {
            writeWord(baos, arg)
        }
        baos.write(0x00)
        return baos.toByteArray()
    }

    fun parseResponse(sentence: List<String>): MikrotikResponse {
        if (sentence.isEmpty()) return MikrotikResponse.Unknown
        return when (val tag = sentence[0]) {
            "!done" -> {
                MikrotikResponse.Done(parseAttributes(sentence.drop(1)))
            }
            "!re" -> {
                MikrotikResponse.Re(parseAttributes(sentence.drop(1)))
            }
            "!trap" -> {
                val attrs = parseAttributes(sentence.drop(1))
                MikrotikResponse.Trap(attrs["message"] ?: "Unknown error", attrs["category"] ?: "", attrs)
            }
            "!fatal" -> {
                MikrotikResponse.Fatal(sentence.getOrElse(1) { "" })
            }
            else -> MikrotikResponse.Unknown
        }
    }

    private fun parseAttributes(words: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (word in words) {
            if (word.startsWith("=")) {
                val eqIdx = word.indexOf('=', 1)
                if (eqIdx > 0) {
                    val key = word.substring(1, eqIdx)
                    val value = word.substring(eqIdx + 1)
                    map[key] = value
                } else {
                    map[word.substring(1)] = ""
                }
            }
        }
        return map
    }
}

class MikrotikProtocolException(message: String, cause: Throwable? = null) : Exception(message, cause)

sealed class MikrotikResponse {
    data class Done(val attributes: Map<String, String>) : MikrotikResponse()
    data class Re(val attributes: Map<String, String>) : MikrotikResponse()
    data class Trap(val message: String, val category: String, val attributes: Map<String, String>) : MikrotikResponse()
    data class Fatal(val message: String) : MikrotikResponse()
    data object Unknown : MikrotikResponse()
}
