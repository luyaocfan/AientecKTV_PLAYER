package com.aientec.ktv_da.util

private val hexArray: CharArray = "0123456789ABCDEF".toCharArray()
internal fun ByteArray.toHex():String{
    val hexChars = CharArray(this.size * 3)
    for (j in this.indices) {
        val v = this[j].toInt() and 0xFF

        hexChars[j * 3] = hexArray[v ushr 4]
        hexChars[j * 3 + 1] = hexArray[v and 0x0F]
        hexChars[j * 3 + 2] = ' '
    }
    return String(hexChars)
}