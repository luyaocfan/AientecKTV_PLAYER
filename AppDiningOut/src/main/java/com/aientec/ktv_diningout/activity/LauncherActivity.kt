package com.aientec.ktv_diningout.activity

import android.Manifest
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.aientec.ktv_diningout.TemporaryHandler
import com.aientec.ktv_diningout.service.DiningOutService
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LauncherActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = true
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = null
    override val permissions: Array<String>
        get() = arrayOf(Manifest.permission.CAMERA)
    override val serviceCls: Class<out ServiceImpl>
        get() = DiningOutService::class.java

    override fun initView() {
        checkCamera()
    }

    override fun onServiceNotStart() {

    }

    private fun checkCamera() {
        val cpf = ProcessCameraProvider.getInstance(this)

        cpf.addListener({
            val cp = cpf.get()
            Log.d("Trace", "----------------------------")
            try {
                var res: Boolean = cp.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                if (res)
                    TemporaryHandler.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                else {
                    res = cp.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    if (res)
                        TemporaryHandler.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                }
                Log.d("Trace", "Res : $res")

                if (res) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "未偵測到鏡頭", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "偵測鏡頭錯誤", Toast.LENGTH_LONG).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}