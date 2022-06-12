package com.aientec.structure

import android.graphics.Rect
import android.os.Parcelable
import androidx.annotation.Size
import kotlinx.parcelize.Parcelize

/**
 *
 *
 * @property id
 * @property name 名稱
 * @property type 類型
 * @property floor 樓層
 * @property state 狀態
 * @property rect 地圖座標
 *
 */
@Parcelize
open class Room  @JvmOverloads constructor(
    var id: Int = -1,
    var name: String = "",
    var type: Int = 0,
    var floor: Int = -1,
    var state: Int = 0,
    var rect: Rect? = null
) :
    Parcelable {

    @Parcelize
    class Type @JvmOverloads constructor(val id: Int = -1, val name: String = "") : Parcelable

    companion object {
        const val STATE_IDLE: Int = 1

        const val STATE_ON_USE: Int = 2

        const val STATE_ON_CHECKED: Int = 3

        const val STATE_ON_CLEAN: Int = 4

        const val STATE_ON_FIX: Int = 5
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Room) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}