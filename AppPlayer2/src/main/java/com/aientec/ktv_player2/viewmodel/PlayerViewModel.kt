package com.aientec.ktv_player2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_player2.module.PlayerDataModel
import com.aientec.structure.Track
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModelImpl() {
    lateinit var adsTrackList: LiveData<List<Track>?>

    private lateinit var playerModel: PlayerDataModel

    fun updateAdsList() {
        viewModelScope.launch {
            playerModel.updateAdPlayList()
        }
    }

    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)
        playerModel = getModel("PLAYER_MODEL") as PlayerDataModel

        adsTrackList = playerModel.adsTrackList
    }
}