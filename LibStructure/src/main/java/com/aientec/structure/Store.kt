package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Store @JvmOverloads constructor(var id: Int = -1, var name: String = "", var address: String = "") : Parcelable