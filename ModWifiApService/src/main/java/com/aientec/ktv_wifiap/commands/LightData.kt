package com.aientec.ktv_wifiap.commands

import androidx.annotation.IntRange
import java.nio.ByteBuffer


/**
 * TODO
 *
 * @property toggle
 * @property brightness 0~100
 * @property colorTemperature 1000~10000
 */
class LightData(
    var toggle: Toggle = Toggle.NONE,
    var brightness: UShort = INVALID_VALUE,
    var colorTemperature: UShort = INVALID_VALUE
) : DSData() {
    override var length: UShort = 14u

    override val command: CommandList
        get() = CommandList.LIGHT_CTRL

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(toggle.code.toShort())
        buffer.putShort(brightness.toShort())
        buffer.putShort(colorTemperature.toShort())
    }

    override fun decodeBody(body: ByteBuffer) {
        val t = body.short.toUShort()
        toggle = Toggle.values().find { it.code == t } ?: return
        brightness = body.short.toUShort()
        colorTemperature = body.short.toUShort()
    }
}