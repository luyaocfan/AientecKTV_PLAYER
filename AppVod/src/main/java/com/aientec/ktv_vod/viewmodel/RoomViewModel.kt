package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.service.VodService
import com.aientec.structure.Room
import com.aientec.structure.User

class RoomViewModel : ViewModelImpl() {


    var roomInfo: MutableLiveData<Room>? = null


    override fun onServiceConnected(service: VodService) {
        super.onServiceConnected(service)


        roomInfo = repository.roomInfo
    }
}