package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.structure.Room
import kotlinx.coroutines.launch

class TypeViewModel:ViewModelImpl() {
    val types:MutableLiveData<ArrayList<Room.Type>> = MutableLiveData()

    val selectedType:MutableLiveData<Room.Type> = MutableLiveData()

    fun updateTypes(){
        viewModelScope.launch {

        }
    }

    fun getSelectionTypeId():Int?{
        return selectedType.value?.id
    }

    fun onTypeSelected(position:Int){
        selectedType.postValue(types.value?.get(position))
    }

    override fun onServiceConnected(service: KTVService) {
        super.onServiceConnected(service)
        viewModelScope.launch {
            types.postValue(repository?.getBoxTypes())
        }
    }
}