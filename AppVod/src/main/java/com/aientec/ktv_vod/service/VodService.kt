package com.aientec.ktv_vod.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_portal2.PortalService2
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.module.Environmental
import com.aientec.ktv_vod.module.Repository
import java.util.*

class VodService : Service() {

      companion object {
            const val STATE_NONE = 0

            const val STATE_READY = 1

            const val STATE_ERROR = 2

            val state: MutableLiveData<Int> = MutableLiveData()
      }

      private val binder: IBinder = ServiceBinder()

      val repository: Repository = Repository(this)

      val environmental: Environmental = Environmental(this)

      override fun onBind(intent: Intent): IBinder {
            return binder
      }

      override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            init()
            return START_STICKY
      }

      override fun onDestroy() {
            release()
            super.onDestroy()
      }

      private fun init() {

            repository.dataBaseRemoteRoot = BuildConfig.FILE_ROOT

            PortalService2.apiRoot = BuildConfig.PORTAL_SERVER

            repository.init()

            environmental.init()
      }

      private fun release() {
            repository.release()

            environmental.release()
      }

      inner class ServiceBinder() : Binder() {
            fun getService(): VodService = this@VodService
      }
}