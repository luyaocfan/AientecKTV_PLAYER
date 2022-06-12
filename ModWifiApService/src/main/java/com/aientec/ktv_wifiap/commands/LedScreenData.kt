package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class LedScreenData(var toggle: Toggle = Toggle.NONE) : DSData() {

    override var length: UShort = 10u

    override val command: CommandList
        get() = CommandList.LED_SCREEN_CTRL

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(toggle.code.toShort())
    }

    override fun decodeBody(body: ByteBuffer) {
        val t = body.short.toUShort()

        toggle = Toggle.values().find { it.code == t } ?: Toggle.NONE
    }
}