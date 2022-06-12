package com.aientec.ktv_vod.common

import android.content.Context
import com.aientec.ktv_vod.R
import kotlin.math.round

object FakeTool {
    lateinit var trackLogos: Array<String>

    const val FILE_ROOT : String = "http://106.104.151.145:10001/"

    fun init(context: Context) {
        trackLogos = context.resources.getStringArray(R.array.trac_logo_res)
    }

    fun getRndUrl(): String {
        val index: Int = round(Math.random() * 9).toInt()
        return trackLogos[index]
    }


}