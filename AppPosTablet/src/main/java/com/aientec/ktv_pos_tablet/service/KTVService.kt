package com.aientec.ktv_pos_tablet.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.aientec.ktv_pos_tablet.model.Hardware
import com.aientec.ktv_pos_tablet.model.Repository

class KTVService : Service() {
    private val binder: ServiceBinder = ServiceBinder()

    val repository: Repository = Repository(this)

    val hardware: Hardware = Hardware(this)

    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    fun init() {
        repository.init()
        hardware.init()
    }

    inner class ServiceBinder : Binder() {
        val service: KTVService = this@KTVService
    }
}