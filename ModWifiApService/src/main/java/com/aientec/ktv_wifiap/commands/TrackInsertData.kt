package com.aientec.ktv_wifiap.commands

import com.aientec.structure.Track
import java.nio.ByteBuffer

class TrackInsertData(var track: Track? = null) : DSData() {
    override val command: CommandList
        get() = CommandList.INSERT_SONG


    override var length: UShort = 415u


    override fun encodeBody(buffer: ByteBuffer) {
        val mTrack: Track = track!!

        buffer.putInt(mTrack.id)
        buffer.putShort(mTrack.length.toShort())
        buffer.put(mTrack.bpm.toByte())


        val snBytes: ByteArray = ByteArray(16).apply {
            mTrack.sn.toByteArray().copyInto(this)
        }
        buffer.put(snBytes)

        val nameBytes: ByteArray = ByteArray(128).apply {
            mTrack.name.toByteArray().copyInto(this)
        }
        buffer.put(nameBytes)

        val singerBytes: ByteArray = ByteArray(128).apply {
            mTrack.performer.toByteArray().copyInto(this)
        }
        buffer.put(singerBytes)

        buffer.put(ByteArray(64).apply { fill(0) })

        val fileBytes: ByteArray = ByteArray(64).apply {
            mTrack.fileName.toByteArray().copyInto(this)
        }
        buffer.put(fileBytes)
    }

    override fun decodeBody(body: ByteBuffer) {
        track = Track().apply {
            id = body.int
            bpm = body.get().toInt()

            val snBytes: ByteArray = ByteArray(16)
            body.get(snBytes)
            sn = String(snBytes).replace(Char(0u).toString(), "")

            val nameBytes: ByteArray = ByteArray(128)
            body.get(nameBytes)
            name = String(nameBytes).replace(Char(0u).toString(), "")

            val singerBytes: ByteArray = ByteArray(128)
            body.get(singerBytes)
            performer = String(singerBytes).replace(Char(0u).toString(), "")

            val imageBytes: ByteArray = ByteArray(64)
            body.get(imageBytes)
            imageUrl = String(imageBytes).replace(Char(0u).toString(), "")

            val fileBytes: ByteArray = ByteArray(64)
            body.get(fileBytes)
            fileName = String(fileBytes).replace(Char(0u).toString(), "")

            state = Track.State.QUEUE
        }
    }
}