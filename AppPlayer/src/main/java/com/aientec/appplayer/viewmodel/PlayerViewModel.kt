package com.aientec.appplayer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.appplayer.data.EventBundle
import com.aientec.appplayer.data.MTVEvent
import com.aientec.appplayer.data.MessageBundle
import com.aientec.appplayer.model.Repository
import com.aientec.ktv_wifiap.commands.DSData
import com.aientec.ktv_wifiap.commands.PlayerData
import com.aientec.structure.Track
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModelImpl() {
      val idleTracks: MutableLiveData<List<Track>?> = MutableLiveData()

      val nextTrack: MutableLiveData<Track?> = MutableLiveData()

      lateinit var playerFunc: MutableLiveData<DSData.PlayerFunc>

      val mtvEvent: MutableLiveData<EventBundle?> = MutableLiveData()

      val scoreMode: MutableLiveData<Boolean> = MutableLiveData(false)

      lateinit var openState: LiveData<Boolean>

      private var isMute: Boolean = false

      //begin OSD
      var message: MutableLiveData<MessageBundle>? = null

      var marque: MutableLiveData<String>? = null

      val barrageMessage: MutableLiveData<String> = MutableLiveData()

      val notifyMassage: MutableLiveData<String> = MutableLiveData()

      //val nextTrack: MutableLiveData<Track> = MutableLiveData()

      val isPublish: MutableLiveData<Boolean> = MutableLiveData()
      //end OSD

      override fun onRepositoryAttach(repo: Repository) {
            super.onRepositoryAttach(repo)
            playerFunc = repository.playerFunc
            openState = repository.openState
            repository.addEventListener(listener)

            repo.addAudioUpdateListener(object : Repository.AudioUpdateListener {
                  override fun onRecorderToggle(toggle: Boolean) {

                  }

                  override fun onMicVolumeChanged(value: Int) {

                  }

                  override fun onMusicVolumeChanged(value: Int) {
                        if (isMute && value > 0) {
                              playerFunc.postValue(DSData.PlayerFunc.UN_MUTE)
                              onUnMute()
                        }
                        if (!isMute && value == 0)
                              onMute()
                  }

                  override fun onEffectVolumeChanged(value: Int) {

                  }

                  override fun onToneChanged(value: Int) {

                  }
            })

            //begin OSD
            message = repo.osdMessage
            repo.addAudioUpdateListener(object : Repository.AudioUpdateListener {
                  override fun onRecorderToggle(toggle: Boolean) {

                  }

                  override fun onMicVolumeChanged(value: Int) {
                        notifyMassage.postValue("??????????????? : $value")
                  }

                  override fun onMusicVolumeChanged(value: Int) {
                        notifyMassage.postValue("???????????? : $value")
                  }

                  override fun onEffectVolumeChanged(value: Int) {
                        notifyMassage.postValue("???????????? : $value")
                  }

                  override fun onToneChanged(value: Int) {
                        notifyMassage.postValue("?????? : ${value-7}")
                  }
            })
            //end OSD

      }

      fun onReady() {
            viewModelScope.launch {
                  val list = repository.updateIdleTracks()
                  idleTracks.postValue(list ?: listOf(Track().apply {
                        fileName = "52898YH1.mp4"
                  }))
                  repository.nextSongRequest()
            }
      }

      fun onOpen(){
            viewModelScope.launch {
                  repository.nextSongRequest()
            }
      }


      fun onPlaying() {
            viewModelScope.launch {
                  nextTrack.postValue(null)
                  repository.notifyPlayFn(8)
                  repository.nextSongRequest()
            }
      }

      fun onScoreToggle(enable: Boolean) {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(if (enable) MTVEvent.ON_SCORE_ENABLE else MTVEvent.ON_SCORE_DISABLE))
            }
      }

      fun onLocalPause() {
            viewModelScope.launch {
                  playerFunc?.postValue(DSData.PlayerFunc.FORCE_PAUSE)
            }
      }

      fun onLocalPlay() {
            viewModelScope.launch {
                  playerFunc?.postValue(DSData.PlayerFunc.FORCE_PLAY)
            }
      }

      fun onOriginalVocal() {
            viewModelScope.launch {
                  repository.notifyPlayFn(1)
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_VOCAL_ORIGINAL))
            }
      }

      fun onBackingVocal() {
            viewModelScope.launch {
                  repository.notifyPlayFn(2)
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_VOCAL_BACKING))
            }
      }

      fun onGuideVocal() {
            viewModelScope.launch {
                  repository.notifyPlayFn(3)
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_VOCAL_GUIDE))
            }
      }

      fun onStop() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_STOP, nextTrack.value))
                  if (nextTrack.value == null)
                        repository.notifyPlayFn(9)
            }
      }

      fun onPause() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_PAUSE))
                  repository.notifyPlayFn(5)
            }
      }


      fun onResume() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_RESUME))
                  repository.notifyPlayFn(4)
            }
      }

      fun onCut() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_CUT))
//            repository.notifyPlayFn(6)
            }
      }

      fun onReplay() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_REPLAY))
                  repository.notifyPlayFn(7)
            }
      }

      fun onNextDisplay() {
            viewModelScope.launch {
                  mtvEvent.postValue(EventBundle(MTVEvent.ON_NEXT_DISPLAY, nextTrack.value))
            }
      }

      fun onMute() {
            viewModelScope.launch {
                  if (!isMute) {
                        isMute = true
                        mtvEvent.postValue(EventBundle(MTVEvent.ON_MUTE))
                        repository.notifyPlayFn(10)
                  }
            }
      }

      fun onUnMute() {
            viewModelScope.launch {
                  if (isMute) {
                        isMute = false
                        mtvEvent.postValue(EventBundle(MTVEvent.ON_UN_MUTE))
                        repository.notifyPlayFn(11)
                  }
            }
      }


      private val listener: Repository.EventListener = object : Repository.EventListener {
            override fun onNextTrack(track: Track?) {
                  nextTrack.postValue(track)
            }

            override fun onScoreMode(enable: Boolean) {
                  scoreMode.postValue(enable)
            }
      }
      //begin OSD
      fun onNotify(msg: String) {
            notifyMassage.postValue(msg)
      }

      fun test() {
            val bundle = MessageBundle().also {
                  it.senderIcon = null
                  it.data = "Test"
                  it.type = MessageBundle.Type.TXT
                  it.sender = "dd"
            }
            message?.postValue(bundle)
//        barrageMessage.postValue("Test : 123456")
      }

      fun onTypeChanged(isPub: Boolean) {
            viewModelScope.launch {
                  isPublish.postValue(isPub)
            }
      }
      //end OSD
}