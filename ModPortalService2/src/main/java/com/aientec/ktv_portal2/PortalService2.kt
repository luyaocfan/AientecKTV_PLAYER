package com.aientec.ktv_portal2

import android.content.Context
import com.aientec.ktv_portal2.api.CommonWebServiceApi
import com.aientec.ktv_portal2.api.CustomerWebServiceApi
import com.aientec.ktv_portal2.api.StoreWebServiceApi
import com.aientec.ktv_portal2.factory.ConvertFactory
import com.aientec.ktv_portal2.handler.CommonApiHandler
import com.aientec.ktv_portal2.handler.CustomerApiHandler
import com.aientec.ktv_portal2.handler.StoreApiHandler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.lang.ref.WeakReference

class PortalService2 private constructor(context: Context) {
      companion object {
            private const val DEFAULT_API_ROOT: String = "http://106.104.151.145:10005/index.php/"

            var apiRoot: String = DEFAULT_API_ROOT

            private var instance: PortalService2? = null

            @Synchronized
            fun getInstance(context: Context): PortalService2 =
                  instance ?: PortalService2(context).also {
                        instance = it
                  }
      }

      private val contextRef: WeakReference<Context> = WeakReference(context)

      lateinit var Store: StoreApiHandler

      lateinit var Common: CommonApiHandler

      lateinit var Customer: CustomerApiHandler

      var debug: Boolean = true

      fun init() {
            val client: OkHttpClient = if (debug) {
                  val logInterceptor = HttpLoggingInterceptor()
                  logInterceptor.level = HttpLoggingInterceptor.Level.BODY

                  OkHttpClient()
                        .newBuilder()
                        .addInterceptor(logInterceptor)
                        .build()
            } else {
                  OkHttpClient().newBuilder().build()
            }

            val retrofit =
                  Retrofit
                        .Builder()
                        .baseUrl(apiRoot)
                        .addConverterFactory(ConvertFactory())
                        .client(client)
                        .build()


            Store = (StoreApiHandler(retrofit.create(StoreWebServiceApi::class.java)))
            Customer = (CustomerApiHandler(retrofit.create(CustomerWebServiceApi::class.java)))
            Common = (CommonApiHandler(retrofit.create(CommonWebServiceApi::class.java)))
      }
}