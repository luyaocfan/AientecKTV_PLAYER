package com.aientec.ktv_pantry.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pantry.model.Hardware
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class SystemViewModel : ViewModelImpl() {
    val isReady: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var hardware: Hardware

    fun initSystem() {
        viewModelScope.launch {
            isReady.postValue(hardware.linkPrinter())
        }
    }

    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)
        hardware = binder.getModel("HW") as Hardware
    }
}