package com.aientec.ktv_portal2.api

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface StoreWebServiceApi {

    @POST("Box/ListBranch")
    fun getStoreList(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBox")
    fun getRoomList(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBoxType")
    fun getRoomTypeList(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetUserInfo")
    fun getUserInfo(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/ListPublicPlaySong")
    fun getIdleTrackList(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetBOxInfo")
    fun getOpenInfo(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/UserLogin")
    fun actionLogin(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/ListReserve")
    fun getReserveList(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/SignConfirm")
    fun actionReserveCheckIn(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/CancelReserve")
    fun actionReserveCancel(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/OpenBox")
    fun actionOpenBox(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/StartBox")
    fun actionStartTimer(@Body body: RequestBody): Call<JSONObject>

    @POST("Reception/CloseBox")
    fun actionCloseBox(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/ListPendingFoods")
    fun getProcessingMealsList(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/FinishCook")
    fun actionMealsDone(@Body body: RequestBody): Call<JSONObject>

    @POST("Orders/DeliveryFoods")
    fun actionUploadMealsPic(@Body body: RequestBody): Call<JSONObject>
}