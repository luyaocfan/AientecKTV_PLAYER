package com.aientec.ktv_pos_tablet.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.viewModels
import com.aientec.ktv_pos_tablet.databinding.ActivityLauncherBinding
import com.aientec.ktv_pos_tablet.fragment.dialog.ConfigurationDialog
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.ktv_pos_tablet.viewmodel.SystemViewModel

class LauncherActivity : AppCompatActivity() {
    private val systemViewModel: SystemViewModel by viewModels()

    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hide()

        binding = ActivityLauncherBinding.inflate(layoutInflater)

        setContentView(binding.root)

        startKtvService()

        systemViewModel.stateMessage.observe(this, {
            binding.log.text = it
        })

        systemViewModel.isDataSyn.observe(this, {
            if (it)
                onInitialDone()
        })

        systemViewModel.configuration.observe(this, {
            if (it == null)
                showConfigDialog()
            else
                systemViewModel.dataSyn()
        })
    }

    private fun startKtvService() {
        val intent: Intent = Intent(this, KTVService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun hide() {
        runOnUiThread {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    private fun onInitialDone() {
        val intent = Intent(this, EntranceActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
        finish()
    }


    private fun showConfigDialog() {
        ConfigurationDialog().show(supportFragmentManager, "config")
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service: KTVService = (binder as KTVService.ServiceBinder).service
            service.init()
            systemViewModel.onServiceConnected(service)
//            onInitialDone()
//            systemViewModel.dataSyn()
            systemViewModel.updateConfiguration()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
}