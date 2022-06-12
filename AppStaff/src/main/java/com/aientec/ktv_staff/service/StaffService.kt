package com.aientec.ktv_staff.service

import com.aientec.ktv_staff.model.Repository
import idv.bruce.common.impl.ModelImpl
import idv.bruce.common.impl.ServiceImpl

class StaffService : ServiceImpl() {
    override val modelList: ArrayList<ModelImpl>?
        get() = arrayListOf(Repository(this))

}