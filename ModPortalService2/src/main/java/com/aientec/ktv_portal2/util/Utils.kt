package com.aientec.ktv_portal2.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

internal fun String.toMd5():String{
    try {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")//獲取md5加密對象
        val digest: ByteArray = instance.digest(this.toByteArray(Charsets.UTF_8))//對字符串加密，返回字節數組
        val sb: StringBuffer = StringBuffer()
        for (b in digest) {
            val i: Int = b.toInt() and 0xff//獲取低八位有效值
            var hexString = Integer.toHexString(i)//將整數轉化爲16進制
            if (hexString.length < 2) {
                hexString = "0$hexString"//如果是一位的話，補0
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}

internal fun JSONObject.toRequestBody():RequestBody{
    return this.toString().toRequestBody("application/json".toMediaTypeOrNull())
}