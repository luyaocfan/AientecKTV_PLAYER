package com.aientec.ktv_player2.activity

import android.util.Log
import com.aientec.ktv_player2.databinding.ActivityLauncherBinding
import com.aientec.ktv_player2.service.KtvService
import com.aientec.ktv_player2.viewmodel.SystemViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import idv.bruce.common.tools.MediaTools

class LauncherActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = true
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = listOf(SystemViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = KtvService::class.java

    private lateinit var binding: ActivityLauncherBinding

    override fun initView() {
        binding = ActivityLauncherBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onServiceNotStart() {

    }
}