package com.aientec.ktv_portal2.api

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CommonWebServiceApi {
    @POST("API/ListRank")
    fun getRanks(@Body body: RequestBody): Call<JSONObject>
}