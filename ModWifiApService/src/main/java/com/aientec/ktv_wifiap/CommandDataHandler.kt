package com.aientec.ktv_wifiap

import android.util.Log
import com.aientec.ktv_wifiap.commands.toHex
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.log

internal class CommandDataHandler(var callback: Callback? = null) : ChannelHandlerAdapter() {
    private companion object {
        const val TAG: String = "CDH"

        val hexArray: CharArray = "0123456789ABCDEF".toCharArray()

        fun printByteArray(data: ByteArray): String {
            val hexChars = CharArray(data.size * 3)
            for (j in data.indices) {
                val v = data[j].toInt() and 0xFF

                hexChars[j * 3] = hexArray[v ushr 4]
                hexChars[j * 3 + 1] = hexArray[v and 0x0F]
                hexChars[j * 3 + 2] = ' '
            }
            return String(hexChars)
        }
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



        GlobalScope.launch {
            callback?.onDataReceived(data)
        }

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



        Log.d("Trace", "Send data : ${printByteArray(data)}")
        super.write(ctx, msg, promise)
    }

}