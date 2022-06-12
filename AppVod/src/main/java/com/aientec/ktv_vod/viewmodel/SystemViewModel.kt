package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.common.structure.Configuration
import com.aientec.structure.OpenInfo
import com.aientec.structure.Room
import com.aientec.structure.Store
import kotlinx.coroutines.launch

class SystemViewModel : ViewModelImpl() {
      val configuration: MutableLiveData<Configuration> = MutableLiveData()

      val serviceReady: MutableLiveData<Boolean> = MutableLiveData()

      var qrCodeData: MutableLiveData<String>? = null

      val stores: MutableLiveData<List<Store>> = MutableLiveData()

      val selectedStore: MutableLiveData<Store> = MutableLiveData()

      val rooms: MutableLiveData<List<Room>> = MutableLiveData()

      val selectedRoom: MutableLiveData<Room> = MutableLiveData()

      val dataAgentInfo: MutableLiveData<Pair<String, Int>> = MutableLiveData()

      val stateMessage: MutableLiveData<String> = MutableLiveData()

      lateinit var connectState: LiveData<Boolean>

      lateinit var isOpen: LiveData<Boolean>

      val uuid: String
            get() = repository.uuid

      val roomId: Int
            get() = repository.roomId

      fun readConfiguration() {
            viewModelScope.launch {
                  configuration.postValue(repository.getConfiguration())
            }
      }

      fun updateStores() {
            viewModelScope.launch {
                  stores.postValue(repository.getStores())
            }
      }

      fun updateRooms() {
            viewModelScope.launch {
                  val store: Store = selectedStore.value ?: return@launch
                  rooms.postValue(repository.getRooms(store.id))
            }
      }

      fun onStoreSelected(store: Store) {
            selectedStore.postValue(store)

      }

      fun onRoomSelected(room: Room) {
            selectedRoom.postValue(room)
      }

      fun onDataAgentInfoSetup(ip: String, port: Int) {
            dataAgentInfo.postValue(Pair(ip, port))
      }

      fun debugOpen() {
            viewModelScope.launch {
                  repository.debugOpen()
            }
      }

      fun checkOpenInfo() {
            viewModelScope.launch {
                  repository.checkOpenInfo()
            }
      }

      fun createConfiguration() {
            viewModelScope.launch {
                  stateMessage.postValue("初始化中")

                  val store: Store = selectedStore.value!!

                  val room: Room = selectedRoom.value!!

                  val dataAgent: Pair<String, Int> = dataAgentInfo.value!!

                  val config: Configuration = Configuration().apply {
                        storeId = store.id
                        storeName = store.name
                        roomId = room.id
                        roomName = room.name
                        dataAgentIp = dataAgent.first
                        dataAgentPort = dataAgent.second
                  }

                  if (repository.updateConfiguration(config)) {
                        configuration.postValue(repository.getConfiguration())
                  }
            }

      }

      fun systemInitialize() {
            viewModelScope.launch {
                  val config: Configuration = configuration.value!!
                  environmental.setAddress(config.dataAgentIp, config.dataAgentPort)
                  stateMessage.postValue("尋找命令轉送服務")
                  if (environmental.findDataServer()) {
                        stateMessage.postValue("伺服器連接中")
                        if (environmental.connectDataServer()) {
                              stateMessage.postValue("正在註冊服務")
                              if (environmental.register()) {
                                    stateMessage.postValue("正在同步數據")

                                    serviceReady.postValue(repository.synDatabase())

                              } else
                                    serviceReady.postValue(false)
                        } else
                              serviceReady.postValue(false)
                  } else
                        serviceReady.postValue(false)

            }
      }

      fun refreshWifiAp() {
            viewModelScope.launch {
                  repository.refreshWifiConfig()
            }
      }

      override fun onServiceConnected(service: VodService) {
            super.onServiceConnected(service)
            qrCodeData = repository.qrCodeData
            isOpen = repository.roomOpen
            connectState = environmental.connectedState
      }
}