package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aientec.ktv_pos_tablet.model.Hardware
import com.aientec.ktv_pos_tablet.model.Repository
import com.aientec.ktv_pos_tablet.service.KTVService

abstract class ViewModelImpl : ViewModel() {

    companion object{
        val isNavigationShown:MutableLiveData<Boolean> = MutableLiveData(true)

        fun toggleNavigationShown(isShown:Boolean){
            isNavigationShown.postValue(isShown)
        }
    }

    protected var repository: Repository? = null

    protected var hardware: Hardware? = null

    open fun onServiceConnected(service: KTVService) {
        repository = service.repository

        hardware = service.hardware
    }
}