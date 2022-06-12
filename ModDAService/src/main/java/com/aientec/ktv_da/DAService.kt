package com.aientec.ktv_da

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.aientec.ktv_da.command.DSData
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DAService private constructor(context: Context) {

    companion object {
        private const val TAG = "DATA_SVR"

        private var instance: DAService? = null

        fun getInstance(context: Context): DAService {
            if (instance == null)
                instance = DAService(context)
            return instance!!
        }
    }

    enum class Event(var code: UShort) {
        NONE(0x0000u),
        REGISTER(0x700Bu),
        LIGHT_CTRL(0x7001u),
        LED_BAR_CTRL(0x7002u),
        LED_SCREEN_CTRL(0x7003u),
        VOICE_CTRL(0x7004u),
        SCORE_TOGGLE(0x7005u),
        AIR_CTRL(0x7006u),
        SERVICE_NOTIFY(0x7007u),
        VOD_PLAY_CTRL(0x7008u),
        VOD_TMS_CTRL(0x7009u),
        VOD_CONNECT_CTRL(0x700Au),
        MEMBER_JOIN(0x700Cu),
        NEXT_SONG(0x3016u),
        DEBUG_LOG(0x700Eu),
        DISCONNECTED(0xFFFFu),
        ROOM_SWITCH(0x3003u)
    }


    fun interface EventListener {
        fun onEvent(event: Event, data: Any?): Boolean
    }

    private val listenerMap: HashMap<Any, EventListener> = HashMap()

    private val contextRef: WeakReference<Context> = WeakReference(context)

    var port: Int = 20005

    var ip: String? = "106.104.151.145"

    private var channel: Channel? = null

    private var socketChannel: SocketChannel? = null

    private lateinit var bootstrap: Bootstrap

    fun init() {
        bootstrap = Bootstrap().apply {
            group(NioEventLoopGroup(8))
            channel(NioSocketChannel::class.java)
            option(ChannelOption.TCP_NODELAY, true)
            option(ChannelOption.SO_KEEPALIVE, true)
            handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel?) {
                    Log.d(TAG, "Init channel")
                    if (ch == null) return
                    socketChannel = ch
                    socketChannel!!.pipeline().apply {
                        addLast(LengthFieldBasedFrameDecoder(2048, 0, 2, -2, 0))
                        addLast(CommandDataHandler {
                            receiveData(it)
                        })
                    }

                    Log.d(TAG, "Socket channel : $socketChannel")
                }

                override fun channelInactive(ctx: ChannelHandlerContext?) {
                    super.channelInactive(ctx)
                    Log.d(TAG, "Socket disconnect : ")
                    listenerMap.values.forEach {
                        if (it.onEvent(Event.DISCONNECTED, null))
                            return@forEach
                    }
                }

            })
        }
    }

    fun release() {}

    fun addListener(owner: Any, listener: EventListener) {
        listenerMap[owner] = listener
    }

    suspend fun connect(): Boolean = suspendCoroutine {
        if (ip == null) {
            it.resume(false)
            return@suspendCoroutine
        }

        Log.d(TAG, "Connect to $ip:$port")

        val future: ChannelFuture = bootstrap.connect(ip!!, port)

        future.addListener(object : ChannelFutureListener {
            override fun operationComplete(channelFuture: ChannelFuture?) {
                Log.d(TAG, "operationComplete : ${channelFuture?.isSuccess}")
                if (channelFuture?.isSuccess == true) {
                    channel = channelFuture.channel()
                    Log.d(TAG, "Channel : $channel")
                    channelFuture.removeListener(this)
                    it.resume(true)
                } else {
                    channelFuture?.removeListener(this)
                    it.resume(false)
                }
            }
        })
    }


    suspend fun updateWifiInformation(ssid: String, password: String): Boolean = suspendCoroutine {

        val chl = channel ?: it.resume(false)

        val body: JSONObject = JSONObject().apply {
            put("ssid", ssid)
            put("password", password)
        }
//        val future: ChannelFuture =
//            channel?.writeAndFlush(request)?.syncUninterruptibly()!!
//
        it.resume(true)
    }

    private fun sendData(data: DSData): Boolean {

        val f = channel?.writeAndFlush(Unpooled.copiedBuffer(data.encode()))?.syncUninterruptibly()

        return f?.isSuccess ?: false
    }

    private fun receiveData(bytes: ByteArray) {
        val dsData: DSData = DSData.decode(bytes) ?: return

//        val event: Event = Event.values().find {
//            it.code == dsData.command.code
//        } ?: Event.NONE
//
//        for (listener in listenerMap.values)
//            if (listener.onEvent(event, dsData)) break
    }

}