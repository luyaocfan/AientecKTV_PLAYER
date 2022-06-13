package com.aientec.ktv_vod.module

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.common.impl.ModuleImpl
import com.aientec.ktv_wifiap.RoomWifiService
import com.aientec.structure.Track
import com.aientec.structure.User
import com.aientec.ktv_vod.structure.EnvironmentalStatus
import com.aientec.ktv_wifiap.commands.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Environmental(context: Context) : ModuleImpl(context), CoroutineScope {
      enum class VocalType {
            ORIGINAL, BACKING, GUIDE
      }

      private val wifiService: RoomWifiService = RoomWifiService.getInstance(context)

      val vocalType: MutableLiveData<VocalType> = MutableLiveData(VocalType.BACKING)

      val playState: MutableLiveData<Boolean> = MutableLiveData(true)

      val scoreToggleState: MutableLiveData<Boolean> = MutableLiveData(false)

      val connectedState: MutableLiveData<Boolean> = MutableLiveData(false)

      val player: Player = Player()

      var sourceType: Int = 1

      private var mMainModeId: Int = -1
            set(value) {
                  field = value
                  mainModeId.postValue(field)
            }

      val mainModeId: MutableLiveData<Int> = MutableLiveData(mMainModeId)

      private var mSubModeId: Int = -1
            set(value) {
                  field = value
                  subModeId.postValue(field)
            }

      val subModeId: MutableLiveData<Int> = MutableLiveData(mSubModeId)

      private var mBasicLightId: Int = -1
            set(value) {
                  field = value
                  basicLightId.postValue(field)
            }

      val basicLightId: MutableLiveData<Int> = MutableLiveData(mBasicLightId)

      private var mMicVolume: Int = 0
            set(value) {
                  field = value
                  micVolume.postValue(field)
            }

      val micVolume: MutableLiveData<Int> = MutableLiveData(mMicVolume)

      private var mMusicVolume: Int = 0
            set(value) {
                  field = value
                  musicVolume.postValue(field)
            }

      val musicVolume: MutableLiveData<Int> = MutableLiveData(mMusicVolume)

      private var mMicEffectVolume: Int = 0
            set(value) {
                  field = value
                  micEffectVolume.postValue(field)
            }

      val micEffectVolume: MutableLiveData<Int> = MutableLiveData(mMicEffectVolume)

      private var mMicEffectMode: Int = 0
            set(value) {
                  field = value
                  micEffectMode.postValue(field)
            }

      val micToneVolume: MutableLiveData<Int> = MutableLiveData()

      private var mMicToneVolume: Int = 0
            set(value) {
                  field = value
                  micToneVolume.postValue(field)
            }

      val micEffectMode: MutableLiveData<Int> = MutableLiveData(mMicEffectMode)

      override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO

      override fun init() {
            wifiService.init()

            wifiService.addListener(this, eventListener)
      }

      override fun release() {

      }

      fun setAddress(ip: String, port: Int) {

            if (BuildConfig.DEBUG) {
                  wifiService.ip = ip
                  wifiService.port = port
            } else {
                  wifiService.ip = ip
                  wifiService.port = port
            }
      }


      suspend fun findDataServer(): Boolean = withContext(coroutineContext) {
            return@withContext wifiService.findServer() != null
      }

      suspend fun connectDataServer(): Boolean = withContext(Dispatchers.IO) {
            return@withContext wifiService.connect()
      }

      suspend fun register(): Boolean = withContext(Dispatchers.IO) {
            return@withContext wifiService.registerDeviceType(RegisterData.ClientType.VOD)
      }

      suspend fun scoreToggle(): Boolean = withContext(Dispatchers.IO) {
            val toggle: Boolean = !scoreToggleState.value!!

            val result: Boolean = wifiService.scoreToggle(toggle)

            if (result)
                  scoreToggleState.postValue(toggle)

            return@withContext result
      }

      suspend fun setMainMode(id: Int): Boolean = withContext(Dispatchers.IO) {
            mMainModeId = id
            return@withContext wifiService.ledBarControl(null, null, mMainModeId)
      }

      suspend fun setSubMode(id: Int): Boolean = withContext(Dispatchers.IO) {
            mSubModeId = id
            return@withContext wifiService.ledBarControl(null, mSubModeId, null)
      }

      suspend fun resetMode(): Boolean = withContext(Dispatchers.IO) {
            mSubModeId = -1
            return@withContext wifiService.ledBarControl(null, null, mMainModeId)
      }

      suspend fun setBasicLight(id: Int): Boolean = withContext(Dispatchers.IO) {
            if (mBasicLightId == id) return@withContext true
            mBasicLightId = id
            return@withContext wifiService.ledBarControl(mBasicLightId, null, null)
      }

      suspend fun sendRoomEffect(id: Int, type: Int): Boolean = withContext(Dispatchers.IO) {
            return@withContext wifiService.roomEffect(id, type)
      }


      suspend fun sendVodMessage(msg: String): Boolean = withContext(Dispatchers.IO) {
            return@withContext wifiService.vodMessage(msg)
      }

      inner class Player internal constructor() {
            private var mute: Boolean = false

            suspend fun cut(): Boolean = withContext(Dispatchers.IO) {
                  return@withContext wifiService.playerControl(DSData.PlayerFunc.CUT)
            }

            suspend fun playToggle(): Boolean = withContext(Dispatchers.IO) {
                  return@withContext if (playState.value!!) {
                        playState.postValue(false)
                        wifiService.playerControl(DSData.PlayerFunc.PAUSE)
                  } else {
                        playState.postValue(true)
                        wifiService.playerControl(DSData.PlayerFunc.PLAY)
                  }
            }

            suspend fun replay(): Boolean = withContext(Dispatchers.IO) {
                  return@withContext wifiService.playerControl(DSData.PlayerFunc.REPLAY)
            }

            suspend fun muteToggle(): Boolean = withContext(Dispatchers.IO) {
                  return@withContext (if (mute)
                        wifiService.playerControl(DSData.PlayerFunc.UN_MUTE)
                  else
                        wifiService.playerControl(DSData.PlayerFunc.MUTE)).also { mute = !mute }
            }

            suspend fun vocalSwitch(): Boolean = withContext(Dispatchers.IO) {
                  return@withContext when (vocalType.value) {
                        VocalType.ORIGINAL -> {
                              vocalType.postValue(VocalType.GUIDE)
                              wifiService.playerControl(DSData.PlayerFunc.GUIDE_VOCAL)
                        }
                        VocalType.BACKING -> {
                              vocalType.postValue(VocalType.ORIGINAL)
                              wifiService.playerControl(DSData.PlayerFunc.ORIGINAL_VOCALS)
                        }
                        VocalType.GUIDE -> {
                              vocalType.postValue(VocalType.BACKING)
                              wifiService.playerControl(DSData.PlayerFunc.BACKING_VOCALS)
                        }
                        null -> true
                  }
            }


            suspend fun micAddVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicVolume >= 29) return@withContext true
                  mMicVolume++
                  return@withContext wifiService.micVolumeControl(mMicVolume)
            }

            suspend fun micDescVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicVolume <= 0) return@withContext true
                  mMicVolume--
                  return@withContext wifiService.micVolumeControl(mMicVolume)
            }

            suspend fun micEffectAddVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicEffectVolume >= 29) return@withContext true
                  mMicEffectVolume++
                  return@withContext wifiService.micEffectControl(mMicEffectVolume)
            }

            suspend fun micEffectDescVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicEffectVolume <= 0) return@withContext true
                  mMicEffectVolume--
                  return@withContext wifiService.micEffectControl(mMicEffectVolume)
            }

            suspend fun setMicMode(mode: Int): Boolean = withContext(Dispatchers.IO) {
                  if (mMicEffectMode == mode) return@withContext true
                  mMicEffectMode = mode
                  return@withContext wifiService.micModeControl(mMicEffectMode)
            }

            suspend fun musicAddVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMusicVolume >= 29) return@withContext true
                  mMusicVolume++
                  return@withContext wifiService.musicVolumeControl(mMusicVolume)
            }

            suspend fun musicDescVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMusicVolume <= 0) return@withContext true
                  mMusicVolume--
                  return@withContext wifiService.musicVolumeControl(mMusicVolume)
            }

            suspend fun micModulateAddVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicToneVolume >= 14) return@withContext true

                  mMicToneVolume++

                  return@withContext wifiService.micModulateControl(mMicToneVolume)
            }

            suspend fun micModulateDescVolume(): Boolean = withContext(Dispatchers.IO) {
                  if (mMicToneVolume <= 0) return@withContext true

                  mMicToneVolume--

                  return@withContext wifiService.micModulateControl(mMicToneVolume)
            }
      }

      private val eventListener: RoomWifiService.EventListener =
            RoomWifiService.EventListener { event, data ->
                  when (event) {
                        RoomWifiService.Event.ON_CONNECTED -> {
                              connectedState.postValue(true)
                        }
                        RoomWifiService.Event.ON_DISCONNECTED -> {
                              connectedState.postValue(false)
                        }
                        RoomWifiService.Event.ON_RECONNECTED -> {
                              launch {
                                    register()
                              }
                        }
                        RoomWifiService.Event.INIT_NOTIFY -> {
                              val initData = data as InitData

                              mBasicLightId = initData.basicLightMode.toShort().toInt()

                              mMainModeId = initData.mainModeCode.toShort().toInt()

                              mSubModeId = initData.subModeCode.toShort().toInt()

                              mMicVolume = initData.micVolume.toShort().toInt()

                              mMusicVolume = initData.musicVolume.toShort().toInt()

                              mMicEffectVolume = initData.effectVolume.toShort().toInt()

                              mMicEffectMode = initData.micMode.toInt()

                              mMicToneVolume = initData.modulate.toInt()

                              sourceType = initData.audioSrc.toInt()
                        }
                        RoomWifiService.Event.VOD_PLAY_CTRL -> {
                              val d = data as PlayerData
                              when (d.func) {
                                    DSData.PlayerFunc.ORIGINAL_VOCALS -> {
                                          vocalType.postValue(VocalType.ORIGINAL)
                                    }
                                    DSData.PlayerFunc.GUIDE_VOCAL -> {
                                          vocalType.postValue(VocalType.GUIDE)
                                    }
                                    DSData.PlayerFunc.BACKING_VOCALS -> {
                                          vocalType.postValue(VocalType.BACKING)
                                    }
                                    DSData.PlayerFunc.PLAY -> {
                                          playState.postValue(true)
                                    }
                                    DSData.PlayerFunc.PAUSE -> {
                                          playState.postValue(false)
                                    }
                              }
                              return@EventListener false
                        }

                        else -> return@EventListener false
                  }
                  return@EventListener false
            }
}