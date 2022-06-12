package com.aientec.ktv_staff.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_staff.model.Repository
import com.aientec.structure.User
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class UserViewModel : ViewModelImpl() {
    private lateinit var repository: Repository

    val user: MutableLiveData<User> = MutableLiveData()

    fun onLogin(account: String, password: String) {
        viewModelScope.launch {
            user.postValue(repository.login(account, password))
//            user.postValue(User())
        }
    }

    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)
        repository = getModel("Repo") as Repository
    }
}