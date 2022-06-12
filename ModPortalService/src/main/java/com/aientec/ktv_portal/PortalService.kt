package com.aientec.ktv_portal

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.annotation.IntDef
import com.aientec.structure.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PortalService private constructor(context: Context) {
    companion object {
        @IntDef(
            FLAG_RESERVE_STATUS_ALL,
            FLAG_RESERVE_STATUS_CHECKED,
            FLAG_RESERVE_STATUS_UNCHECKED
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class StatusInt

        const val FLAG_RESERVE_STATUS_ALL: Int = -1
        const val FLAG_RESERVE_STATUS_CHECKED: Int = 2
        const val FLAG_RESERVE_STATUS_UNCHECKED: Int = 1

        private const val RESPONSE_STATUS = "Status"
        private const val RESPONSE_MESSAGE = "Message"

        private const val STATUS_SUCCESS = 0

        private var instance: PortalService? = null

        fun getInstance(context: Context): PortalService {
            if (instance == null)
                instance = PortalService(context)
            return instance!!
        }

        private val dateFormat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
    }


    private val contextRef: WeakReference<Context> = WeakReference(context)

    var apiRoot: String = "http://106.104.151.145:10005/index.php/"

    private lateinit var retrofit: Retrofit

    private lateinit var service: WebServiceApi

    private var isInit: Boolean = false

    fun init() {
        if (isInit) return
        val client: OkHttpClient = if (BuildConfig.DEBUG) {
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY

            OkHttpClient()
                .newBuilder()
                .addInterceptor(logInterceptor)
                .build()
        } else {
            OkHttpClient().newBuilder().build()
        }

        retrofit =
            Retrofit
                .Builder()
                .baseUrl(apiRoot)
                .addConverterFactory(ConvertFactory())
                .client(client)
                .build()

        service = retrofit.create(WebServiceApi::class.java)

        isInit = true
    }

    fun deviceGetStores(): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject()

        val call: Call<JSONObject> = service.deviceGetStores(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val objArr: JSONArray = resObj.getJSONArray("BranchList")

                val count: Int = objArr.length()

                val list: ArrayList<Store> = ArrayList()

                for (index in 0 until count) {
                    val obj: JSONObject = objArr.getJSONObject(index)

                    list.add(
                        Store(
                            obj.getInt("BranchId"),
                            obj.getString("BranchName"),
                            obj.getString("BranchAddress")
                        )
                    )
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }
        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun deviceGetRooms(storeId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            this.put("branchId", storeId)
        }

        val call: Call<JSONObject> = service.deviceGetRooms(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)

            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val objArr: JSONArray = resObj.getJSONArray("BoxList")

                val count: Int = objArr.length()

                val list: ArrayList<Room> = ArrayList()

                for (index in 0 until count) {
                    val obj: JSONObject = objArr.getJSONObject(index)

                    list.add(
                        Room(
                            obj.getInt("Id"),
                            obj.getString("BoxName"),
                            obj.getInt("BoxType"),
                            obj.getInt("BoxFloor"),
                            obj.getInt("BoxStatus")
                        )
                    )
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }
        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun deviceGetTypes(): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject()

        val call: Call<JSONObject> = service.deviceGetRoomTypes(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val objArr: JSONArray = resObj.getJSONArray("TypeList")

                val count: Int = objArr.length()

                val list: ArrayList<Room.Type> = ArrayList()

                for (index in 0 until count) {
                    val obj: JSONObject = objArr.getJSONObject(index)
                    list.add(
                        Room.Type(
                            obj.getInt("Id"),
                            obj.getString("TypeName")
                        )
                    )
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }
        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun appLogin(account: String, password: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("userCode", account)
            put("userPwd", md5(password))
        }

        val call: Call<JSONObject> = service.appLogin(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                resObj.getJSONObject("MemberInfo").apply {
                    val user: User = User(
                        getInt("MemberId"), getString("MemberName"), resObj.getString("Token")
                    )

                    portalResponse.data = user
                }
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun deviceGetUserInfo(token: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", token)
        }

        val call: Call<JSONObject> = service.deviceGetUserInfo(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                resObj.getJSONObject("MemberInfo").apply {
                    val user: User = User(
                        getInt("MemberId"), getString("MemberName")
                    )

                    portalResponse.data = user
                }
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun deviceDownloadDatabase(savePath: String): PortalResponse {
        val response: PortalResponse = PortalResponse()
        response.status = PortalResponse.STATUS_SERVER_ERROR

        val assetManager: AssetManager = contextRef.get()?.assets ?: return response

        val inStream: InputStream

        try {
            inStream = assetManager.open("ktv.db")
        } catch (e: IOException) {
            e.printStackTrace()
            return response
        }

        try {
            val outStream: FileOutputStream = FileOutputStream(savePath, false)

            val count: Long = inStream.copyTo(outStream)

            Log.d("Trace", "Copy : $count bytes")

            outStream.close()
            inStream.close()

            response.status = PortalResponse.STATUS_OK
            response.data = savePath
        } catch (e: Exception) {
            e.printStackTrace()
            return response
        }

        return response
    }


    fun staffLogin(account: String, password: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("userCode", account)
            put("userPwd", md5(password))
        }

        val call: Call<JSONObject> = service.staffLogin(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val user: User = User(0, resObj.getString("UserName"), resObj.getString("Token"))

                portalResponse.data = user
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffListReserve(
        token: String,
        storeId: Int,
        @StatusInt status: Int = FLAG_RESERVE_STATUS_ALL
    ): PortalResponse {
        val portalResponse = PortalResponse()

        val flag: String = when (status) {
            FLAG_RESERVE_STATUS_ALL -> "1,2"
            FLAG_RESERVE_STATUS_CHECKED -> "2"
            FLAG_RESERVE_STATUS_UNCHECKED -> "1"
            else -> {
                portalResponse.status = PortalResponse.STATUS_JSON_ERROR
                portalResponse.data = "Status value error"
                return portalResponse
            }
        }


        val jsonObject = JSONObject().apply {
            put("token", token)
            put("branchId", storeId)
            put("states", flag)
        }

        val call: Call<JSONObject> = service.staffListReserve(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                val list: ArrayList<Reserve> = ArrayList()

                portalResponse.status = PortalResponse.STATUS_OK

                val arrObj: JSONArray = resObj.getJSONArray("ReserveList")

                val length: Int = arrObj.length()

                var itemObj: JSONObject

                for (i in 0 until length) {
                    itemObj = arrObj.getJSONObject(i)

                    val reserve: Reserve = Reserve(
                        itemObj.getInt("Id"),
                        "",
                        itemObj.getInt("MemberId"),
                        itemObj.getString("ReserveNo"),
                        itemObj.getString("ReserveName"),
                        itemObj.getString("ReservePhone"),
                        itemObj.getString("ReserveTime"),
                        if (!itemObj.isNull("SignTime")) itemObj.getString("SignTime") else "",
                        itemObj.getInt("PersonNum")
                    )
                    list.add(reserve)
                }
                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffReserveCheckIn(token: String, reserveId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("reserveId", reserveId)
        }

        val call: Call<JSONObject> = service.staffReserveCheckIn(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffReserveCancel(token: String, reserveId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("reserveId", reserveId)
        }

        val call: Call<JSONObject> = service.staffReserveCancel(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffOpenBox(
        token: String,
        boxId: Int,
        reserveId: Int,
        type: Int,
        duration: Int,
        startTime: Long
    ): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("boxId", boxId)
            put("reserveId", reserveId)
            put("timeType", type)
            put("timeLen", duration)
            put("startTime", dateFormat.format(Date(startTime)))
        }

        val call: Call<JSONObject> = service.staffOpenBox(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffSubmitPay(
        token: String,
        boxId: Int,
        memberId: Int,
        price: Int,
        remark: String
    ): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("boxId", boxId)
            put("memberId", memberId)
            put("settleValue", price)
            put("remark", remark)
        }

        val call: Call<JSONObject> = service.staffSubmitPay(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffCloseBox(
        token: String,
        boxId: Int
    ): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("boxId", boxId)
        }

        val call: Call<JSONObject> = service.staffCloseBox(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffListProcessingMeals(token: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
        }

        val call: Call<JSONObject> = service.staffListProcessingMeals(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val listObj: JSONArray = resObj.getJSONArray("FoodList")

                val length: Int = listObj.length()

                var meals: Meals

                val list: ArrayList<Meals> = ArrayList()

                for (i in 0 until length) {
                    listObj.getJSONObject(i).apply {
                        meals = Meals(
                            this.getInt("Id"),
                            this.getString("BoxName"),
                            this.getString("OrderNo"),
                            this.getString("FoodName"),
                            this.getInt("FoodPrice"),
                            -1,
                            "",
                            this.getInt("FoodNum"),
                            this.getString("FoodState"),
                            dateFormat.parse(this.getString("SubmitTime"))?.time ?: 0L
                        )

                        list.add(meals)
                    }
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffMealsDone(token: String, ids: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObject = JSONObject().apply {
            put("token", token)
            put("orderIds", ids)
        }

        val call: Call<JSONObject> = service.staffMealsDone(
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun staffUploadMealsPic(token: String, ids: String, filePath: String): PortalResponse {
        val portalResponse = PortalResponse()

        val imageFile: File = File(filePath)

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("token", token)
            .addFormDataPart("orderIds", ids)
            .addFormDataPart(
                "photo", imageFile.name,
                imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            .build()


        val call: Call<JSONObject> = service.staffUploadMealsPic(requestBody)

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }


    fun vodGetOpenInfo(token: String, boxId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", token)
            put("boxId", boxId)
        }

        val call: Call<JSONObject> = service.vodGetOpenInfo(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val info: OpenInfo = OpenInfo(
                    resObj.getInt("OrderId"),
                    resObj.getInt("TimeLen"),
                    resObj.getString("MemberName"),
                    resObj.getString("MemberIcon")
                )

                portalResponse.data = info
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun vodStartTimer(token: String, orderId: Int, startTime: Long): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", token)
            put("orderId", orderId)
            put("startTime", dateFormat.format(Date(startTime)))
        }

        val call: Call<JSONObject> = service.staffStartTime(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun vodGetPlayList(token: String, boxId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", "")
            put("boxId", boxId)
        }

        val call: Call<JSONObject> = service.vodGetPlayList(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val listObjArray: JSONArray = resObj.getJSONArray("SongList")

                val count: Int = listObjArray.length()

                val list: ArrayList<Track> = ArrayList()

                for (i in 0 until count) {
                    val trackObj: JSONObject = listObjArray.getJSONObject(i)
                    list.add(Track(
                        trackObj.getInt("Id"),
                        trackObj.getString("SongFile").split(".")[0],
                        trackObj.getString("SongName"),
                        trackObj.getString("Singer"),
                        "",
                        "",
                        "",
                        trackObj.getString("SongImg"),
                        Track.State.values().find { it.code == trackObj.getInt("PlayStatus") }
                            ?: Track.State.NONE,
                        trackObj.getString("PlayStatusName"),
                        trackObj.getString("SongFile").split(".")[1]
                    ))
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = resObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun vodGetIdleTracks(token: String): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", "")
        }

        val call: Call<JSONObject> = service.vodGetIdleList(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK

                val listObjArray: JSONArray = resObj.getJSONArray("SongList")

                val count: Int = listObjArray.length()

                val list: ArrayList<Track> = ArrayList()

                for (i in 0 until count) {
                    val trackObj: JSONObject = listObjArray.getJSONObject(i)

                    list.add(
                        Track(
                            trackObj.getInt("Id"),
                            trackObj.getString("SongFile").split(".")[0],
                            trackObj.getString("SongName"),
                            trackObj.getString("Singer"),
                            "",
                            "",
                            "",
                            trackObj.getString("SongImg"),
                            Track.State.NONE,
                            "",
                            trackObj.getString("SongFile").split(".")[1]
                        )
                    )
                }

                portalResponse.data = list
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun vodUpdatePlayingTrack(token: String, roomId: Int, trackId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", token)
            put("boxId", roomId)
            put("playId", trackId)
        }

        val call: Call<JSONObject> = service.vodUpdatePlayingTrack(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    fun vodUpdateNextTrack(token: String, roomId: Int, trackId: Int): PortalResponse {
        val portalResponse = PortalResponse()

        val jsonObj = JSONObject().apply {
            put("token", token)
            put("boxId", roomId)
            put("playId", trackId)
        }

        val call: Call<JSONObject> = service.vodUpdateNextTrack(
            jsonObj.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
        )

        val response: Response<JSONObject>

        try {
            response = call.execute()

            val resObj = response.body() ?: throw IOException("Response body is null")

            val status = resObj.getInt(RESPONSE_STATUS)


            if (status == STATUS_SUCCESS) {
                portalResponse.status = PortalResponse.STATUS_OK
                portalResponse.message = jsonObj.getString(RESPONSE_MESSAGE)
            } else {
                portalResponse.status = PortalResponse.STATUS_API_ERROR

                portalResponse.message = when (status) {
                    2 -> "參數不正確"
                    3 -> "撥放已結束"
                    6 -> "包廂不存在"
                    else -> "未知錯誤"
                }
            }

        } catch (e: IOException) {
            portalResponse.status = PortalResponse.STATUS_SERVER_ERROR
            portalResponse.message = e.message.toString()
        } catch (e: JSONException) {
            portalResponse.status = PortalResponse.STATUS_JSON_ERROR
            portalResponse.message = e.message.toString()
        }

        return portalResponse
    }

    private fun md5(str: String): String {
        try {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")//獲取md5加密對象
            val digest: ByteArray = instance.digest(str.toByteArray(Charsets.UTF_8))//對字符串加密，返回字節數組
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
}