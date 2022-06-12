package com.aientec.ktv_pantry.activity

import com.aientec.ktv_pantry.databinding.ActivityEntranceBinding
import com.aientec.ktv_pantry.service.PantryService
import com.aientec.ktv_pantry.viewmodel.UserViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl

class EntranceActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = false
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = listOf(UserViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = PantryService::class.java

    private lateinit var binding: ActivityEntranceBinding

    override fun initView() {
        binding = ActivityEntranceBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onServiceNotStart() {

    }
}