package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class TrackMoveData(var oldIndex: UShort, var newIndex: UShort, var id: UInt, var afterId: UInt) :
    DSData() {

    constructor() : this(0u, 0u, 0u, 0u)

    override val command: CommandList
        get() = CommandList.MOVE_SONG

    override var length: UShort = 20u

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(oldIndex.toShort())
        buffer.putShort(newIndex.toShort())
        buffer.putInt(id.toInt())
        buffer.putInt(afterId.toInt())
    }

    override fun decodeBody(body: ByteBuffer) {
        oldIndex = body.short.toUShort()
        newIndex = body.short.toUShort()
        id = body.int.toUInt()
        afterId = body.int.toUInt()
    }
}