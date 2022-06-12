package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.ktv_pos_tablet.structure.Order
import com.aientec.structure.Room
import kotlinx.coroutines.launch

class BoxViewModel : ViewModelImpl() {
    companion object {
        const val EVENT_NONE = 0
        const val EVENT_OPEN_BOX = 1
        const val EVENT_CLOSE_BOX = 2
        const val EVENT_CHECKOUT = 3
    }

    val boxItems: MutableLiveData<ArrayList<Any>> = MutableLiveData()

    val selectedBox: MutableLiveData<Box?> = MutableLiveData()

    val event: MutableLiveData<Int> = MutableLiveData(EVENT_NONE)

    val durationMin: MutableLiveData<Long> = MutableLiveData()

    private var mFloor: Int = -1

    private var mType: Int = -1

    fun filterBoxes(floor: Int = mFloor, type: Int = mType) {
        viewModelScope.launch {
            mFloor = floor
            mType = type

            val filterBoxes: ArrayList<Box> =
                repository?.filterBoxes(mFloor, mType) ?: return@launch
            val list = ArrayList<Any>()
            if (mType == -1) {
                val types: ArrayList<Room.Type> = repository?.getBoxTypes() ?: return@launch

                for (t in types) {
                    if (t.id == -1) continue

//                    list.add(t)
                    val subList: ArrayList<Box> = filterBoxes.filter {
                        return@filter it.type == t.id
                    } as ArrayList<Box>

                    if (subList.size > 0) {
                        list.add(t)
                        list.addAll(subList)
                    }
                }
            } else
                list.addAll(filterBoxes)

            boxItems.postValue(list)
        }
    }

    fun updateBoxes() {
        viewModelScope.launch {
            repository?.updateBoxes()
        }
    }

    fun onBoxSelected(box: Box?) {
        if (box == selectedBox.value)
            selectedBox.postValue(null)
        else
            selectedBox.postValue(box)
    }


    fun onEventChanged(flag: Int) {
        event.postValue(flag)
    }


    fun checkBill() {
        if (selectedBox.value == null) return
        viewModelScope.launch {
            val box = selectedBox.value!!
            val res = repository?.staffCheckBill(box, 1000, "") ?: false
            if (res) {
                hardware?.printBill(box)
                box.state = Room.STATE_ON_CHECKED
                selectedBox.postValue(box)
            }
        }
    }


}