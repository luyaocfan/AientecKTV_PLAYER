package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class ACData(
    var speed: UShort = INVALID_VALUE,
    var temperature: UShort = INVALID_VALUE
) : DSData() {
    override var length: UShort = 12u

    override val command: CommandList
        get() = CommandList.AC_CTRL

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(speed.toShort())
        buffer.putShort(temperature.toShort())
    }

    override fun decodeBody(body: ByteBuffer) {
        speed = body.short.toUShort()
        temperature = body.short.toUShort()
    }
}