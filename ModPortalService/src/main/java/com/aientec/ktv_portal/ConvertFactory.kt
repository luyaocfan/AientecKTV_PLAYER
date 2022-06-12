package com.aientec.ktv_portal

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.Exception
import java.lang.reflect.Type

internal class ConvertFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<out Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *> {
        return PortalResponseConverter()
    }

    private class PortalResponseConverter : Converter<ResponseBody, JSONObject> {
        override fun convert(value: ResponseBody): JSONObject? {
            return try {
                JSONObject(value.string())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}