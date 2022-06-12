package com.aientec.ktv_vod.common.structure

import android.annotation.SuppressLint
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class Configuration {

    var storeId: Int = -1

    var storeName: String = ""

    var roomId: Int = -1

    var roomName: String = ""

    var wifiSsid: String = ""

    var password: String = ""

    var dataAgentIp: String = ""

    var dataAgentPort: Int = -1

    fun readFromSharedPreferences(sharedPreferences: SharedPreferences): Boolean {
        val str: String = sharedPreferences.getString("configuration", null) ?: return false

        try {
            val jsonObject: JSONObject = JSONObject(str)

            storeId = jsonObject.getInt("store_id")

            storeName = jsonObject.getString("store_name")

            roomId = jsonObject.getInt("room_id")

            roomName = jsonObject.getString("room_name")

            dataAgentIp = jsonObject.getString("data_agent_ip")

            dataAgentPort = jsonObject.getInt("data_agent_port")
        } catch (e: JSONException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun generateRandomWifi() {
        wifiSsid = "${roomName}_${randomString(5)}"
        password = randomString(12)
    }

    @SuppressLint("ApplySharedPref")
    fun writToSharedPreferences(sharedPreferences: SharedPreferences): Boolean {
        val jsonObject: JSONObject = JSONObject().apply {

            put("store_id", storeId)

            put("store_name", storeName)

            put("room_id", roomId)

            put("room_name", roomName)

            put("data_agent_ip", dataAgentIp)

            put("data_agent_port", dataAgentPort)
        }

        return sharedPreferences.edit().putString("configuration", jsonObject.toString()).commit()
    }

    private fun randomString(length: Int): String = List(length) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}