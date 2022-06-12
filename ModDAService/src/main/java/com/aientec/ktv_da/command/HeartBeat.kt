package com.aientec.ktv_da.command

import java.nio.ByteBuffer

class HeartBeat:DSData() {
    override val command: Short
        get() = 1001

    override fun encodeBody(buffer: ByteBuffer) {
    }

    override fun decodeBody(body: ByteBuffer) {
    }
}