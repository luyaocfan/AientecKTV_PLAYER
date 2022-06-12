package com.aientec.ktv_wifiap.commands

import com.aientec.ktv_wifiap.RoomWifiService
import java.nio.ByteBuffer

/**
 * TODO
 *
 * @property func
 * 1、原唱
 * 2、伴唱
 * 3、導唱
 * 4、播放
 * 5、暫停
 * 6、切歌
 * 7、重唱
 * 8、更新點歌列表
 */
class PlayerData(var func: PlayerFunc = PlayerFunc.NONE) : DSData() {

    override var length: UShort = 10u

    override val command: CommandList
        get() = CommandList.VOD_PLAY_CTRL

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.putShort(func.code.toShort())
    }

    override fun decodeBody(body: ByteBuffer) {
        val f = body.short.toUShort()

        func = PlayerFunc.values().find {
             it.code == f
        } ?: PlayerFunc.NONE
    }
}