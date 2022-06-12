package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.structure.Configuration
import com.aientec.structure.Store
import kotlinx.coroutines.launch

class SystemViewModel : ViewModelImpl() {
    val stateMessage: MutableLiveData<String> = MutableLiveData()

    val isDataSyn: MutableLiveData<Boolean> = MutableLiveData()

    val stores: MutableLiveData<ArrayList<Store>> = MutableLiveData()

    val configuration: MutableLiveData<Configuration> = MutableLiveData()

    fun dataSyn() {
        viewModelScope.launch {
            stateMessage.postValue("數據同步中")
            val result: String? = repository!!.dataSyn()
            if (result != null)
                stateMessage.postValue(result)
            isDataSyn.postValue(result == null)
        }
    }

    fun updateStores() {
        viewModelScope.launch {
            stores.postValue(repository?.getStores())
        }
    }

    fun updateConfiguration(){
        viewModelScope.launch {
            configuration.postValue(repository?.readConfig())
        }
    }

    fun onStoreSelected(store: Store) {
        viewModelScope.launch {
            val config:Configuration = Configuration(store.id, store.name)
            stateMessage.postValue("更新設定")
            if(repository?.updateConfig(config) == true){
                configuration.postValue(repository?.readConfig())
            }else{
                stateMessage.postValue("更新失敗")
            }
        }
    }
}