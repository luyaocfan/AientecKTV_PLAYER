package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class OpenInfo(
    val status: Int,
    val memberId: Int,
    val memberName: String,
    val memberIcon: String
) : Parcelable