package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

/**
 * TODO
 *
 */

class RoomEffectData(
      var code: UShort = INVALID_VALUE,
      var type: UShort = 0x0001u
) : DSData() {

      override var length: UShort = 12u

      override val command: CommandList
            get() = CommandList.ROOM_EFFECT

      override fun encodeBody(buffer: ByteBuffer) {
            buffer.putShort(code.toShort())
            buffer.putShort(type.toShort())
      }

      override fun decodeBody(body: ByteBuffer) {
            code = body.short.toUShort()
            type = body.short.toUShort()
      }
}