package com.aientec.ktv_diningout.service

import com.aientec.ktv_diningout.model.Repository
import idv.bruce.common.impl.ModelImpl
import idv.bruce.common.impl.ServiceImpl

class DiningOutService : ServiceImpl() {
    override val modelList: List<ModelImpl>
        get() = listOf(Repository(this))


}