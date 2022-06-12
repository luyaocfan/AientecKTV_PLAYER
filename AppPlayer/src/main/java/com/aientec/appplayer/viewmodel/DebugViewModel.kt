package com.aientec.appplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.appplayer.model.Repository
import kotlinx.coroutines.launch

class DebugViewModel : ViewModelImpl() {
      lateinit var logMsg: MutableLiveData<String>

      override fun onRepositoryAttach(repo: Repository) {
            super.onRepositoryAttach(repo)
            logMsg = repo.debugLog
      }

      fun addLog(msg: String) {
            viewModelScope.launch {
                  logMsg.postValue(msg)
            }
      }
}