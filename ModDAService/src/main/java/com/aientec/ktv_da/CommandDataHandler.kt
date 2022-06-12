package com.aientec.ktv_da

import android.util.Log
import com.aientec.ktv_da.util.toHex
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise

internal class CommandDataHandler(var callback: Callback? = null) : ChannelHandlerAdapter() {
    private companion object {
        const val TAG: String = "CDH"
    }

    fun interface Callback {
        fun onDataReceived(data: ByteArray)
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val buffer: ByteBuf = msg as ByteBuf
        val length: Int
        val data: ByteArray

        if (buffer.hasArray()) {
            val bytes: ByteArray = buffer.array()
            val offset: Int = buffer.arrayOffset()
            length = bytes.size - offset
            data = ByteArray(length)
            bytes.copyInto(data, 0, offset)
        } else {
            val index = buffer.readerIndex()
            length = buffer.readableBytes()
            data = ByteArray(length)
            buffer.getBytes(index, data)
        }

        callback?.onDataReceived(data)

        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        val buffer: ByteBuf = msg as ByteBuf
        val length: Int
        val data: ByteArray

        if (buffer.hasArray()) {
            val bytes: ByteArray = buffer.array()
            val offset: Int = buffer.arrayOffset()
            length = bytes.size - offset
            data = ByteArray(length)
            bytes.copyInto(data, 0, offset)
        } else {
            length = buffer.readableBytes()
            data = ByteArray(length)
            buffer.getBytes(buffer.readerIndex(), data)
        }



        Log.d("Trace", "Send data : ${data.toHex()}")
        super.write(ctx, msg, promise)
    }

}