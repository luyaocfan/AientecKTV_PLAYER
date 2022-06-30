package com.aientec.player2.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aientec.player2.data.MessageBundle
import com.aientec.player2.data.PlayerControl
import com.aientec.player2.model.PlayerModel
import com.aientec.structure.Track
import kotlinx.coroutines.launch
import java.util.*

class PlayerViewModel : ViewModel() {
    companion object {
        const val PLAYER_STATE_NONE = -1

        const val PLAYER_STATE_RESUME = 0

        const val PLAYER_STATE_PAUSE = 1

        const val PLAYER_STATE_CUT = 2

        const val PLAYER_STATE_REPLAY = 3

        private const val TAG = "PlayerViewModel"

        private const val STATE_ICON_DISPLAY_TIME = 1000L

        private const val NOTIFY_DISPLAY_TIME = 1000L
    }

    private lateinit var model: PlayerModel

    val dataSyn: MutableLiveData<Boolean> = MutableLiveData(false)

    val idleMTVList: MutableLiveData<List<Track>?> = MutableLiveData()

    private var mIsIdle: Boolean = true
        set(value) {
            field = value
            isIdle.postValue(field)
        }

    val isIdle: MutableLiveData<Boolean> = MutableLiveData()

    val notifyMessage: MutableLiveData<String?> = MutableLiveData()

    val osdMessage: MutableLiveData<MessageBundle?> = MutableLiveData()

    val isConnected: MutableLiveData<Boolean> = MutableLiveData()

    val isRoomOpened: MutableLiveData<Boolean> = MutableLiveData()

    private var mPlayerState: Int = PLAYER_STATE_NONE
        set(value) {
            field = value
            playerState.postValue(field)
        }
    val playerState: MutableLiveData<Int> = MutableLiveData()

    val isMute: MutableLiveData<Boolean> = MutableLiveData()

    val ratingState: MutableLiveData<Int> = MutableLiveData()

    private var mNextTrack: Track? = null
        set(value) {
            field = value
            nextTrack.postValue(field)
        }

    val nextTrack: MutableLiveData<Track?> = MutableLiveData()

    val playerControl: MutableLiveData<PlayerControl?> = MutableLiveData(null)

    private var stateTimer: Timer? = null

    private var notifyTimer: Timer? = null

    private var ratingTimer: Timer? = null

    fun systemInit(context: Context) {
        viewModelScope.launch {
            model = PlayerModel.getInstance(context)

            model.playerControlListener = playerControlListener

            model.roomStateChangeListener = roomStateChangeListener

            model.osdListener = osdListener

            model.addAudioUpdateListener(audioUpdateListener)

            val res: Boolean = model.systemInitial()

            idleMTVList.postValue(model.updateIdleTracks())

            dataSyn.postValue(res)

        }
    }

    fun nextTrackRequest() {
        viewModelScope.launch {
            Log.d(TAG, "OnNexReq")
            model.nextSongRequest()
        }
    }

    fun onPlayerStart() {
        viewModelScope.launch {
            mNextTrack = null
            model.notifyPlayFn(8)
            model.nextSongRequest()
            mIsIdle = false
        }
    }

    fun onPlayerEnd() {
        viewModelScope.launch {
            if (mNextTrack == null)
                model.notifyPlayFn(9)
            mIsIdle = true
        }
    }

    fun onPlayerResume() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_RESUME, false)
            model.notifyPlayFn(4)
        }
    }

    fun onPlayerPause() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_PAUSE, true)
            model.notifyPlayFn(5)
        }
    }

    fun onPlayerCut() {
        updateState(PLAYER_STATE_CUT, false)
    }

    fun onPlayerReplay() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_REPLAY, false)
            model.notifyPlayFn(7)
        }

    }

    fun onPlayerMuteToggle(mute: Boolean) {
        viewModelScope.launch {
            isMute.postValue(mute)
            model.notifyPlayFn(if (mute) 10 else 11)
        }
    }

    fun onPlayerVocalChanged(type: Int) {
        viewModelScope.launch {
            when (type) {
                1 -> updateNotifyMessage("原唱")
                2 -> updateNotifyMessage("伴唱")
                3 -> updateNotifyMessage("導唱")
            }
            model.notifyPlayFn(type)
        }
    }

    fun onPlayerRatingToggle(enable: Boolean) {
        if (enable) {
            ratingState.postValue(0)
            ratingTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        ratingState.postValue(1)
                    }
                }, 3000)
            }
        } else {
            ratingTimer?.cancel()
            ratingState.postValue(-1)
        }
    }

    private fun updateState(state: Int, keep: Boolean) {

        if (!mIsIdle) {
            mPlayerState = state

            stateTimer?.cancel()

            if (!keep) {
                stateTimer = Timer().apply {
                    schedule(object : TimerTask() {
                        override fun run() {
                            playerState.postValue(PLAYER_STATE_NONE)
                        }
                    }, STATE_ICON_DISPLAY_TIME)
                }
            }
        }
    }

    private fun updateNotifyMessage(msg: String) {
        notifyMessage.postValue(msg)

        notifyTimer?.cancel()

        notifyTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    notifyMessage.postValue(null)
                }
            }, NOTIFY_DISPLAY_TIME)
        }
    }

    private val audioUpdateListener: PlayerModel.AudioUpdateListener =
        object : PlayerModel.AudioUpdateListener {
            override fun onRecorderToggle(toggle: Boolean) {

            }

            override fun onMicVolumeChanged(value: Int) {
                updateNotifyMessage("麥克風音量 : $value")
            }

            override fun onMusicVolumeChanged(value: Int) {
                updateNotifyMessage("音樂音量 : $value")
            }

            override fun onEffectVolumeChanged(value: Int) {
                updateNotifyMessage("回音音量 : $value")
            }

            override fun onToneChanged(value: Int) {
                updateNotifyMessage("音調 : ${value - 7}")
            }

        }

    private val playerControlListener: PlayerModel.PlayerControlListener =
        object : PlayerModel.PlayerControlListener {
            override fun onAddTrack(track: Track?) {
                mNextTrack = track
            }

            override fun onResume() {
                playerControl.postValue(PlayerControl.RESUME)
            }

            override fun onPause() {
                playerControl.postValue(PlayerControl.PAUSE)
            }

            override fun onCut() {
                playerControl.postValue(PlayerControl.CUT)
            }

            override fun onReplay() {
                playerControl.postValue(PlayerControl.REPLAY)
            }

            override fun onMuteToggle(mute: Boolean) {
                playerControl.postValue(PlayerControl.MUTE(mute))
            }

            override fun onRatingToggle(enable: Boolean) {
                playerControl.postValue(PlayerControl.RATING(enable))
            }

            override fun onVocalChanged(type: Int) {
                playerControl.postValue(PlayerControl.VOCAL(type))
            }

        }

    private val roomStateChangeListener: PlayerModel.RoomStateChangeListener =
        object : PlayerModel.RoomStateChangeListener {
            override fun onNetworkConnectionChanged(connected: Boolean) {
                isConnected.postValue(connected)
            }

            override fun onRoomOpenStateChanged(opened: Boolean) {
                isRoomOpened.postValue(opened)
            }
        }

    private val osdListener: PlayerModel.OsdListener = object : PlayerModel.OsdListener {
        override fun onOsdEvent(messageBundle: MessageBundle) {
            if (messageBundle.type != MessageBundle.Type.VOD)
                osdMessage.postValue(messageBundle)
            else
                updateNotifyMessage(messageBundle.data as String)
        }
    }
}