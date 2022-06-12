package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.module.Environmental
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.structure.EnvironmentalStatus
import kotlinx.coroutines.launch

class ControlViewModel : ViewModelImpl() {
      val commandSendState: MutableLiveData<Boolean> = MutableLiveData()

      var vocalType: MutableLiveData<Environmental.VocalType>? = null

      var playState: MutableLiveData<Boolean>? = null

      lateinit var mainMode: LiveData<Int>

      lateinit var subMode: LiveData<Int>

      lateinit var basicLight: LiveData<Int>

      lateinit var micVolume: LiveData<Int>

      lateinit var musicVolume: LiveData<Int>

      lateinit var micEffectVolume: LiveData<Int>

      lateinit var micEffectMode: LiveData<Int>

      lateinit var playingState: MutableLiveData<Boolean>


      override fun onServiceConnected(service: VodService) {
            super.onServiceConnected(service)
            vocalType = environmental.vocalType

            playState = environmental.playState

            mainMode = environmental.mainModeId

            subMode = environmental.subModeId

            basicLight = environmental.basicLightId

            micVolume = environmental.micVolume

            musicVolume = environmental.musicVolume

            micEffectVolume = environmental.micEffectVolume

            micEffectMode = environmental.micEffectMode
      }

      fun cut() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.cut())
            }
      }

      fun playToggle() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.playToggle())
            }
      }

      fun replay() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.replay())
            }
      }

      fun vocalSwitch() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.vocalSwitch())
            }
      }

      fun scoreToggle() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.scoreToggle())
            }
      }

      fun micVolumeAdd() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.micAddVolume())
            }
      }

      fun micVolumeDesc() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.micDescVolume())
            }
      }

      fun micEffectAdd() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.micEffectAddVolume())
            }
      }

      fun micEffectDesc() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.micEffectDescVolume())
            }
      }

      fun musicVolumeAdd() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.musicAddVolume())
            }
      }

      fun musicVolumeDesc() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.musicDescVolume())
            }
      }

      fun muteToggle() {
            viewModelScope.launch {
                  commandSendState.postValue(environmental.player.muteToggle())
            }
      }

      fun onMainModeSelected(code: Int, message: String) {
            viewModelScope.launch {
                  if (code != mainMode.value) {
                        environmental.setMainMode(code)
                        environmental.sendVodMessage(message)
                  }
            }
      }

      fun onSubModeSelected(code: Int, message: String) {
            viewModelScope.launch {
                  if (code != subMode.value) {
                        environmental.setSubMode(code)
                        environmental.sendVodMessage(message)
                  }
            }
      }

      fun onModeReset() {
            viewModelScope.launch {
                  environmental.resetMode()
            }
      }

      fun onMicModeSelected(mode: Int) {
            viewModelScope.launch {
                  environmental.player.setMicMode(mode)
            }
      }

      fun onBasicLightSelected(code: Int, message: String) {
            viewModelScope.launch {
                  if (code != basicLight.value) {
                        if (code != mainMode.value) {
                              environmental.setBasicLight(code)
                              environmental.sendVodMessage(message)
                        }
                  }
            }
      }

      fun onEffectSend(code: Int, type: Int = 1) {
            viewModelScope.launch {
                  environmental.sendRoomEffect(code, type)
            }
      }

      fun getAudioSrc():Int = environmental.sourceType
}