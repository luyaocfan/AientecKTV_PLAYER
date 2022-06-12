package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.structure.Reserve
import kotlinx.coroutines.launch

class OrderViewModel : ViewModelImpl() {
    val box: MutableLiveData<Box?> = MutableLiveData()

    val reserve: MutableLiveData<Reserve?> = MutableLiveData()

    val duration: MutableLiveData<Int?> = MutableLiveData()

    val isCreated: MutableLiveData<Boolean> = MutableLiveData()

    val isBoxClose: MutableLiveData<Boolean> = MutableLiveData()

    fun setBox(b: Box?) {
        viewModelScope.launch {
            box.postValue(b)
        }
    }

    fun setReserve(r: Reserve?) {
        viewModelScope.launch {
            reserve.postValue(r)
        }
    }

    fun setDuration(d: Int?) {
        viewModelScope.launch {
            duration.postValue(d)
        }
    }

    fun createOrder() {
        viewModelScope.launch {
            val boxVal: Box = box.value ?: return@launch

            val reserveVal: Reserve = reserve.value ?: return@launch

            val durationVal: Int = duration.value ?: return@launch

            if (durationVal <= 0) return@launch

            val res: Boolean = repository?.openBox(boxVal, reserveVal, 1, durationVal) ?: false

            if (res)
                hardware?.printOrder(
                    boxVal,
                    reserveVal,
                    repository?.getBoxType(boxVal.type) ?: return@launch
                )

            isCreated.postValue(res)
        }
    }

    fun closeBox() {
        viewModelScope.launch {
            isBoxClose.postValue(null)
            val boxVal: Box = box.value ?: return@launch

            isBoxClose.postValue(repository?.closeBox(boxVal) ?: false)
        }
    }

    fun orderInit() {
        viewModelScope.launch {
            isBoxClose.postValue(null)
            isCreated.postValue(null)
            duration.postValue(null)
            reserve.postValue(null)
        }
    }

}