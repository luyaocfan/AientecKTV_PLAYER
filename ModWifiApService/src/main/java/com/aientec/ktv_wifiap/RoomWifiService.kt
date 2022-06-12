package com.aientec.ktv_wifiap

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.IntRange
import com.aientec.ktv_wifiap.commands.*
import com.aientec.structure.Track
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.ConnectException
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RoomWifiService private constructor(context: Context) {

      companion object {
            private const val TAG = "DATA_SVR"

            private var instance: RoomWifiService? = null

            fun getInstance(context: Context): RoomWifiService {
                  if (instance == null)
                        instance = RoomWifiService(context)
                  return instance!!
            }
      }

      enum class ServerType {
            INTERNAL, EXTERNAL, ROUTER
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
            SCENE(0x700Fu),
            DISCONNECTED(0xFFFFu),
            ROOM_SWITCH(0x3003u),
            TRACKS_UPDATE(0x3001u),
            INIT_NOTIFY(0x7010u),
            ADD_SONG(0x7012u),
            INSERT_SONG(0x7013u),
            MOVE_SONG(0x7014u),
            DEL_SONG(0x7015u),
            ROOM_EFFECT(0x7017u),

            //==================================//
            ON_CONNECTED(0x9000u),
            ON_DISCONNECTED(0x9001u),
            ON_RECONNECTED(0x9002u)
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

      var serverType: ServerType = ServerType.EXTERNAL

      var maxRetryCount: Int = 3

      private var mRetryCount: Int = 0

      private lateinit var bootstrap: Bootstrap

      fun init() {
            mRetryCount = 0

            bootstrap = Bootstrap().apply {
                  group(NioEventLoopGroup(8))
                  channel(NioSocketChannel::class.java)
                  option(ChannelOption.TCP_NODELAY, true)
                  option(ChannelOption.SO_KEEPALIVE, true)
                  option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                  handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel?) {
                              Log.d(TAG, "Init channel")
                              if (ch == null) return
                              socketChannel = ch
                              socketChannel!!.pipeline().apply {
                                    addLast(LengthFieldBasedFrameDecoder(65535, 0, 2, -2, 0))
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
                  findServer()
            }
      }

      fun release() {}

      fun addListener(owner: Any, listener: EventListener) {
            listenerMap[owner] = listener
      }


      fun connect(): Boolean {
            if (ip == null)
                  return false

            Log.d(TAG, "Connect to $ip:$port")

            var future: ChannelFuture? = null

            try {
                  future =
                        bootstrap.connect(ip!!, port).addListener(object : ChannelFutureListener {
                              override fun operationComplete(channelFuture: ChannelFuture?) {
                                    if (channelFuture?.isSuccess == true) {
                                          channel = channelFuture.channel()
                                          Log.d(TAG, "Channel : $channel")
                                          future?.removeListener(this)
                                          listenerMap.values.forEach {
                                                it.onEvent(
                                                      Event.ON_CONNECTED,
                                                      null
                                                )
                                          }
                                    }
                              }
                        }).sync()

                  Log.d(TAG, "Connected : ${future.isSuccess}")



                  if (future!!.isSuccess) {
                        future.channel()!!.closeFuture().addListener(
                              object : ChannelFutureListener {
                                    override fun operationComplete(future: ChannelFuture?) {
                                          listenerMap.values.forEach {
                                                it.onEvent(
                                                      Event.ON_DISCONNECTED,
                                                      null
                                                )
                                          }
                                          if (connect())
                                                listenerMap.values.forEach {
                                                      it.onEvent(
                                                            Event.ON_RECONNECTED,
                                                            null
                                                      )
                                                }
                                          future?.removeListener(this)
                                    }
                              }
                        )

                        return true
                  } else
                        return false
            } catch (e: Exception) {
                  Log.e(TAG, e.message)
                  Thread.sleep(5000)
                  return connect()
            }
      }

      fun registerDeviceType(type: RegisterData.ClientType): Boolean = sendData(RegisterData(type))

      fun playerControl(func: DSData.PlayerFunc): Boolean {
            return sendData(PlayerData(func))
      }

      fun scoreToggle(toggle: Boolean): Boolean {
            val data: ScoreData = ScoreData()
            data.toggle = if (toggle) DSData.Toggle.ON else DSData.Toggle.OFF
            return sendData(data)
      }

      fun nextSongRequest(): Boolean {
            return sendData(NextSongData())
      }

      fun ledBarControl(
            basicLightId: Int?,
            subModeId: Int?,
            mainModeId: Int?
      ): Boolean {
            return sendData(
                  LedBarData(
                        basicLightId?.toUShort() ?: DSData.INVALID_VALUE,
                        subModeId?.toUShort() ?: DSData.INVALID_VALUE,
                        mainModeId?.toUShort() ?: DSData.INVALID_VALUE
                  )
            )
      }

      fun roomEffect(code: Int, type: Int): Boolean {
            return sendData(RoomEffectData(code.toUShort(), type.toUShort()))
      }

      fun micRecordToggle(toggle: Boolean): Boolean {
            return sendData(VoiceData(if (toggle) DSData.Toggle.ON else DSData.Toggle.OFF))
      }

      fun micVolumeControl(value: Int): Boolean {
            return sendData(VoiceData(DSData.Toggle.NONE, value.toUShort()))
      }

      fun micEffectControl(value: Int): Boolean {
            return sendData(
                  VoiceData(
                        DSData.Toggle.NONE,
                        DSData.INVALID_VALUE,
                        DSData.INVALID_VALUE,
                        value.toUShort()
                  )
            )
      }

      fun micModeControl(value: Int): Boolean {
            return sendData(
                  VoiceData(
                        DSData.Toggle.NONE,
                        DSData.INVALID_VALUE,
                        DSData.INVALID_VALUE,
                        DSData.INVALID_VALUE,
                        if (value == -1) DSData.INVALID_VALUE else value.toUShort()
                  )
            )
      }

      fun musicVolumeControl(value: Int): Boolean {
            return sendData(
                  VoiceData(
                        DSData.Toggle.NONE,
                        DSData.INVALID_VALUE,
                        value.toUShort(),
                        DSData.INVALID_VALUE,
                        DSData.INVALID_VALUE
                  )
            )
      }


      fun vodMessage(content: String): Boolean {
            return sendData(TmsData(0u, TmsData.Type.VOD).apply {
                  data = content.toByteArray()
                  dataLength = data!!.size.toUShort()
            })
      }

      fun addTrack(track: Track): Boolean {
            return sendData(TrackReqData(track))
      }

      fun insertTrack(track: Track): Boolean {
            return sendData(TrackInsertData(track))
      }

      fun delTrack(seq: Int, id: Int): Boolean {
            return sendData(TrackDelData(seq.toUShort(), id.toUInt()))
      }

      fun moveTrack(oldSeq: Int, newSeq: Int, id: Int, targetId: Int): Boolean {
            return sendData(
                  TrackMoveData(
                        oldSeq.toUShort(),
                        newSeq.toUShort(),
                        id.toUInt(),
                        targetId.toUInt()
                  )
            )
      }

      fun setScene(id: DSData.SceneId): Boolean {
            return sendData(SceneData(id))
      }


      fun findServer(): String? {
            return when (serverType) {
                  ServerType.INTERNAL -> {
                        if (startInternalDataServer())
                              findInternalServer()
                        else
                              null
                  }
                  ServerType.EXTERNAL -> findExternalServer()
                  ServerType.ROUTER -> findRouterServer()
            }
      }

      private fun startInternalDataServer(): Boolean {
            //Check data server is start
            return true
      }

      private fun findExternalServer(): String? {
            return String.format(Locale.TAIWAN, "%s:%d", ip, port)
      }

      private fun findRouterServer(): String? {
            if (ip != null) return String.format(Locale.TAIWAN, "%s:%d", ip, port)

            val cm: ConnectivityManager =
                  contextRef.get()!!
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network: Network = cm.activeNetwork ?: return null

            val networkCapabilities: NetworkCapabilities = cm.getNetworkCapabilities(network)

            if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return null

            val lp: LinkProperties = cm.getLinkProperties(network)

            for (r in lp.routes)
                  if (r.isDefaultRoute) ip = r.gateway.hostAddress


            return String.format(Locale.TAIWAN, "%s:%d", ip, port)
      }


      private fun findInternalServer(): String? {
            if (ip != null) return String.format(Locale.TAIWAN, "%s:%d", ip, port)

            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                  val intf: NetworkInterface = en.nextElement()
                  val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                  while (enumIpAddr.hasMoreElements()) {
                        val inetAddress: InetAddress = enumIpAddr.nextElement()
                        Log.d(TAG, "List : $inetAddress")
                        if (!inetAddress.isLoopbackAddress) {
                              ip = inetAddress.hostAddress.toString()
                              break
                        }
                  }
            }

            Log.d(TAG, "Address : $ip")

            return String.format(Locale.TAIWAN, "%s:%d", ip, port)
      }

      suspend fun updateWifiInformation(ssid: String, password: String): Boolean =
            suspendCoroutine {
                  ///cgi-bin/setwifi

                  val chl = channel ?: it.resume(false)

                  val body: JSONObject = JSONObject().apply {
                        put("ssid", ssid)
                        put("password", password)
                  }

                  it.resume(true)
            }

      private fun sendData(data: DSData): Boolean {
            return try {
                  val f =
                        channel?.writeAndFlush(Unpooled.copiedBuffer(data.encode()))
                              ?.syncUninterruptibly()

                  f?.isSuccess ?: false
            } catch (e: Exception) {
                  Log.e(TAG, e.message.toString())

                  false
            }
      }

      private fun receiveData(bytes: ByteArray) {
            val dsData: DSData = DSData.decode(bytes) ?: return

            val event: Event = Event.values().find {
                  it.code == dsData.command.code
            } ?: Event.NONE

            for (listener in listenerMap.values)
                  if (listener.onEvent(event, dsData)) break
      }

}