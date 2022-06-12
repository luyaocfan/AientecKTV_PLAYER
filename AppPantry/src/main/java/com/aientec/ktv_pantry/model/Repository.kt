package com.aientec.ktv_pantry.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_portal.PortalResponse
import com.aientec.ktv_portal.PortalService
import com.aientec.structure.Meals
import com.aientec.structure.User
import idv.bruce.common.impl.ModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

class Repository(context: Context) : ModelImpl(context) {
    private val portalService: PortalService = PortalService.getInstance(context)

    private var user: User? = null

    private val updateThread: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var updateFuture: Future<*>? = null

    val mealsList: MutableLiveData<List<Meals>> = MutableLiveData()

    override val tag: String
        get() = "Repo"

    override fun init() {
        portalService.init()
    }

    override fun release() {

    }

    suspend fun login(account: String, password: String): User? = withContext(Dispatchers.IO) {
        val portalResponse: PortalResponse = portalService.staffLogin(account, password)

        return@withContext if (portalResponse.status == PortalResponse.STATUS_OK) {
            user = portalResponse.data as User

            user
        } else {
            null
        }
    }

    suspend fun startUpdate() = withContext(Dispatchers.IO) {
        updateFuture = updateThread.scheduleAtFixedRate(updateRunnable, 0, 10, TimeUnit.SECONDS)
    }

    suspend fun stopUpdate() = withContext(Dispatchers.IO) {
        if (updateFuture != null || !updateFuture!!.isCancelled)
            updateFuture!!.cancel(true)
        updateFuture = null
    }

    private fun getMealsList(): List<Meals>? {
        val portalResponse: PortalResponse =
            portalService.staffListProcessingMeals(user?.token ?: return null)

        return if (portalResponse.status == PortalResponse.STATUS_OK) {
            (portalResponse.data as List<*>).filterIsInstance(Meals::class.java)
        } else
            null
    }

    private val updateRunnable: Runnable = Runnable {
        mealsList.postValue(getMealsList())
    }
}