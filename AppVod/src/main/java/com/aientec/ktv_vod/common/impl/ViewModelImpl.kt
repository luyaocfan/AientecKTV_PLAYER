package com.aientec.ktv_vod.common.impl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aientec.ktv_vod.module.Environmental
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.service.VodService

abstract class ViewModelImpl : ViewModel() {
    companion object {
        val isProgress: MutableLiveData<Boolean> = MutableLiveData()

        val toast: MutableLiveData<String> = MutableLiveData()

        val alertMsgRes: MutableLiveData<Int> = MutableLiveData()
    }

    protected lateinit var repository: Repository

    protected lateinit var environmental: Environmental


    open fun onServiceConnected(service: VodService) {
        repository = service.repository

        environmental = service.environmental
    }
}