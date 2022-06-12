package com.aientec.ktv_vod.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.impl.ActivityImpl
import com.aientec.ktv_vod.databinding.ActivityStyleBinding
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.viewmodel.ControlViewModel
import com.aientec.ktv_vod.viewmodel.SystemViewModel

class StyleActivity : ActivityImpl() {
      private lateinit var binding: ActivityStyleBinding

      private val controlViewModel: ControlViewModel by viewModels()

      private val systemViewModel: SystemViewModel by viewModels()

      private var selectedView: View? = null

      override fun initViews(savedInstanceState: Bundle?) {
            binding = ActivityStyleBinding.inflate(layoutInflater)

            setContentView(binding.root)

            binding.style1.setOnClickListener(onClickListener)
            binding.style2.setOnClickListener(onClickListener)
            binding.style3.setOnClickListener(onClickListener)
            binding.style4.setOnClickListener(onClickListener)
            binding.style5.setOnClickListener(onClickListener)
      }


      override fun onServiceConnect(service: VodService) {
            controlViewModel.onServiceConnected(service)

            systemViewModel.onServiceConnected(service)

            systemViewModel.isOpen.observe(this) {
                  if (it == false) {
                        startActivity(Intent(this, IdleActivity::class.java))
                        finish()
                  }
            }
      }

      override fun onServiceDisconnect() {

      }

      override fun getServiceStartFlag(): Int {
            return SERVICE_START_MODE_BIND
      }

      override fun getViewInitFlag(): Int {
            return UI_MODE_SET_ON_SERVICE_CONNECTED
      }

      private fun onSelect(view: View) {
            if (selectedView != null)
                  selectedView!!.setBackgroundResource(R.drawable.bg_style_btn)
            selectedView = view
            selectedView!!.setBackgroundResource(R.drawable.bg_style_btn_highlight)

            when (selectedView) {
                  binding.style1 -> controlViewModel.onMainModeSelected(
                        1,
                        "${resources.getString(R.string.mode_1)}模式"
                  )
                  binding.style2 -> controlViewModel.onMainModeSelected(
                        2,
                        "${resources.getString(R.string.mode_2)}模式"
                  )
                  binding.style3 -> controlViewModel.onMainModeSelected(
                        3,
                        "${resources.getString(R.string.mode_3)}模式"
                  )
                  binding.style4 -> controlViewModel.onMainModeSelected(
                        4,
                        "${resources.getString(R.string.mode_4)}模式"
                  )
                  binding.style5 -> controlViewModel.onMainModeSelected(
                        5,
                        "${resources.getString(R.string.mode_5)}模式"
                  )
            }
      }

      private fun onDone() {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
      }


      private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
            if (v == selectedView) {
                  onDone()
            } else {
                  onSelect(v!!)
            }
      }
}