package com.aientec.ktv_pos_tablet.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.navigation.findNavController
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.ActivityEntranceBinding
import com.aientec.ktv_pos_tablet.databinding.ActivityHostBinding
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.ktv_pos_tablet.viewmodel.UserViewModel
import com.aientec.ktv_pos_tablet.viewmodel.ViewModelImpl

class EntranceActivity : AppCompatActivity() {
    private var isViewInit: Boolean = false

    private lateinit var binding: ActivityEntranceBinding

    private val userViewModel: UserViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, KTVService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun initView() {
        if (isViewInit) return

        binding = ActivityEntranceBinding.inflate(layoutInflater)

        setContentView(binding.root)



        isViewInit = true
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {

            val service: KTVService = (binder as KTVService.ServiceBinder).service
            userViewModel.onServiceConnected(service)
            initView()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
}