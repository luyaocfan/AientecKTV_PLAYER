package com.aientec.ktv_vod.activity

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.viewModels
import com.aientec.ktv_vod.common.impl.ActivityImpl
import com.aientec.ktv_vod.databinding.ActivityLauncherBinding
import com.aientec.ktv_vod.dialog.DataAgentDialog
import com.aientec.ktv_vod.dialog.RoomSelectDialog
import com.aientec.ktv_vod.dialog.StoreSelectDialog
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.viewmodel.SystemViewModel


class LauncherActivity : ActivityImpl() {
      private lateinit var binding: ActivityLauncherBinding

      private val systemViewModel: SystemViewModel by viewModels()

      override fun initViews(savedInstanceState: Bundle?) {


            binding = ActivityLauncherBinding.inflate(layoutInflater)
            setContentView(binding.root)
      }


      override fun onServiceConnect(service: VodService) {
            systemViewModel.onServiceConnected(service)

            systemViewModel.readConfiguration()

            systemViewModel.configuration.observe(this) {
                  if (it == null) {
                        onFirstLauncher()
                  } else {
                        systemViewModel.systemInitialize()
                  }
            }

            systemViewModel.serviceReady.observe(this) {
                  if (it) {
                        onReady()
                  }
            }

            systemViewModel.stateMessage.observe(this) {
                  binding.log.text = it
            }


      }

      override fun onServiceDisconnect() {

      }

      override fun getServiceStartFlag(): Int {
            return SERVICE_START_MODE_CREATE or SERVICE_START_MODE_BIND
      }

      override fun getViewInitFlag(): Int {
            return UI_MODE_SET_ON_SERVICE_CONNECTED
      }

      private fun onReady() {
            startActivity(Intent(this, IdleActivity::class.java))
            finish()
      }

      private fun onFirstLauncher() {
            systemViewModel.selectedStore.observe(this) {
                  RoomSelectDialog().show(supportFragmentManager, "room_dialog")
            }

            systemViewModel.selectedRoom.observe(this) {
                  systemViewModel.onDataAgentInfoSetup("10.10.10.1", 40051)
            }

            systemViewModel.dataAgentInfo.observe(this) {
                  systemViewModel.createConfiguration()
            }

            StoreSelectDialog().show(supportFragmentManager, "store_dialog")
      }
}