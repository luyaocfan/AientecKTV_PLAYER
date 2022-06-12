package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.structure.Reserve
import kotlinx.coroutines.launch

class ReserveViewModel : ViewModelImpl() {
    val reserveList: MutableLiveData<List<Reserve>> = MutableLiveData()

    fun updateReserveList() {
        viewModelScope.launch {
            reserveList.postValue(repository?.getReserveList())
        }
    }

    fun updateCheckedReserveList() {
        viewModelScope.launch {
            reserveList.postValue(repository?.getReserveList(2))
        }
    }

    fun updateUncheckedReserveList() {
        viewModelScope.launch {
            reserveList.postValue(repository?.getReserveList(1))
        }
    }

    fun onReserveCheckIn(reserve: Reserve) {
        viewModelScope.launch {
            if (repository?.reserveCheckIn(reserve.id) == true)
                updateReserveList()
        }
    }

    fun onReserveCancel(reserve: Reserve) {
        viewModelScope.launch {
            if (repository?.reserveCancel(reserve.id) == true)
                updateReserveList()
        }
    }
}