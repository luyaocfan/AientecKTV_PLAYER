package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class InitData(
      var basicLightMode: UShort = INVALID_VALUE,
      var subModeCode: UShort = INVALID_VALUE,
      var mainModeCode: UShort = INVALID_VALUE,
      var micVolume: UShort = INVALID_VALUE,
      var musicVolume: UShort = INVALID_VALUE,
      var effectVolume: UShort = INVALID_VALUE,
      var micMode: UShort = INVALID_VALUE,
      var modulate: UShort = INVALID_VALUE,
      var audioSrc: UShort = INVALID_VALUE
) : DSData() {
      override val command: DSData.CommandList = DSData.CommandList.INIT_NOTIFY


      override fun encodeBody(buffer: ByteBuffer) {

      }

      override fun decodeBody(body: ByteBuffer) {
            try {
                  basicLightMode = body.short.toUShort()

                  subModeCode = body.short.toUShort()

                  mainModeCode = body.short.toUShort()

                  micVolume = body.short.toUShort()

                  musicVolume = body.short.toUShort()

                  effectVolume = body.short.toUShort()

                  micMode = body.short.toUShort()

                  modulate = body.short.toUShort()

                  audioSrc = body.short.toUShort()
            } catch (e: Exception) {
                  e.printStackTrace()
            }
      }
}