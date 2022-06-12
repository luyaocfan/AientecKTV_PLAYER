package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import be.teletask.onvif.models.Device
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.ktv_pos_tablet.structure.IpCamera
import com.aientec.ktv_pos_tablet.structure.MapMarker
import kotlinx.coroutines.launch

class MarkerViewModel : ViewModelImpl() {
    val isInit: MutableLiveData<Boolean> = MutableLiveData()

    val selectedCamera: MutableLiveData<IpCamera?> = MutableLiveData()

    val markers: MutableLiveData<ArrayList<MapMarker>?> = MutableLiveData()

    fun onCameraSelected(device: IpCamera?) {
        selectedCamera.postValue(device)
    }

    fun updateMarkers(floorId: Int) {
        viewModelScope.launch {
            markers.postValue(repository?.getMapMarkers(floorId))
        }
    }
}