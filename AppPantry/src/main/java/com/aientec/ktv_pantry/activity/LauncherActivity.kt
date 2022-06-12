package com.aientec.ktv_pantry.activity

import android.content.Intent
import android.widget.Toast
import com.aientec.ktv_pantry.service.PantryService
import com.aientec.ktv_pantry.viewmodel.SystemViewModel
import idv.bruce.common.impl.ActivityImpl
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl

class LauncherActivity : ActivityImpl() {
    override val autoStartService: Boolean
        get() = true
    override val viewModelList: List<Class<out ViewModelImpl>>?
        get() = listOf(SystemViewModel::class.java)
    override val permissions: Array<String>?
        get() = null
    override val serviceCls: Class<out ServiceImpl>
        get() = PantryService::class.java

    override fun initView() {
        val systemViewModel: SystemViewModel =
            getViewModel(SystemViewModel::class.java) as SystemViewModel

        systemViewModel.isReady.observe(this, {
            if (it) {
                startActivity(Intent(this, EntranceActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "系統初始化失敗", Toast.LENGTH_LONG).show()
            }
        })

        systemViewModel.initSystem()
    }

    override fun onServiceNotStart() {

    }
}