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
import com.ine.ktv.playerengine.InePlayerController
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

    private var mController: InePlayerController? = null
    val isIdle: MutableLiveData<Boolean> = MutableLiveData()

    val notifyMessage: MutableLiveData<String?> = MutableLiveData()

    val nextDisplay: MutableLiveData<String?> = MutableLiveData()

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

    val toastMsg: MutableLiveData<String?> = MutableLiveData()

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

//    private var nextTrackLocker: Boolean = false

    /**
     * 系統初始化
     */
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

    /**
     * 請求下一首歌
     */
    fun nextTrackRequest() {
        viewModelScope.launch {
//            nextTrackLocker = true
            Log.d(TAG, "OnNexReq")
            model.nextSongRequest()
        }
    }

    /**
     * 開始播放事件
     */
    fun onPlayerStart() {
        viewModelScope.launch {
//            nextTrackLocker = true
            mNextTrack = null
            model.notifyPlayFn(8)
            model.nextSongRequest()
            mIsIdle = false
            //Log.d("luyao", "fun onPlayerStart mIsIdle: $mIsIdle  playerState: $playerState.value")
        }
    }

    /**
     * 結束播放事件
     */
    fun onPlayerEnd() {
        viewModelScope.launch {
            if (mNextTrack == null)
                model.notifyPlayFn(9)

            //if (playerState.value == PLAYER_STATE_PAUSE)
            mIsIdle = true
            //Log.d("luyao", "fun onPlayerEnd mIsIdle: $mIsIdle  playerState: $playerState.value")
        }
    }

    /**
     * 繼續播放事件
     */
    fun onPlayerResume() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_RESUME, false)
            model.notifyPlayFn(4)
        }
        if (mController != null)
            mController = null
    }

    /**
     * 暫停播放事件
     */
    fun onPlayerPause(Controller: InePlayerController? = null) {
        viewModelScope.launch {
            updateState(PLAYER_STATE_PAUSE, true)
            model.notifyPlayFn(5)
        }
        mController = Controller
    }

    /**
     * 切歌事件
     */
    fun onPlayerCut() {
        updateState(PLAYER_STATE_CUT, false)
    }

    /**
     * 重唱事件
     */
    fun onPlayerReplay() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_REPLAY, false)
            model.notifyPlayFn(7)
        }

    }

    /**
     * 靜音切換事件
     */
    fun onPlayerMuteToggle(mute: Boolean) {
        viewModelScope.launch {
            isMute.postValue(mute)
            model.notifyPlayFn(if (mute) 10 else 11)
        }
    }

    /**
     * MTV撥放改變
     */
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

    /**
     * 評分功能切換
     */
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

    /**
     * 撥放狀態顯示實現
     * @param state 撥放器狀態
     * @param keep 是否讓顯示保留 true : 永久保留 false : 顯示1000毫秒後移除
     */
    private fun updateState(state: Int, keep: Boolean) {
        //Log.d("luyao", "fun updateState state: $state  keep: $keep  mIsIdle: $mIsIdle")
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

    /**
     * 左上角消息展示，1000毫秒後移除
     * @param msg 顯示文字內容
     */
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

    fun onOsdDone() {
        Log.d(TAG, "OnOsdDone")
        osdMessage.postValue(null)
        if (mController != null)
        {
            if (mController?.isPaused == true) {
                mController?.resume()
                onPlayerResume()
            }
            mController = null
        }
    }

    fun onToast(msg: String) {
        viewModelScope.launch {
            toastMsg.postValue(msg)
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
//                nextTrackLocker = false
            }

            override fun onResume() {
                playerControl.postValue(PlayerControl.RESUME)
            }

            override fun onPause(osd: Boolean) {
                playerControl.postValue(PlayerControl.PAUSE(osd))
            }

            override fun onCut() {
//                if (!nextTrackLocker)
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

    fun _test() {
        viewModelScope.launch {
            model._test()
        }
    }
}