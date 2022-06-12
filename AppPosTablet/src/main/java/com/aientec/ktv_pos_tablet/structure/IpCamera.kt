package com.aientec.ktv_pos_tablet.structure

import android.graphics.Point

class IpCamera(
    var id: Int = -1,
    var name: String = "",
    var host: String = "",
    var floor: Int = 0,
    var position: Point? = null
)