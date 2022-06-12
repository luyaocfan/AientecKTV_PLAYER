package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Account(var username: String, var password: String) : Parcelable