package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class VoiceData(
      var toggle: Toggle = Toggle.NONE,
      var micVolume: UShort = INVALID_VALUE,
      var musicVolume: UShort = INVALID_VALUE,
      var effect: UShort = INVALID_VALUE,
      var micMode: UShort = INVALID_VALUE,
      var modulate: UShort = INVALID_VALUE,
      var audioSrc: UShort = INVALID_VALUE
) : DSData() {
      override val command: CommandList
            get() = CommandList.VOICE_CTRL

      override var length: UShort = 20u

      override fun encodeBody(buffer: ByteBuffer) {
            buffer.apply {
//                  putShort(toggle.code.toShort())
                  putShort(micVolume.toShort())
                  putShort(musicVolume.toShort())
                  putShort(effect.toShort())
                  putShort(micMode.toShort())
                  putShort(modulate.toShort())
                  putShort(audioSrc.toShort())
            }
      }

      override fun decodeBody(body: ByteBuffer) {
            body.apply {
//                  val t = short.toUShort()
//                  toggle = Toggle.values().find { it.code == t } ?: Toggle.NONE
                  try {
                        micVolume = short.toUShort()
                        musicVolume = short.toUShort()
                        effect = short.toUShort()
                        micMode = short.toUShort()
                        modulate = short.toUShort()
                        audioSrc = short.toUShort()
                  } catch (e: Exception) {
                        e.printStackTrace()
                  }
            }
      }
}