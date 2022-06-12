package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class TmsData(
    var id: UInt = 0u,
    var type: Type = Type.NONE,
    var dataLength: UShort = INVALID_VALUE,
) : DSData() {
    enum class Type(var code: UByte) {
        NONE(0xFFu),
        TEXT(0x01u),
        PICTURE(0x02u),
        VIDEO(0x03u),
        EMOJI(0x04u),
        VOD(0xF0u)
    }

    override var length: UShort = 15u

    override val command: CommandList
        get() = CommandList.VOD_TMS_CTRL

    var data: ByteArray? = null
        set(value) {
            field = value
            if (field != null)
                length = (15 + field!!.size).toUShort()
        }

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putInt(id.toInt())
        buffer.put(type.code.toByte())
        buffer.putShort(dataLength.toShort())
        buffer.put(data)
    }

    override fun decodeBody(body: ByteBuffer) {
        id = body.int.toUInt()
        val t = body.get().toUByte()
        type = Type.values().find { it.code == t } ?: Type.NONE

        dataLength = body.short.toUShort()

        data = ByteArray(dataLength.toInt())

        body.get(data)
    }
}