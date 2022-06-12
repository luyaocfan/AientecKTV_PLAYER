package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class TrackDelData(var index: UShort, var id: UInt) : DSData() {

    constructor() : this(0u, 0u)

    override val command: CommandList
        get() = CommandList.DEL_SONG

    override var length: UShort = 14u

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(index.toShort())
        buffer.putInt(id.toInt())
    }

    override fun decodeBody(body: ByteBuffer) {
        index = body.short.toUShort()
        id = body.int.toUInt()
    }
}