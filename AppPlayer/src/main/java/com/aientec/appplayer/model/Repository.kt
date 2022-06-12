package com.aientec.appplayer.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aientec.appplayer.BuildConfig
import com.aientec.appplayer.data.MessageBundle
import com.aientec.ktv_portal2.PortalResponse
import com.aientec.ktv_portal2.PortalService2
import com.aientec.ktv_wifiap.RoomWifiService
import com.aientec.ktv_wifiap.commands.*
import com.aientec.structure.Track
import com.aientec.structure.User
import com.linecorp.apng.ApngDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext


class Repository private constructor(context: Context) : ModelImpl(context), CoroutineScope {

      companion object {
            private var instance: Repository? = null

            fun getInstance(context: Context): Repository {
                  if (instance == null)
                        instance = Repository(context)
                  return instance!!
            }
      }

      interface EventListener {
            fun onNextTrack(track: Track?)
            fun onScoreMode(enable: Boolean)
      }

      interface AudioUpdateListener {
            fun onRecorderToggle(toggle: Boolean)
            fun onMicVolumeChanged(value: Int)
            fun onMusicVolumeChanged(value: Int)
            fun onEffectVolumeChanged(value: Int)
            fun onToneChanged(value: Int)
      }


      private val portalService: PortalService2 = PortalService2.getInstance(context)

      private val wifiService: RoomWifiService = RoomWifiService.getInstance(context)

      val playerFunc: MutableLiveData<DSData.PlayerFunc> = MutableLiveData(null)

      val osdMessage: MutableLiveData<MessageBundle> = MutableLiveData()

      val debugLog: MutableLiveData<String> = MutableLiveData()

      val scoreToggle: MutableLiveData<Boolean> = MutableLiveData(false)

      val connectionState: MutableLiveData<Boolean> = MutableLiveData()

      val openState: MutableLiveData<Boolean> = MutableLiveData()

      private val onlineMemberMap: HashMap<Int, User> = HashMap()

      private var playingList: ArrayList<Track>? = null

//    private val room: Room = Room(BuildConfig.ROOM_ID)

      private val remoteFile: String = "${BuildConfig.IMG_ROOT}%s"

      private val eventListerList: ArrayList<EventListener> = ArrayList()

      private val audioUpdateListenerList: ArrayList<AudioUpdateListener> = ArrayList()

      override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO

      private lateinit var userImgFilePath: File

      override fun init() {
            userImgFilePath = File(contextRef.get()!!.cacheDir, "user")

            if (!userImgFilePath.exists())
                  userImgFilePath.mkdirs()

            PortalService2.apiRoot = BuildConfig.PORTAL_SERVER

            portalService.init()

            onlineMemberMap.clear()

            val dsIp: String = BuildConfig.DS_SEVER.split(":")[0]

            val dsPort: Int = Integer.valueOf(BuildConfig.DS_SEVER.split(":")[1])

            wifiService.serverType = RoomWifiService.ServerType.EXTERNAL

            wifiService.ip = dsIp
            wifiService.port = dsPort

            wifiService.init()

            wifiService.addListener(this, listener)
      }


      override fun release() {
            wifiService.release()
      }

      fun addEventListener(listener: EventListener) {
            if (!eventListerList.contains(listener))
                  eventListerList.add(listener)
      }

      fun removeListener(listener: EventListener) {
            if (eventListerList.contains(listener))
                  eventListerList.remove(listener)
      }

      fun addAudioUpdateListener(listener: AudioUpdateListener) {
            if (!audioUpdateListenerList.contains(listener))
                  audioUpdateListenerList.add(listener)
      }

      fun removeAudioUpdateListener(listener: AudioUpdateListener) {
            if (audioUpdateListenerList.contains(listener))
                  audioUpdateListenerList.remove(listener)
      }


      suspend fun systemInitial(): Boolean = withContext(Dispatchers.IO) {
            return@withContext connectDataServer()
      }


      private suspend fun connectDataServer(): Boolean = withContext(Dispatchers.IO) {
            val isConnected = wifiService.connect()
            if (isConnected)
                  return@withContext wifiService.registerDeviceType(RegisterData.ClientType.PLAYER)
            return@withContext wifiService.connect()
      }

      private fun getUserById(id: Int): User? {
            return onlineMemberMap.values.find { it.id == id }
      }

