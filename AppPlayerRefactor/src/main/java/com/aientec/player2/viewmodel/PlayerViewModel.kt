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
     * ???????????????
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
     * ??????????????????
     */
    fun nextTrackRequest() {
        viewModelScope.launch {
//            nextTrackLocker = true
            Log.d(TAG, "OnNexReq")
            model.nextSongRequest()
        }
    }

    /**
     * ??????????????????
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
     * ??????????????????
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
     * ??????????????????
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
     * ??????????????????
     */
    fun onPlayerPause(Controller: InePlayerController? = null) {
        viewModelScope.launch {
            updateState(PLAYER_STATE_PAUSE, true)
            model.notifyPlayFn(5)
        }
        mController = Controller
    }

    /**
     * ????????????
     */
    fun onPlayerCut() {
        updateState(PLAYER_STATE_CUT, false)
    }

    /**
     * ????????????
     */
    fun onPlayerReplay() {
        viewModelScope.launch {
            updateState(PLAYER_STATE_REPLAY, false)
            model.notifyPlayFn(7)
        }

    }

    /**
     * ??????????????????
     */
    fun onPlayerMuteToggle(mute: Boolean) {
        viewModelScope.launch {
            isMute.postValue(mute)
            model.notifyPlayFn(if (mute) 10 else 11)
        }
    }

    /**
     * MTV????????????
     */
    fun onPlayerVocalChanged(type: Int) {
        viewModelScope.launch {
            when (type) {
                1 -> updateNotifyMessage("??????")
                2 -> updateNotifyMessage("??????")
                3 -> updateNotifyMessage("??????")
            }
            model.notifyPlayFn(type)
        }
    }

    /**
     * ??????????????????
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
     * ????????????????????????
     * @param state ???????????????
     * @param keep ????????????????????? true : ???????????? false : ??????1000???????????????
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
     * ????????????????????????1000???????????????
     * @param msg ??????????????????
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
                updateNotifyMessage("??????????????? : $value")
            }

            override fun onMusicVolumeChanged(value: Int) {
                updateNotifyMessage("???????????? : $value")
            }

            override fun onEffectVolumeChanged(value: Int) {
                updateNotifyMessage("???????????? : $value")
            }

            override fun onToneChanged(value: Int) {
                updateNotifyMessage("?????? : ${value - 7}")
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