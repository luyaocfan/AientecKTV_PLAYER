package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.ktv_pos_tablet.structure.Floor
import kotlinx.coroutines.launch

class FloorViewModel : ViewModelImpl() {
    val floors: MutableLiveData<ArrayList<Floor>> = MutableLiveData()

    val selectFloor: MutableLiveData<Floor> = MutableLiveData()

    val hasLastFloor: MutableLiveData<Boolean> = MutableLiveData()

    val hasNextFloor: MutableLiveData<Boolean> = MutableLiveData()

    fun onFloorSelected(index: Int) {
        selectFloor.postValue(floors.value!![index])

        hasLastFloor.postValue(index != 0)

        hasNextFloor.postValue(index != floors.value!!.size - 1)
    }

    fun getSelectionFloorId(): Int? {
        return selectFloor.value?.id
    }

    fun getSelectionFloorIndex(): Int {
        if (selectFloor.value == null) return -1
        return floors.value?.indexOf(selectFloor.value!!) ?: -1
    }

    fun nextFloor() {
        val index: Int = getSelectionFloorIndex() + 1
        onFloorSelected(index.coerceAtMost(floors.value!!.size - 1))
    }

    fun lastFloor() {
        val index: Int = getSelectionFloorIndex() - 1
        onFloorSelected(index.coerceAtLeast(0))
    }

    fun updateFloors(isShowAll: Boolean) {
        viewModelScope.launch {
            val list: ArrayList<Floor> = repository?.getFloorsInfo() ?: return@launch

            if (!isShowAll)
                list.removeAt(0)

            floors.postValue(list)
        }
    }

}