package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class NextSongAckData : DSData() {
    override val command: CommandList
        get() = CommandList.NEXT_SONG_ACK

    var songNumber: String
        get() = String(mSongNumber, Charsets.UTF_8).replace(Char(0u).toString(), "")
        set(value) {
            value.toByteArray(Charsets.UTF_8).copyInto(mSongNumber, 0, 0)
        }

    var name: String
        get() = String(mName, Charsets.UTF_8).replace(Char(0u).toString(), "")
        set(value) {
            value.toByteArray(Charsets.UTF_8).copyInto(mName, 0, 0)
        }

    var singer: String
        get() = String(mSinger, Charsets.UTF_8).replace(Char(0u).toString(), "")
        set(value) {
            value.toByteArray(Charsets.UTF_8).copyInto(mSinger, 0, 0)
        }

    var fileName: String
        get() = String(mFileName, Charsets.UTF_8).replace(Char(0u).toString(), "")
        set(value) {
            value.toByteArray(Charsets.UTF_8).copyInto(mFileName, 0, 0)
        }

    private var mSongNumber: ByteArray = ByteArray(16)

    private var mName: ByteArray = ByteArray(128)

    private var mSinger: ByteArray = ByteArray(128)

    private var mFileName: ByteArray = ByteArray(64)

    override var length: UShort = 8u

    override fun encodeBody(buffer: ByteBuffer) {

    }

    override fun decodeBody(body: ByteBuffer) {

        body.get(mSongNumber)

        body.get(mName)

        body.get(mSinger)

        body.get(mFileName)
    }

    override fun toString(): String {
        return "NextSongAckData(songNumber='$songNumber', name='$name', singer='$singer', fileName='$fileName')"
    }


}