package com.aientec.player2.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    }

    private lateinit var model: PlayerModel

    val dataSyn: MutableLiveData<Boolean> = MutableLiveData(false)

    val idleMTVList: MutableLiveData<List<Track>?> = MutableLiveData()

    val isIdle: MutableLiveData<Boolean> = MutableLiveData()

    val notifyMessage: MutableLiveData<String?> = MutableLiveData()

    val playerState: MutableLiveData<Int> = MutableLiveData()

    val isMute: MutableLiveData<Boolean> = MutableLiveData()

    val nextTrack: MutableLiveData<Track?> = MutableLiveData()

    val playerControl: MutableLiveData<PlayerControl?> = MutableLiveData(null)

    fun systemInit(context: Context) {
        viewModelScope.launch {
            model = PlayerModel.getInstance(context)

            model.playerControlListener = playerControlListener

            val res: Boolean = model.systemInitial()

            dataSyn.postValue(res)

            idleMTVList.postValue(model.updateIdleTracks())
        }
    }

    fun onNextReq() {
        viewModelScope.launch {
            Log.d(TAG, "OnNexReq")
            model.nextSongRequest()
        }
    }

    fun onMTVTypeChanged(idle: Boolean) {
        isIdle.postValue(idle)
    }

    fun onPlayerResume() {
        playerState.postValue(PLAYER_STATE_RESUME)
        disableState(1000L)
    }

    fun onPlayerPause() {
        playerState.postValue(PLAYER_STATE_PAUSE)
    }

    fun onPlayerCut() {
        playerState.postValue(PLAYER_STATE_CUT)
        disableState(1000L)
    }

    fun onPlayerReplay() {
        playerState.postValue(PLAYER_STATE_REPLAY)
        disableState(1000L)
    }

    private fun disableState(time: Long) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                playerState.postValue(PLAYER_STATE_NONE)
            }
        }, time)
    }

    private val playerControlListener: PlayerModel.PlayerControlListener =
        object : PlayerModel.PlayerControlListener {
            override fun onAddTrack(track: Track?) {
                nextTrack.postValue(track)
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

            override fun onScoreToggle(enable: Boolean) {
                playerControl.postValue(PlayerControl.SCORE(enable))
            }

            override fun onVocalChanged(type: Int) {
                playerControl.postValue(PlayerControl.VOCAL(type))
            }

        }

}