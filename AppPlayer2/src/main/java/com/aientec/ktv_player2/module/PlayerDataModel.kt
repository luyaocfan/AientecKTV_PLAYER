package com.aientec.ktv_player2.module

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_player2.BuildConfig
import com.aientec.ktv_portal2.PortalResponse
import com.aientec.ktv_portal2.PortalService2
import com.aientec.ktv_wifiap.RoomWifiService
import com.aientec.ktv_wifiap.commands.DSData
import com.aientec.ktv_wifiap.commands.NextSongAckData
import com.aientec.ktv_wifiap.commands.PlayerData
import com.aientec.ktv_wifiap.commands.RegisterData
import com.aientec.structure.Track
import idv.bruce.common.impl.ModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerDataModel(context: Context) : ModelImpl(context) {
    sealed class PlayerFunc(val code: Int) {

        class OriginalVocal : PlayerFunc(1)

        class BackingVocal : PlayerFunc(2)

        class GuideVocal : PlayerFunc(3)

        class Play : PlayerFunc(4)

        class Pause : PlayerFunc(5)

        class Cut : PlayerFunc(6)

        class Replay : PlayerFunc(7)

        class Next : PlayerFunc(8)

        class Complete : PlayerFunc(9)
    }

    override val tag: String = "PLAYER_MODEL"

    private val portalService2: PortalService2 = PortalService2.getInstance(context)

    private val wifiService: RoomWifiService = RoomWifiService.getInstance(context)

    val nextTrack: MutableLiveData<Track?> = MutableLiveData()

    val adsTrackList: MutableLiveData<List<Track>?> = MutableLiveData()

    val playerFunc: MutableLiveData<PlayerFunc?> = MutableLiveData()

    override fun init() {

        val dsIp: String = BuildConfig.DS_SEVER.split(":")[0]

        val dsPort: Int = Integer.valueOf(BuildConfig.DS_SEVER.split(":")[1])

        wifiService.serverType = RoomWifiService.ServerType.EXTERNAL
        wifiService.addListener(this, wifiEventListener)
        wifiService.ip = dsIp
        wifiService.port = dsPort
        wifiService.init()
    }

    override fun release() {

    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        return@withContext wifiService.connect()
    }

    suspend fun register(): Boolean = withContext(Dispatchers.IO) {
        return@withContext wifiService.registerDeviceType(RegisterData.ClientType.PLAYER)
    }

    suspend fun updateAdPlayList(): Boolean = withContext(Dispatchers.IO) {
        val portalResponse: PortalResponse = portalService2.Store.getAdsTrackList()

        return@withContext when (portalResponse) {
            is PortalResponse.Fail -> false
            is PortalResponse.Success -> {
                adsTrackList.postValue((portalResponse.data as List<*>).filterIsInstance(Track::class.java))
                true
            }
        }
    }

    suspend fun notifyPlayerFunc(func: PlayerFunc): Boolean = withContext(Dispatchers.IO) {
        val f: DSData.PlayerFunc = DSData.PlayerFunc.values().find {
            it.code == func.code.toUShort()
        } ?: DSData.PlayerFunc.NONE
        wifiService.playerControl(f)
    }


    private val wifiEventListener: RoomWifiService.EventListener =
        RoomWifiService.EventListener { event, d ->
            return@EventListener when (event) {
                RoomWifiService.Event.VOD_PLAY_CTRL -> {
                    val data = d as PlayerData
                    val func: PlayerFunc? = when (data.func) {
                        DSData.PlayerFunc.NONE -> null
                        DSData.PlayerFunc.ORIGINAL_VOCALS -> PlayerFunc.OriginalVocal()
                        DSData.PlayerFunc.BACKING_VOCALS -> PlayerFunc.BackingVocal()
                        DSData.PlayerFunc.GUIDE_VOCAL -> PlayerFunc.GuideVocal()
                        DSData.PlayerFunc.PLAY -> PlayerFunc.Play()
                        DSData.PlayerFunc.PAUSE -> PlayerFunc.Pause()
                        DSData.PlayerFunc.CUT -> PlayerFunc.Cut()
                        DSData.PlayerFunc.REPLAY -> PlayerFunc.Replay()
                        DSData.PlayerFunc.NEXT -> PlayerFunc.Next()
                        DSData.PlayerFunc.DONE -> PlayerFunc.Complete()
                        else -> {
                            return@EventListener false
                        }
                    }
                    playerFunc.postValue(func)
                    true
                }
                RoomWifiService.Event.NEXT_SONG -> {
                    val data = d as NextSongAckData
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
                    nextTrack.postValue(track)
                    true
                }
                else -> false
            }
        }
}