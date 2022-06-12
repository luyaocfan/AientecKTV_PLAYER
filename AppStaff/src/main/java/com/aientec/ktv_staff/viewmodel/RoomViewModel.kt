package com.aientec.ktv_staff.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_staff.model.Repository
import com.aientec.structure.OpenInfo
import com.aientec.structure.Room
import com.aientec.structure.Store
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class RoomViewModel : ViewModelImpl() {
    private lateinit var repository: Repository

    val selectedRoom: MutableLiveData<Room> = MutableLiveData()

    val rooms: MutableLiveData<List<Room>> = MutableLiveData()

    val selectStore: MutableLiveData<Store> = MutableLiveData()

    val stores: MutableLiveData<List<Store>> = MutableLiveData()

    val isStart: MutableLiveData<Boolean> = MutableLiveData(null)

    val isClose: MutableLiveData<Boolean> = MutableLiveData(null)

    fun onRoomSelected(room: Room?) {
        selectedRoom.postValue(room)
    }

    fun onStoresUpdate() {
        viewModelScope.launch {
            stores.postValue(repository.getStores())
        }
    }

    fun onStoreSelect(store: Store?) {
        viewModelScope.launch {
            selectStore.postValue(store)
            if (store == null)
                rooms.postValue(null)
            else
                rooms.postValue(repository.getRooms(store.id))
        }
    }

    fun onRoomOpen() {
        viewModelScope.launch {
            val room: Room = selectedRoom.value ?: return@launch
//
//            val openInfo: OpenInfo = repository.getOpenInfo(room.id) ?: return@launch
//
//            isStart.postValue(repository.startTimer(openInfo.orderId))
        }
    }

    fun onInit() {
        isStart.postValue(null)
        isClose.postValue(null)
    }

    fun onRoomClose() {
        viewModelScope.launch {
            val room: Room = selectedRoom.value ?: return@launch

            isClose.postValue(repository.closeBox(room.id))
        }
    }

    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)
        repository = getModel("Repo") as Repository
    }
}