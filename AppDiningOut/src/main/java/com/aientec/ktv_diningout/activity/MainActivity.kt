package com.aientec.ktv_diningout.activity

import com.aientec.ktv_diningout.databinding.ActivityMainBinding
import com.aientec.ktv_diningout.service.DiningOutService
import com.aientec.ktv_diningout.viewmodel.MealsViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl

class MainActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = false
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = listOf(MealsViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = DiningOutService::class.java

    private lateinit var binding: ActivityMainBinding

    override fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onServiceNotStart() {

    }
}