package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Meals(
    val id: Int,
    val boxName:String,
    val orderNumber: String,
    val name: String,
    val price: Int,
    val type: Int,
    val img: String,
    val count: Int,
    val state: String,
    val time: Long
) :
    Parcelable {

    @Parcelize
    class Type(val id: Int, val name: String) : Parcelable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Meals

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}