package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class LogData : DSData() {
    override val command: CommandList
        get() = CommandList.DEBUG_LOG

    var msg: String = ""

    override fun encodeBody(buffer: ByteBuffer) {

    }

    override fun decodeBody(body: ByteBuffer) {
        val type: Byte = body.get()

        val length: UShort = body.short.toUShort()

        val data: ByteArray = ByteArray(length.toInt())

        body.get(data)

        msg = String(data, Charsets.UTF_8)
    }
}