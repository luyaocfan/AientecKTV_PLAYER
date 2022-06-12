package com.aientec.ktv_wifiap.commands

import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer


abstract class DSData {

      enum class Toggle(var code: UShort) {
            NONE(0xFFFFu),
            OFF(0x0000u),
            ON(0x0001u)
      }

      enum class CommandList(var code: UShort) {
            NONE(0x0000u),
            REGISTER(0x700Bu),
            LIGHT_CTRL(0x7001u),
            LED_BAR_CTRL(0x7002u),
            LED_SCREEN_CTRL(0x7003u),
            VOICE_CTRL(0x7004u),
            SCORE_CTRL(0x7005u),
            AC_CTRL(0x7006u),
            SERVICE_CTRL(0x7007u),
            VOD_PLAY_CTRL(0x7008u),
            VOD_TMS_CTRL(0x7009u),
            VOD_CONNECT_CTRL(0x700Au),
            STATE_NOTIFY(0x700Cu),
            DEBUG_LOG(0x700Eu),
            SCENE(0x700Fu),

            //New cmd
            TRACKS_UPDATE(0x3001u),
            ADD_SONG(0x7012u),
            INSERT_SONG(0x7013u),
            MOVE_SONG(0x7014u),
            DEL_SONG(0x7015u),
            NEXT_SONG(0x7016u),
            ROOM_EFFECT(0x7017u),
            NEXT_SONG_ACK(0x3016u),
            ROOM_SWITCH(0x3003u),
            INIT_NOTIFY(0x7010u)
      }

      enum class PlayerFunc(var code: UShort) {
            NONE(0x0000u),
            ORIGINAL_VOCALS(0x0001u),
            BACKING_VOCALS(0x0002u),
            GUIDE_VOCAL(0x0003u),
            PLAY(0x0004u),
            PAUSE(0x0005u),
            CUT(0x0006u),
            REPLAY(0x0007u),
            NEXT(0x0008u),
            DONE(0x0009u),
            MUTE(0x000Au),
            UN_MUTE(0x000Bu),
            FORCE_PAUSE(0xffffu),
            FORCE_PLAY(0xfffeu)
      }

      enum class SceneId(var code: Byte) {
            SWITCH(0x01),
            OPERA(0x02),
            ELECTRONICA(0x03),
            CHILL_OUT(0x04),
            GAME(0x05),
            KARAOKE(0x06),
            PARTY(0x07),
            MUTE(0x08)
      }

      companion object {
            private const val TAG = "DS_DATA"

            const val INVALID_VALUE: UShort = 0xFFFFu

            private const val PID: Byte = 'A'.code.toByte()

            private var localSeq: UShort = 0u

            private const val VER: Byte = 0

            fun resetLocalSeq() {
                  localSeq = 0u
            }

            fun decode(data: ByteArray): DSData? {
                  Log.d(TAG, "Start decode : ${data.size}")

                  Log.d(TAG, data.toHex())

                  val buffer: ByteBuffer = ByteBuffer.wrap(data)

                  val length: UShort = buffer.short.toUShort()

                  val cmdData: UShort = buffer.short.toUShort()

                  Log.d(TAG, "Length : $length, Cmd : $cmdData")


                  val cmd: CommandList = CommandList.values().find {
                        it.code == cmdData
                  } ?: return null

                  val seq: UShort = buffer.short.toUShort()

                  val pid: Byte = buffer.get()
                  val version: Byte = buffer.get()

                  val dsData: DSData = when (cmd) {
                        CommandList.REGISTER -> RegisterData()
                        CommandList.LIGHT_CTRL -> LightData()
                        CommandList.LED_BAR_CTRL -> LedBarData()
                        CommandList.LED_SCREEN_CTRL -> LedScreenData()
                        CommandList.VOICE_CTRL -> VoiceData()
                        CommandList.SCORE_CTRL -> ScoreData()
                        CommandList.AC_CTRL -> ACData()
                        CommandList.SERVICE_CTRL -> ServiceData()
                        CommandList.VOD_PLAY_CTRL -> PlayerData()
                        CommandList.VOD_TMS_CTRL -> TmsData()
                        CommandList.STATE_NOTIFY -> StateData()
                        CommandList.NEXT_SONG -> NextSongAckData()
                        CommandList.NEXT_SONG_ACK -> NextSongAckData()
                        CommandList.DEBUG_LOG -> LogData()
                        CommandList.ROOM_SWITCH -> RoomSwitchData()
                        CommandList.TRACKS_UPDATE -> PlayListData()
                        CommandList.INIT_NOTIFY -> InitData()
                        CommandList.ADD_SONG -> TrackReqData()
                        CommandList.INSERT_SONG -> TrackInsertData()
                        CommandList.MOVE_SONG -> TrackMoveData()
                        CommandList.DEL_SONG -> TrackMoveData()
                        CommandList.SCENE -> SceneData()
                        else -> {
                              Log.d(TAG, "Decode error")
                              return null
                        }
                  }
                  dsData.seq = seq
                  dsData.length = length
                  dsData.decodeBody(buffer)

                  dsData.mBuffer = buffer.deepClone(buffer)

                  Log.d(TAG, "Decode success : ${dsData.javaClass.simpleName}")
                  Log.d(TAG, "Content : $dsData")

                  return dsData
            }
      }

      var seq: UShort = 0x00u

      protected open var length: UShort = 8u

      abstract val command: CommandList

      private var mBuffer: ByteBuffer? = null

      private fun encodeHeader(buffer: ByteBuffer) {
            buffer.putShort(length.toShort())
            buffer.putShort(command.code.toShort())
            buffer.putShort(localSeq.toShort())
            buffer.put(PID)
            buffer.put(VER)
      }

      protected abstract fun encodeBody(buffer: ByteBuffer)

      fun encode(): ByteArray {
            mBuffer = ByteBuffer.allocate(length.toInt())

            encodeHeader(mBuffer!!)

            encodeBody(mBuffer!!)

            localSeq++

            mBuffer!!.flip()

            val data: ByteArray = ByteArray(mBuffer!!.remaining())

            mBuffer!!.get(data)

            return data
      }

      protected abstract fun decodeBody(body: ByteBuffer)

      override fun toString(): String {
            val data: ByteArray? = mBuffer?.array()

            return "${command.name} ($length) : ${data?.toHex()}"
      }

}

fun ByteBuffer.deepClone(buffer: ByteBuffer): ByteBuffer {
      val clone = ByteBuffer.allocate(buffer.capacity())
      buffer.rewind() //copy from the beginning

      clone.put(buffer)
      buffer.rewind()
      clone.flip()
      return clone
}

fun ByteArray.toHex(): String {
      val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
      val hexChars = CharArray(this.size * 3)
      try {
            for (j in this.indices) {
                  val v: Int = (this[j].toInt() and 0xFF)
                  hexChars[j * 3] = HEX_ARRAY[v ushr 4]
                  hexChars[j * 3 + 1] = HEX_ARRAY[v and 0x0F]
                  hexChars[j * 3 + 2] = ' '
            }

      } catch (e: Exception) {
            e.printStackTrace()
      }
      return String(hexChars)
}