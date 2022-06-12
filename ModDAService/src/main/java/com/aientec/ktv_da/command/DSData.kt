package com.aientec.ktv_da.command

import android.util.Log
import com.aientec.ktv_da.util.toHex
import java.nio.ByteBuffer

sealed class DSData {
    companion object {
        private const val TAG = "DS_DATA"

        internal const val DEFAULT_LENGTH: UShort = 8u

        const val INVALID_VALUE: UShort = 0xFFFFu

        private const val PID: Byte = 'A'.code.toByte()

        private var localSeq: UShort = 0u

        private const val VER: Byte = 0

        fun decode(data: ByteArray): DSData? {

            val buffer: ByteBuffer = ByteBuffer.wrap(data)

            val mLength: UShort = buffer.short.toUShort()

            val cmdData: UShort = buffer.short.toUShort()

            Log.d(TAG, "Length : $mLength, Cmd : $cmdData")

            val mSeq: UShort = buffer.short.toUShort()

            val pid: Byte = buffer.get()
            val version: Byte = buffer.get()

            return parseDSData(cmdData.toInt())?.apply {
                this.seq = mSeq
                this.length = mLength
                this.decodeBody(buffer)
            }
        }

        private fun parseDSData(cmd:Int) : DSData?{
            return when(cmd){
                1001 -> HeartBeat()

                else->null
            }
        }
    }

    var seq: UShort = 0x00u

    protected open var length: UShort = DEFAULT_LENGTH

    abstract val command: Short

    private fun encodeHeader(buffer: ByteBuffer) {
        buffer.putShort(length.toShort())
        buffer.putShort(command)
        buffer.putShort(localSeq.toShort())
        buffer.put(PID)
        buffer.put(VER)
    }

    protected abstract fun encodeBody(buffer: ByteBuffer)

    fun encode(): ByteArray {
        val mBuffer = ByteBuffer.allocate(length.toInt())

        encodeHeader(mBuffer)

        encodeBody(mBuffer)

        localSeq++

        mBuffer.flip()

        val data: ByteArray = ByteArray(mBuffer.remaining())

        mBuffer.get(data)

        return data
    }

    protected abstract fun decodeBody(body: ByteBuffer)

    override fun toString(): String {
        return this::class.java.simpleName
    }
}
