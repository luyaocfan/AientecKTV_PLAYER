package com.aientec.ktv_wifiap.commands

import android.util.Log
import androidx.annotation.Size
import java.nio.ByteBuffer
import java.nio.charset.Charset

class StateData(
    var userId: UInt = 0u,
    var state: State = State.NONE,
    @Size(32) var token: String? = null
) :
    DSData() {
    enum class State(var code: Byte) {
        NONE(0xFF.toByte()),
        OFFLINE(0x00),
        ONLINE(0x01)
    }

    override var length: UShort = 45u

    override val command: CommandList
        get() = CommandList.STATE_NOTIFY

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putInt(userId.toInt())
        buffer.put(state.code)
        if (token != null)
            buffer.put(token!!.toByteArray(Charset.defaultCharset()))
    }

    override fun decodeBody(body: ByteBuffer) {
        userId = body.int.toUInt()

        val byte: Byte = body.get()


        state = State.values().find {
            it.code == byte
        } ?: State.NONE

        val bytes: ByteArray = ByteArray(32)
        body.get(bytes)


        token = String(bytes, Charset.defaultCharset())

        Log.d("Trace", "UserId : $userId, State : $byte, Token : $token")
    }
}