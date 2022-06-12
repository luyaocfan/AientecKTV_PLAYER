package com.aientec.appplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aientec.appplayer.model.Repository

abstract class ViewModelImpl : ViewModel() {
    companion object {
        val isProgress: MutableLiveData<Boolean> = MutableLiveData()

        val toast: MutableLiveData<String> = MutableLiveData()
    }

    protected lateinit var repository: Repository

    val isAttached: MutableLiveData<Boolean> = MutableLiveData(false)

    open fun onRepositoryAttach(repo: Repository) {
        repository = repo
        isAttached.postValue(true)
    }
}