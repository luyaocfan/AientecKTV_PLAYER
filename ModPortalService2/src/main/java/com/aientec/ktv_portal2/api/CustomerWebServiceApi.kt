package com.aientec.ktv_portal2.api

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CustomerWebServiceApi {

    @POST("Box/ListBranch")
    fun staffGetStores(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBox")
    fun staffGetRooms(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBoxType")
    fun staffGetRoomTypes(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetUserInfo")
    fun staffGetUserInfo(@Body body: RequestBody): Call<JSONObject>

    @POST("API/ListRank")
    fun generalGetRanks(@Body body: RequestBody): Call<JSONObject>

    @POST("API/UserLogin")
    fun appLogin(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/ListPublicPlaySong")
    fun staffGetIdleList(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetWorkingBox")
    fun staffGetOpenInfo(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/UserLogin")
    fun staffLogin(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/ListReserve")
    fun staffListReserve(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/SignConfirm")
    fun staffReserveCheckIn(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/CancelReserve")
    fun staffReserveCancel(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/OpenBox")
    fun staffOpenBox(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/StartBox")
    fun staffStartTime(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/CloseBox")
    fun staffCloseBox(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/ListPendingFoods")
    fun staffListProcessingMeals(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/FinishCook")
    fun staffMealsDone(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/DeliveryFoods")
    fun staffUploadMealsPic(@Body body: RequestBody): Call<JSONObject>
}