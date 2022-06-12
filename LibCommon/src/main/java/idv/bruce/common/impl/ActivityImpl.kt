package idv.bruce.common.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

abstract class ActivityImpl : AppCompatActivity() {
    companion object {
        private const val PERMISSION_CODE = 0x445
    }

    protected abstract val autoStartService: Boolean

    protected abstract val viewModelList: List<Class<out ViewModelImpl>>?

    protected abstract val permissions: Array<String>?

    protected abstract val serviceCls: Class<out ServiceImpl>

    private val viewModelInstanceList: ArrayList<ViewModelImpl> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE)
            init()
    }

    private fun init() {

        viewModelList?.forEach {
            viewModelInstanceList.add(ViewModelProvider(this)[it])
        }

        if (checkPermissions()) {
            ServiceImpl.serviceStartLiveData.observe(this, {
                if (it) {
                    bindService(
                        Intent(this, serviceCls),
                        serviceConnection,
                        Context.BIND_IMPORTANT
                    )
                } else {
                    if (autoStartService)
                        startService(Intent(this, serviceCls))
                    else
                        onServiceNotStart()
                }
            })
        } else {
            requestPermissions(permissions!!, PERMISSION_CODE)
        }
    }

    private fun checkPermissions(): Boolean {
        val mPermissions: Array<String> = permissions ?: return true

        for (permission in mPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }

        return true
    }

    protected fun getViewModel(classType: Class<out ViewModelImpl>): ViewModelImpl? =
        viewModelInstanceList.find { it::class.java == classType }

    abstract fun initView()

    abstract fun onServiceNotStart()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val serviceBinder = (p1 ?: return) as ServiceImpl.ServiceBinder

            viewModelInstanceList.forEach {
                it.onServiceConnected(serviceBinder)
            }

            ViewModelImpl.toastMsg.observe(this@ActivityImpl, {
                if (it != null)
                    Toast.makeText(this@ActivityImpl, it, Toast.LENGTH_LONG).show()
            })

            initView()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            viewModelInstanceList.forEach {
                it.onServiceDisconnect()
            }
        }
    }
}