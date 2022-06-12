package com.aientec.ktv_player2.activity

import com.aientec.ktv_player2.databinding.ActivityMainBinding
import com.aientec.ktv_player2.service.KtvService
import com.aientec.ktv_player2.viewmodel.PlayerViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl

class MainActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = true
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = listOf(PlayerViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = KtvService::class.java

    private lateinit var binding: ActivityMainBinding



    override fun initView() {

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onServiceNotStart() {

    }
}