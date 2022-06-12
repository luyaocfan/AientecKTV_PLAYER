package com.aientec.ktv_vod

import android.app.Application
import com.aientec.ktv_vod.common.FakeTool

class VodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FakeTool.init(this)
    }
}