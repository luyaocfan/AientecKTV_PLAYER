package com.aientec.ktv_pos_tablet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pos_tablet.model.Repository
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.structure.User
import kotlinx.coroutines.launch

class UserViewModel : ViewModelImpl() {

    val user: MutableLiveData<User> = MutableLiveData()

    fun onLogin(account: String, password: String) {
        viewModelScope.launch {
            user.postValue(repository?.staffLogin(account, password))
//            user.postValue(User())
        }
    }
}