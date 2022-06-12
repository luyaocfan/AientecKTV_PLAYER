package com.aientec.appplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.appplayer.BuildConfig
import com.aientec.appplayer.model.Repository
import kotlinx.coroutines.launch

class SystemViewModel : ViewModelImpl() {
      val isDataSyn: MutableLiveData<Boolean?> = MutableLiveData(null)

      lateinit var connectionState: LiveData<Boolean>

      fun onApplicationInit() {
            viewModelScope.launch {


//                  if (BuildConfig.DEBUG)
//                        isDataSyn.postValue(true)
//                  else {
                        isProgress.postValue(true)
                        val result: Boolean = repository.systemInitial()
                        isProgress.postValue(false)

                        isDataSyn.postValue(result)
//                  }
            }
      }

      override fun onRepositoryAttach(repo: Repository) {
            super.onRepositoryAttach(repo)
            connectionState = repo.connectionState
      }
}