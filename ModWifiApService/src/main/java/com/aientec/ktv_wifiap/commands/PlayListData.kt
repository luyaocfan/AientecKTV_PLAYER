package com.aientec.ktv_wifiap.commands

import android.util.Log
import com.aientec.structure.Track
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer

class PlayListData : DSData() {
    override val command: CommandList
        get() = CommandList.TRACKS_UPDATE

    var list: List<Track> = ArrayList()

    override fun encodeBody(buffer: ByteBuffer) {

    }

    override fun decodeBody(body: ByteBuffer) {
        val payloadLength: Int = length.toInt() - 8

        val bytes: ByteArray = ByteArray(payloadLength)

        body.get(bytes)

        val jsonString: String = String(bytes, Charsets.UTF_8)

        try {

            val jsonObj: JSONObject = JSONObject(jsonString)

            val listObj: JSONArray = jsonObj.getJSONArray("SongList")

            val count: Int = listObj.length()

            var itemObj: JSONObject

            var track: Track


            Log.d("Trace", "PlayList : ${listObj.toString()}")

            for (i in 0 until count) {
                itemObj = listObj.getJSONObject(i)

                track = Track().apply {
                    id = itemObj.getInt("SongId")
                    sn = itemObj.getString("SongNo")
                    name = itemObj.getString("SongName")
                    performer = itemObj.getString("Singer")
                    imageUrl = itemObj.getString("SongImg")
                    fileName = itemObj.getString("SongFile")
                    bpm = itemObj.getInt("BPMClass")
                    state = Track.State.values().find {
                        it.code == itemObj.getInt("Status")
                    } ?: Track.State.NONE
                }

                (list as ArrayList<Track>).add(track)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}