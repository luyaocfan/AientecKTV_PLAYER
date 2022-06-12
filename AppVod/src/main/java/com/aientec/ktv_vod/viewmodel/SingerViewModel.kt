package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.structure.Singer
import kotlinx.coroutines.launch

class SingerViewModel:ViewModelImpl() {
    val singerList:MutableLiveData<List<Singer>?> = MutableLiveData()

    fun singerListUpdate(){
        viewModelScope.launch {
            singerList.postValue(repository.getSingerList())
        }
    }
}