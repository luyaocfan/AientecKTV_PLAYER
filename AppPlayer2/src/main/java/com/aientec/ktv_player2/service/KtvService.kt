package com.aientec.ktv_player2.service

import com.aientec.ktv_player2.module.OsdDataModel
import com.aientec.ktv_player2.module.PlayerDataModel
import idv.bruce.common.impl.ModelImpl
import idv.bruce.common.impl.ServiceImpl

class KtvService : ServiceImpl() {
    override val modelList: List<ModelImpl>
        get() = listOf(OsdDataModel(this), PlayerDataModel(this))
}