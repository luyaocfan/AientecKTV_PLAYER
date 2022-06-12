package com.aientec.ktv_wifiap.commands

import androidx.annotation.Size
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RegisterData(
    var type: ClientType = ClientType.NONE,
    var id: UInt = 0u,
    @Size(32)
    var token: String? = null
) : DSData() {
    enum class ClientType(var code: UShort) {
        NONE(0xFFFFu),
        APP(0x00u),
        VOD(0x01u),
        WEB(0x02u),
        PLAYER(0x03u)
    }

    override val command: CommandList
        get() = CommandList.REGISTER

    override var length: UShort = 46u

//    override var length: UShort = 14u


    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(type.code.toShort())
        buffer.putInt(id.toInt())

        if (token != null)
            buffer.put(token!!.toByteArray(Charset.defaultCharset()))
        else
            buffer.position(buffer.position() + 32)
    }

    override fun decodeBody(body: ByteBuffer) {
        val t: UShort = body.short.toUShort()

        type = ClientType.values().find {
            it.code == t
        } ?: ClientType.NONE

        if (type == ClientType.APP) {
            id = body.int.toUInt()

            val bytes: ByteArray = ByteArray(32)
            body.get(bytes)

            token = String(bytes, Charset.defaultCharset())
        }


    }
}