package com.aientec.ktv_staff


import com.aientec.ktv_staff.databinding.ActivityMainBinding
import com.aientec.ktv_staff.service.StaffService
import com.aientec.ktv_staff.viewmodel.RoomViewModel
import com.aientec.ktv_staff.viewmodel.UserViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl

class MainActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = true
    override val viewModelList: List<Class<out ViewModelImpl>>
        get() = arrayListOf(RoomViewModel::class.java, UserViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = StaffService::class.java

    private lateinit var binding: ActivityMainBinding

    override fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onServiceNotStart() {
        finish()
    }
}