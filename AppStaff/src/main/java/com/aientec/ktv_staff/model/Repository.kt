package com.aientec.ktv_staff.model

import android.content.Context
import com.aientec.ktv_portal.PortalResponse
import com.aientec.ktv_portal.PortalService
import com.aientec.structure.OpenInfo
import com.aientec.structure.Room
import com.aientec.structure.Store
import com.aientec.structure.User
import idv.bruce.common.impl.ModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(context: Context) : ModelImpl(context) {
    private var user: User? = null

    override val tag: String
        get() = "Repo"

    private val portalService: PortalService = PortalService.getInstance(context)

    override fun init() {
        portalService.init()
    }

    override fun release() {

    }

    suspend fun getStores(): List<Store>? = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.deviceGetStores()

        if (response.status == PortalResponse.STATUS_OK)
            return@withContext (response.data as List<*>).filterIsInstance(Store::class.java)
        else
            return@withContext null
    }

    suspend fun getRooms(storeId: Int): List<Room>? = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.deviceGetRooms(storeId)

        if (response.status == PortalResponse.STATUS_OK)
            return@withContext (response.data as List<*>).filterIsInstance(Room::class.java)
        else
            return@withContext null
    }

    suspend fun login(account: String, password: String): User? = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.staffLogin(account, password)

        if (response.status == PortalResponse.STATUS_OK) {
            user = response.data as User
            return@withContext user
        } else
            return@withContext null
    }

    suspend fun getOpenInfo(boxId: Int): OpenInfo? = withContext(Dispatchers.IO) {
        val response: PortalResponse =
            portalService.vodGetOpenInfo(user?.token ?: return@withContext null, boxId)

        return@withContext if (response.status == PortalResponse.STATUS_OK)
            response.data as OpenInfo
        else
            null
    }

    suspend fun startTimer(orderId: Int): Boolean = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.vodStartTimer(
            user?.token ?: return@withContext false,
            orderId,
            System.currentTimeMillis()
        )

        return@withContext response.status == PortalResponse.STATUS_OK
    }

    suspend fun closeBox(boxId: Int): Boolean = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.staffCloseBox(
            user?.token ?: return@withContext false,
            boxId
        )

        return@withContext response.status == PortalResponse.STATUS_OK
    }
}