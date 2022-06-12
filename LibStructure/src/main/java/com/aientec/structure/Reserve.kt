package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

@Parcelize
class Reserve(
    val id: Int,
    val storeName: String,
    val memberId: Int,
    val code: String,
    val memberName: String,
    val memberPhone: String,
    val time: String,
    val checkInTime: String,
    val personCount: Int
) : Parcelable {
    companion object {
        private const val timeFormat: String = "yyyy-MM-dd HH:mm:ss"
        private val dateFormat: SimpleDateFormat = SimpleDateFormat(timeFormat, Locale.TAIWAN)
    }

    val timestamp: Long
        get() {
            if (time.isEmpty()) return -1L

            val date: Date = dateFormat.parse(time) ?: return -1L

            return date.time
        }

    val checkInTimestamp: Long
        get() {
            if (checkInTime.isEmpty()) return -1L

            val date: Date = dateFormat.parse(checkInTime) ?: return -1L

            return date.time
        }
}