package com.aientec.ktv_wifiap.commands

import android.util.Log
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

class SocketStickyHandler : LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 2) {

}