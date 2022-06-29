package com.aientec.player2.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aientec.player2.model.PlayerModel
import com.aientec.structure.Track
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private lateinit var model: PlayerModel

    val dataSyn: MutableLiveData<Boolean> = MutableLiveData(false)

    val idleMTVList: MutableLiveData<List<Track>?> = MutableLiveData()

    fun systemInit(context: Context) {
        viewModelScope.launch {
            model = PlayerModel.getInstance(context)

            val res: Boolean = model.systemInitial()

            dataSyn.postValue(res)

            idleMTVList.postValue(model.updateIdleTracks())
        }
    }


}