      suspend fun updateIdleTracks(): List<Track>? = withContext(Dispatchers.IO) {
            val response: PortalResponse = portalService.Store.getAdsTrackList()

            return@withContext when (response) {
                  is PortalResponse.Success -> {
                        (response.data as ArrayList<*>).filterIsInstance(Track::class.java)
                  }
                  is PortalResponse.Fail -> null
            }
      }

      /*
      *1、原唱
      *2、伴唱
      *3、導唱
      *4、播放
      *5、暫停
      *6、切歌  （app发送切歌指令， dataserver转发给vod player， vodplayer实际切歌成功后，发送同样消息给dataserver， dataserver广播给各个app）
      *7、重唱
      *8、下一首(播放到下一首 vod player通知dataserver， dataserver广播给其他client)
      *9、点歌列表已播放完毕
      *
      * */
      suspend fun notifyPlayFn(code: Int) = withContext(Dispatchers.IO) {
            val func: DSData.PlayerFunc = DSData.PlayerFunc.values().find {
                  it.code == code.toUShort()
            } ?: DSData.PlayerFunc.NONE
            wifiService.playerControl(func)
      }

      suspend fun nextSongRequest() = withContext(Dispatchers.IO) {
            wifiService.nextSongRequest()
      }

      private fun downloadPicture(user: User) {
            val url: String = user.icon
            if (url.isEmpty()) return

            val file: File = File(userImgFilePath, "${user.id}.tmp")

            val connection: HttpURLConnection = (URL(url).openConnection()) as HttpURLConnection

            connection.connect()

            val inputStream: InputStream = connection.inputStream

            val outputStream: FileOutputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            inputStream.close()

            outputStream.close()

            file.renameTo(File(userImgFilePath, "${user.id}.jpg"))

      }

