package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class RoomSwitchData : DSData() {
    override val command: CommandList
        get() = CommandList.ROOM_SWITCH

    private var mSwitch: Byte = 0x00

    val switch: Boolean
        get() {
            return mSwitch.toInt() == 1
        }

    override fun encodeBody(buffer: ByteBuffer) {

    }

    override fun decodeBody(body: ByteBuffer) {
        mSwitch = body.get()
    }
}