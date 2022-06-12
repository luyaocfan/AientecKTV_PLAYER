package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class NextSongData : DSData() {
    override val command: CommandList
        get() = CommandList.NEXT_SONG

    override var length: UShort = 8u

    override fun encodeBody(buffer: ByteBuffer) {

    }

    override fun decodeBody(body: ByteBuffer) {


    }


}