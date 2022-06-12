package com.aientec.ktv_diningout.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_diningout.common.MealsGroup
import com.aientec.ktv_portal.PortalResponse
import com.aientec.ktv_portal.PortalService
import com.aientec.structure.Meals
import com.aientec.structure.User
import idv.bruce.common.impl.ModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Repository(context: Context) : ModelImpl(context) {
    private val portalService: PortalService = PortalService.getInstance(context)

    private var user: User? = null

    private val updateThread: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var updateFuture: Future<*>? = null

    val mealsGroupList: MutableLiveData<List<MealsGroup>> = MutableLiveData()

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
        stopUpdate()
        updateFuture = updateThread.scheduleAtFixedRate(updateRunnable, 0, 30, TimeUnit.SECONDS)
    }

    suspend fun stopUpdate() = withContext(Dispatchers.IO) {
        if (updateFuture != null || updateFuture?.isCancelled == false)
            updateFuture!!.cancel(true)
        updateFuture = null
    }

    suspend fun mealsDone(mealsList: List<Meals>): Boolean = withContext(Dispatchers.IO) {
        var ids: String = ""

        for (item in mealsList)
            ids += ",${item.id}"

        ids = ids.removePrefix(",")
        val response: PortalResponse =
            portalService.staffMealsDone(user?.token ?: return@withContext false, ids)

        return@withContext response.status == PortalResponse.STATUS_OK
    }

    suspend fun mealsPicUpload(mealsList: List<Meals>, imgFile: String): Boolean =
        withContext(Dispatchers.IO) {
            var ids: String = ""

            for (item in mealsList)
                ids += ",${item.id}"

            ids = ids.removePrefix(",")

            val response: PortalResponse = portalService.staffUploadMealsPic(
                user?.token ?: return@withContext false,
                ids,
                imgFile
            )

            return@withContext response.status == PortalResponse.STATUS_OK
        }

    private fun getMealsList(): List<Meals>? {
        val portalResponse: PortalResponse =
            portalService.staffListProcessingMeals(user?.token ?: return null)

        return if (portalResponse.status == PortalResponse.STATUS_OK) {
            (portalResponse.data as List<*>).filterIsInstance(Meals::class.java)
        } else
            null
    }

    private fun updateMealsGroup(list: List<Meals>) {
        val mMealsGroupList: ArrayList<MealsGroup> = ArrayList()

        val mList: ArrayList<Meals> = ArrayList(list)

        while (mList.isNotEmpty()) {

            val item: Meals = mList[0]

            val mMealsGroup: MealsGroup = MealsGroup(item.orderNumber, item.boxName).apply {
                val subList: List<Meals> = mList.filter {
                    it.orderNumber == item.orderNumber
                }
                this.mealsList.addAll(subList)
                mList.removeAll(subList.toSet())
            }

            mMealsGroupList.add(mMealsGroup)
        }

        mealsGroupList.postValue(mMealsGroupList)
    }


    private val updateRunnable: Runnable = Runnable {
        updateMealsGroup(getMealsList() ?: return@Runnable)
    }
}