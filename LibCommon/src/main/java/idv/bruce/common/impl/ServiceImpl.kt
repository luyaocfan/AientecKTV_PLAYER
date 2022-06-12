package idv.bruce.common.impl

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData

abstract class ServiceImpl : Service() {
    companion object {
        val serviceStartLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

        val serviceStart: Boolean = serviceStartLiveData.value!!
    }

    protected abstract val modelList: List<ModelImpl>?

    private val binder:ServiceBinder = ServiceBinder()

    override fun onCreate() {
        super.onCreate()
        modelList?.forEach { it.init() }
    }

    override fun onDestroy() {
        modelList?.forEach { it.release() }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        serviceStartLiveData.postValue(true)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class ServiceBinder : Binder() {
        val models: List<ModelImpl>? = modelList

        fun getModel(tag: String): ModelImpl? = modelList?.find { it.tag == tag }
    }
}