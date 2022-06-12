package com.aientec.ktv_vod.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.impl.ActivityImpl
import com.aientec.ktv_vod.databinding.ActivityEntranceBinding
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.aientec.ktv_vod.viewmodel.RoomViewModel

class EntranceActivity : ActivityImpl() {
    private lateinit var binding: ActivityEntranceBinding

    private val roomViewModel: RoomViewModel by viewModels()

    private val systemViewModel: SystemViewModel by viewModels()

    override fun initViews(savedInstanceState: Bundle?) {
        binding = ActivityEntranceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.skip.setOnClickListener {
            findNavController(R.id.nav_fragment_entrance).navigate(R.id.action_qrcodeFragment_to_authorizationFragment)
        }
    }

    override fun onServiceConnect(service: VodService) {
        roomViewModel.onServiceConnected(service)

        systemViewModel.onServiceConnected(service)

        systemViewModel.refreshWifiAp()
    }

    override fun onServiceDisconnect() {

    }

    override fun getServiceStartFlag(): Int {
        return SERVICE_START_MODE_BIND
    }

    override fun getViewInitFlag(): Int {
        return UI_MODE_SET_ON_SERVICE_CONNECTED
    }
}