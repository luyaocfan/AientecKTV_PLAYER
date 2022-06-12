package com.aientec.ktv_vod.common.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aientec.ktv_vod.service.VodService

abstract class ActivityImpl : AppCompatActivity() {
      companion object {
            const val SERVICE_START_MODE_NONE = 0
            const val SERVICE_START_MODE_CREATE = 1
            const val SERVICE_START_MODE_BIND = 2
            const val SERVICE_STOP_WHEN_DESTROY = 4

            const val UI_MODE_SET_ON_CREATE = 0
            const val UI_MODE_SET_ON_SERVICE_CONNECTED = 1
      }

      private var isViewInitialized: Boolean = false

      private val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                  onServiceDisconnect()
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                  onServiceConnect((service as VodService.ServiceBinder).getService())
                  if (getViewInitFlag() == UI_MODE_SET_ON_SERVICE_CONNECTED && !isViewInitialized) {
                        initViews(null)
                        isViewInitialized = true
                  }
            }
      }

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            hideSystemBars()

            if (getViewInitFlag() == UI_MODE_SET_ON_CREATE && !isViewInitialized) {
                  initViews(savedInstanceState)
                  isViewInitialized = true
            }

            if (needCreateService())
                  startService(Intent(this, VodService::class.java))

      }

      override fun onDestroy() {
            super.onDestroy()
            isViewInitialized = false
            if (needStopService())
                  stopService(Intent(this, VodService::class.java))
      }

      override fun onStart() {
            super.onStart()
            if (needBindService())
                  bindService(
                        Intent(this, VodService::class.java), serviceConnection,
                        Context.BIND_AUTO_CREATE
                  )
      }

      override fun onStop() {
            super.onStop()
            if (needBindService())
                  unbindService(serviceConnection)
      }

      abstract fun initViews(savedInstanceState: Bundle?)

      abstract fun onServiceConnect(service: VodService)

      abstract fun onServiceDisconnect()

      abstract fun getServiceStartFlag(): Int

      abstract fun getViewInitFlag(): Int

      private fun needCreateService(): Boolean {
            return (getServiceStartFlag() and SERVICE_START_MODE_CREATE) == SERVICE_START_MODE_CREATE
      }

      private fun needBindService(): Boolean {
            return (getServiceStartFlag() and SERVICE_START_MODE_BIND) == SERVICE_START_MODE_BIND
      }

      private fun needStopService(): Boolean {
            return (getServiceStartFlag() and SERVICE_STOP_WHEN_DESTROY) == SERVICE_STOP_WHEN_DESTROY
      }

      private fun hideSystemBars() {
//            val windowInsetsController =
//                  ViewCompat.getWindowInsetsController(window.decorView) ?: return
//            // Configure the behavior of the hidden system bars
//            windowInsetsController.systemBarsBehavior =
//                  WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            // Hide both the status bar and the navigation bar
//            windowInsetsController.isAppearanceLightNavigationBars = false
//            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

            window.decorView.apply {
                  // Hide both the navigation bar and the status bar.
                  // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
                  // a general rule, you should design your app to hide the status bar whenever you
                  // hide the navigation bar.
                  systemUiVisibility =
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
      }
}