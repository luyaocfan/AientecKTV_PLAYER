package com.aientec.ktv_player2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_player2.BuildConfig
import com.aientec.ktv_player2.module.PlayerDataModel
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class SystemViewModel : ViewModelImpl() {
      val systemReady: LiveData<Boolean>
            get() = mSystemReady

      private val mSystemReady: MutableLiveData<Boolean> = MutableLiveData()

      private lateinit var playerDataModel: PlayerDataModel

      fun initialize() {
            viewModelScope.launch {

                  val res:Boolean = if(BuildConfig.DEBUG){
                        true
                  }else{
                        if (playerDataModel.connect())
                              playerDataModel.register()
                        else
                              false
                  }

                  mSystemReady.postValue(res)
            }
      }

      override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
            super.onServiceConnected(binder)
            playerDataModel = binder.getModel("PLAYER_MODEL") as PlayerDataModel
      }
}