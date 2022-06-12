package com.aientec.ktv_portal2.handler

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

abstract class ApiHandler<T> internal constructor(protected var apiInstance: T) {
    abstract val tag: String

    protected companion object {
        const val RESPONSE_STATUS = "Status"
        const val RESPONSE_MESSAGE = "Message"

        const val STATUS_SUCCESS = 0
    }

    protected sealed class ApiCallback{
        data class Complete(val resObj:JSONObject):ApiCallback()
        data class Error(val msg:String):ApiCallback()
    }

    protected val dateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)

    protected fun apiExecute(
        body: RequestBody,
        api: (input: RequestBody) -> Call<JSONObject>
    ):ApiCallback {
        val call: Call<JSONObject> = api(body)

        val response: Response<JSONObject>

        return try {
            response = call.execute()

            val resObj: JSONObject = response.body() ?: throw IOException("body is null")

            val status = resObj.getInt(RESPONSE_STATUS)

            if (status == STATUS_SUCCESS)
                ApiCallback.Complete(resObj)
            else
                ApiCallback.Error(resObj.getString(RESPONSE_MESSAGE))

        } catch (e: Exception) {
            ApiCallback.Error(e.message ?: "")
        }
    }
}