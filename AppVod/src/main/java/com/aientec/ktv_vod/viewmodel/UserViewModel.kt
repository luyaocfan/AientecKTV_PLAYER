package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.service.VodService
import com.aientec.structure.User

class UserViewModel : ViewModelImpl() {
    lateinit var users: MutableLiveData<List<User>>

    override fun onServiceConnected(service: VodService) {
        super.onServiceConnected(service)
        users = repository.users
    }

    fun onUserSelected(id: Int) {

    }
}