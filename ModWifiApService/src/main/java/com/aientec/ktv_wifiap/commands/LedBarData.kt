package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

/**
 * TODO
 *
 */

class LedBarData(
      var basicLightMode: UShort = INVALID_VALUE,
      var subMode: UShort = INVALID_VALUE,
      var mode: UShort = INVALID_VALUE
) : DSData() {

      override var length: UShort = 14u

      override val command: CommandList
            get() = CommandList.LED_BAR_CTRL

      override fun encodeBody(buffer: ByteBuffer) {
            buffer.putShort(basicLightMode.toShort())
            buffer.putShort(subMode.toShort())
            buffer.putShort(mode.toShort())
      }

      override fun decodeBody(body: ByteBuffer) {
            basicLightMode = body.short.toUShort()
            subMode = body.short.toUShort()
            mode = body.short.toUShort()
      }
}