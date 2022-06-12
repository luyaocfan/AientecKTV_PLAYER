package com.aientec.ktv_pantry.service

import com.aientec.ktv_pantry.model.Hardware
import com.aientec.ktv_pantry.model.Repository
import idv.bruce.common.impl.ModelImpl
import idv.bruce.common.impl.ServiceImpl

class PantryService : ServiceImpl() {
    override val modelList: List<ModelImpl>
        get() = listOf(Repository(this), Hardware.getInstance(this))

}