      private val listener: RoomWifiService.EventListener =
            RoomWifiService.EventListener { event, dsData ->
                  when (event) {
                        RoomWifiService.Event.ROOM_SWITCH -> {
                              openState.postValue((dsData as RoomSwitchData).switch)
                        }
                        RoomWifiService.Event.ON_CONNECTED -> {
                              connectionState.postValue(true)
                        }
                        RoomWifiService.Event.ON_DISCONNECTED -> {
                              connectionState.postValue(false)
                        }
                        RoomWifiService.Event.ON_RECONNECTED -> {
                              launch(coroutineContext) {
                                    wifiService.registerDeviceType(RegisterData.ClientType.PLAYER)
                              }
                        }
                        RoomWifiService.Event.SCORE_TOGGLE -> {
                              val data = dsData as ScoreData
                              eventListerList.forEach { it.onScoreMode(data.toggle == DSData.Toggle.ON) }
                        }
                        RoomWifiService.Event.DEBUG_LOG -> {
                              val data = dsData as LogData

                              debugLog.postValue(data.msg)
                        }
                        RoomWifiService.Event.NEXT_SONG -> {
                              val data = dsData as NextSongAckData

                              val track = if (data.songNumber.isNotEmpty()) {
                                    Track().apply {
                                          sn = data.songNumber
                                          name = data.name
                                          performer = data.singer
                                          fileName = data.fileName
                                    }
                              } else {
                                    null
                              }

                              Log.d("Repo", "Next song : ${track.toString()}")

                              eventListerList.forEach { it.onNextTrack(track) }
                        }
                        RoomWifiService.Event.VOD_PLAY_CTRL -> {
                              val data = dsData as PlayerData
                              playerFunc.postValue(data.func)
                        }
                        RoomWifiService.Event.MEMBER_JOIN -> {
                              val data = dsData as StateData

                              when (data.state) {
                                    StateData.State.NONE -> return@EventListener false
                                    StateData.State.OFFLINE -> {
                                          onlineMemberMap.values.removeIf {
                                                it.token == data.token
                                          }
                                    }
                                    StateData.State.ONLINE -> {
                                          val portalResponse: PortalResponse =
                                                portalService.Store.getUserInfo(
                                                      data.token ?: return@EventListener false
                                                )

                                          when (portalResponse) {
                                                is PortalResponse.Success -> {
                                                      val user: User = portalResponse.data as User
                                                      onlineMemberMap[user.id] = user
                                                      downloadPicture(user)
                                                }
                                          }
                                    }
                              }
                        }
                        RoomWifiService.Event.VOICE_CTRL -> {
                              val mData: VoiceData = dsData as VoiceData

                              when {
//                        mData.toggle != DSData.Toggle.NONE -> audioUpdateListenerList.forEach {
//                            it.onRecorderToggle(
//                                mData.toggle == DSData.Toggle.ON
//                            )
//                        }
                                    mData.micVolume != DSData.INVALID_VALUE -> audioUpdateListenerList.forEach {
                                          it.onMicVolumeChanged(
                                                mData.micVolume.toInt()
                                          )
                                    }
                                    mData.musicVolume != DSData.INVALID_VALUE -> audioUpdateListenerList.forEach {
                                          it.onMusicVolumeChanged(
                                                mData.musicVolume.toInt()
                                          )
                                    }
                                    mData.effect != DSData.INVALID_VALUE -> audioUpdateListenerList.forEach {
                                          it.onEffectVolumeChanged(
                                                mData.effect.toInt()
                                          )
                                    }
                                    mData.micMode != DSData.INVALID_VALUE -> audioUpdateListenerList.forEach {
                                          it.onToneChanged(
                                                mData.micMode.toInt()
                                          )
                                    }
                              }
                        }
                        RoomWifiService.Event.VOD_TMS_CTRL -> {
                              val mData = dsData as TmsData

                              val user: User? = getUserById(mData.id.toInt())

                              val messageBundle: MessageBundle = MessageBundle().apply {
                                    sender = user?.name ?: "未知"
                                    senderIcon = if (user != null) {
                                          val file: File = File(userImgFilePath, "${user!!.id}.jpg")
                                          if (file.exists())
                                                file.absolutePath
                                          else
                                                null
                                    } else {
                                          null
                                    }
                              }
                              when (mData.type) {
                                    TmsData.Type.NONE -> return@EventListener false
                                    TmsData.Type.TEXT -> {
                                          messageBundle.type = MessageBundle.Type.TXT
                                          messageBundle.data =
                                                String(mData.data ?: return@EventListener false)
                                    }
                                    TmsData.Type.PICTURE -> {
                                          try {
                                                messageBundle.type = MessageBundle.Type.IMAGE

                                                val fileName: String =
                                                      String(
                                                            mData.data ?: return@EventListener false
                                                      )

                                                val saveFile =
                                                      File(contextRef.get()!!.cacheDir, fileName)

                                                val url: String = String.format(
                                                      Locale.TAIWAN,
                                                      remoteFile,
                                                      fileName
                                                )

                                                val connection: HttpURLConnection =
                                                      URL(url).openConnection() as HttpURLConnection
                                                connection.doInput = true
                                                connection.connect()

                                                if (saveFile.parentFile?.exists() == false) {
                                                      saveFile.parentFile?.mkdirs()
                                                }


                                                val out: FileOutputStream =
                                                      FileOutputStream(saveFile)

                                                val input: InputStream = connection.inputStream

                                                input.copyTo(out)

                                                input.close()

                                                out.close()

                                                messageBundle.data = saveFile.absolutePath
                                          } catch (e: Exception) {
                                                messageBundle.type = MessageBundle.Type.TXT
                                                messageBundle.data =
                                                      "Picture error : ${e.message.toString()}"
                                          }
                                    }
                                    TmsData.Type.VIDEO -> {

                                          messageBundle.type = MessageBundle.Type.VIDEO
                                          messageBundle.data = String.format(
                                                Locale.TAIWAN,
                                                remoteFile,
                                                String(mData.data ?: return@EventListener false)
                                          )
                                    }
                                    TmsData.Type.EMOJI -> {
                                          val inputStream: InputStream =
                                                contextRef.get()!!.assets.open("anime/${String(mData.data!!)}")

                                          val apngDrawable: ApngDrawable =
                                                ApngDrawable.Companion.decode(inputStream)

                                          messageBundle.type = MessageBundle.Type.EMOJI
                                          messageBundle.data = apngDrawable
                                    }
                                    TmsData.Type.VOD -> {
                                          messageBundle.type = MessageBundle.Type.VOD
                                          messageBundle.data =
                                                String(mData.data ?: return@EventListener false)
                                    }
                              }
                              osdMessage.postValue(messageBundle)
                        }
                        else -> return@EventListener false
                  }
                  return@EventListener true
            }
}