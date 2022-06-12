package com.aientec.ktv_portal

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface WebServiceApi {

    @POST("Box/ListBranch")
    fun deviceGetStores(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBox")
    fun deviceGetRooms(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/ListBoxType")
    fun deviceGetRoomTypes(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetUserInfo")
    fun deviceGetUserInfo(@Body body: RequestBody): Call<JSONObject>

    @POST("API/ListRank")
    fun generalGetRanks(@Body body: RequestBody): Call<JSONObject>

    @POST("API/UserLogin")
    fun appLogin(@Body body: RequestBody): Call<JSONObject>

    @POST("API/ListPlaySong")
    fun appGetPlayList(@Body body: RequestBody): Call<JSONObject>

    @POST("API/AddPlayCommand")
    fun appTrackControl(@Body body: RequestBody): Call<JSONObject>

    @POST("API/EditPlaySong")
    fun appPlayListControl(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/ListPlaySong")
    fun vodGetPlayList(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/ListPublicPlaySong")
    fun vodGetIdleList(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/SetSongPlaying")
    fun vodUpdatePlayingTrack(@Body body: RequestBody): Call<JSONObject>

    @POST("VOD/PlayNextSong")
    fun vodUpdateNextTrack(@Body body: RequestBody): Call<JSONObject>

    @POST("Box/GetWorkingBox")
    fun vodGetOpenInfo(@Body body: RequestBody): Call<JSONObject>

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

    @POST("Reception/SubmitPay")
    fun staffSubmitPay(@Body body: RequestBody): Call<JSONObject>
}