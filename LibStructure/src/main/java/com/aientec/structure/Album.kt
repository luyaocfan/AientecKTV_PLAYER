package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * TODO 確認欄位
 *
 * @property id
 * @property name 專輯名稱
 * @property performer 演出者
 * @property description 描述
 * @property cover 封面Url
 */

@Parcelize
open class Album @JvmOverloads constructor(
    var id: Int = -1,
    var name: String = "",
    var performer: String = "",
    var description: String = "",
    var cover: String = ""
) : Parcelable {
    var tracks: List<Track>? = null

    //Fake parameter
    var title: String? = null
}