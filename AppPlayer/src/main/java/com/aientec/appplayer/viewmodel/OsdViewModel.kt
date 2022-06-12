package com.aientec.appplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.appplayer.data.MessageBundle
import com.aientec.appplayer.model.Repository
import com.aientec.structure.Track
import com.linecorp.apng.ApngDrawable
import kotlinx.coroutines.launch

class OsdViewModel : ViewModelImpl() {
    var message: MutableLiveData<MessageBundle>? = null

    var marque: MutableLiveData<String>? = null

    val barrageMessage: MutableLiveData<String> = MutableLiveData()

    val notifyMassage: MutableLiveData<String> = MutableLiveData()

    val nextTrack: MutableLiveData<Track> = MutableLiveData()

    val isPublish: MutableLiveData<Boolean> = MutableLiveData()

    override fun onRepositoryAttach(repo: Repository) {
        super.onRepositoryAttach(repo)

        message = repo.osdMessage

        repo.addAudioUpdateListener(object : Repository.AudioUpdateListener {
            override fun onRecorderToggle(toggle: Boolean) {

            }

            override fun onMicVolumeChanged(value: Int) {
                notifyMassage.postValue("麥克風音量 : $value")
            }

            override fun onMusicVolumeChanged(value: Int) {
                notifyMassage.postValue("音樂音量 : $value")
            }

            override fun onEffectVolumeChanged(value: Int) {
                notifyMassage.postValue("特效音量 : $value")
            }

            override fun onToneChanged(value: Int) {
                notifyMassage.postValue("回音 : $value")
            }
        })
    }

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
}