package com.aientec.structure

import android.os.Parcelable
import androidx.annotation.Size
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class User @JvmOverloads constructor(
    var id: Int = -1,
    var name: String = "",
    @Size(32) var token: String? = null,
    var icon: String = ""
) : Parcelable