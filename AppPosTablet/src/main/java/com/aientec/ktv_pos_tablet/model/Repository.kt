package com.aientec.ktv_pos_tablet.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.graphics.PointF
import android.util.Size
import com.aientec.ktv_portal.PortalResponse
import com.aientec.ktv_portal.PortalService
import com.aientec.ktv_pos_tablet.structure.*
import com.aientec.structure.Reserve
import com.aientec.structure.Room
import com.aientec.structure.Store
import com.aientec.structure.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.suspendCoroutine

class Repository(context: Context) : Model(context) {
    private val portalService: PortalService = PortalService.getInstance(context)

    private val boxMap: HashMap<Int, Box> = HashMap()

    private val roomTypeMap: HashMap<Int, Room.Type> = HashMap()

    private val cameraList: HashMap<Int, IpCamera> = HashMap()

    private val floorMap: HashMap<Int, Floor> = HashMap()

    private var config: Configuration? = null

    private lateinit var demoMapFile: File

    private lateinit var demoMapSize: Size

    private var user: User? = null

    private var store: Store? = null

    private val openMap: HashMap<Box, Reserve> = HashMap()

    override fun init() {
        portalService.apiRoot = "http://106.104.151.145:10005/index.php/"
        portalService.init()

        boxMap.clear()

        cameraList.clear()
        cameraList[0] = IpCamera(0, "包廂101", "192.168.77.40", 1, Point(523, 167))
        cameraList[1] = IpCamera(1, "備餐與出餐", "192.168.77.41", 1, Point(912, 167))
        cameraList[2] = IpCamera(2, "櫃台", "192.168.77.42", 1, Point(406, 440))
        cameraList[3] = IpCamera(3, "包廂102", "192.168.77.43", 1, Point(860, 1100))
//        cameraList[3] = IpCamera(3, "鏡頭4", "192.168.77.43")
//        cameraList[4] = IpCamera(4, "鏡頭5", "192.168.77.44")

        val inStream: InputStream = contextRef.get()!!.assets.open("aientec_map.png")

        demoMapFile = File(contextRef.get()!!.cacheDir, "map.png")

        val length: Int = 0

        val buffer: ByteArray = ByteArray(1024)

        val out: FileOutputStream = FileOutputStream(demoMapFile)

        while (length.run { inStream.read(buffer) } >= 0)
            out.write(buffer, 0, length)

        out.close()
        inStream.close()

        demoMapSize = Size(1280, 1280)
    }

    override fun release() {

    }

    suspend fun readConfig(): Configuration? = withContext(Dispatchers.IO) {
        if (config != null) return@withContext config

        val sharedPreferences: SharedPreferences =
            contextRef.get()!!.getSharedPreferences("config", Context.MODE_PRIVATE)

        val isInit: Boolean = sharedPreferences.getBoolean("init", false)

        if (isInit) {
            config = Configuration(
                sharedPreferences.getInt("store_id", -1),
                sharedPreferences.getString("store_name", "") ?: ""
            )
            return@withContext config
        } else
            return@withContext null
    }

    suspend fun updateConfig(configuration: Configuration): Boolean = withContext(Dispatchers.IO) {
        val editor: SharedPreferences.Editor =
            contextRef.get()!!.getSharedPreferences("config", Context.MODE_PRIVATE).edit()

        editor.putInt("store_id", configuration.storeId)
        editor.putString("store_name", configuration.storeName)
        editor.putBoolean("init", true)

        config = configuration

        return@withContext editor.commit()
    }

    suspend fun dataSyn(): String? = withContext(Dispatchers.IO) {
        var response: PortalResponse?

        response = portalService.deviceGetTypes()

        if (response.status == PortalResponse.STATUS_OK) {
            val list: ArrayList<Room.Type> =
                (response.data as ArrayList<*>).filterIsInstance<Room.Type>() as ArrayList<Room.Type>

            roomTypeMap.clear()

            roomTypeMap[-1] = Room.Type(-1, "全部包廂")

            for (type in list)
                roomTypeMap[type.id] = type
        } else
            return@withContext response.message

        response = portalService.deviceGetRooms(config!!.storeId)

        if (response.status == PortalResponse.STATUS_OK) {
            val list: ArrayList<Room> =
                (response.data as ArrayList<*>).filterIsInstance<Room>() as ArrayList<Room>

            boxMap.clear()
            floorMap.clear()

            floorMap[0] = Floor().apply {
                this.id = 0
                this.name = "全部樓層"
                this.map = demoMapFile.absolutePath
                this.size = demoMapSize
            }

            for (room in list) {

                boxMap[room.id] = Box(room)

                if (!floorMap.containsKey(room.floor))
                    floorMap[room.floor] = Floor().apply {
                        this.id = room.floor
                        this.name = "${room.floor}樓"
                        this.map = demoMapFile.absolutePath
                        this.size = demoMapSize
                    }
            }
        } else
            return@withContext response.message

        return@withContext null
    }

    suspend fun getStores(): ArrayList<Store>? = withContext(Dispatchers.IO) {
        val response = portalService.deviceGetStores()

        if (response.status == PortalResponse.STATUS_OK) {
            return@withContext (response.data as ArrayList<*>).filterIsInstance<Store>() as ArrayList<Store>
        } else {
            return@withContext null
        }

    }

    suspend fun getFloorsInfo(): ArrayList<Floor> = withContext(Dispatchers.IO) {
        val list: ArrayList<Floor> = ArrayList()

        list.addAll(floorMap.values)
        list.sortBy {
            return@sortBy it.id
        }

        return@withContext list
    }

    suspend fun getBoxTypes(): ArrayList<Room.Type> = withContext(Dispatchers.IO) {
        val list: ArrayList<Room.Type> = ArrayList()

        list.addAll(roomTypeMap.values)
        list.sortBy {
            return@sortBy it.id
        }

        return@withContext list
    }

    suspend fun getBoxType(id: Int): Room.Type? = withContext(Dispatchers.IO) {
        return@withContext roomTypeMap[id]
    }

    suspend fun updateBoxes(): Boolean = withContext(Dispatchers.IO) {
        val response = portalService.deviceGetRooms(config!!.storeId)

        if (response.status == PortalResponse.STATUS_OK) {
            val list: ArrayList<Room> =
                (response.data as ArrayList<*>).filterIsInstance<Room>() as ArrayList<Room>

            boxMap.clear()

            for (room in list)
                boxMap[room.id] = Box(room)

            return@withContext true
        } else {
            return@withContext false
        }
    }

    suspend fun filterBoxes(floor: Int, type: Int): ArrayList<Box> = withContext(Dispatchers.IO) {
        return@withContext boxMap.values.filter {
            return@filter ((it.floor == floor) || (floor == 0)) && ((it.type == type) || (type == -1))
        } as ArrayList<Box>
    }

    suspend fun staffLogin(account: String, password: String): User? = withContext(Dispatchers.IO) {
        val response: PortalResponse = portalService.staffLogin(account, password)

        user = if (response.status == PortalResponse.STATUS_OK)
            response.data as User
        else
            null

        return@withContext user
    }

    suspend fun getCameras(): ArrayList<IpCamera> = withContext(Dispatchers.IO) {
        val list: ArrayList<IpCamera> = ArrayList()
        list.addAll(cameraList.values)
        return@withContext list
    }

    suspend fun getMapMarkers(floor: Int): ArrayList<MapMarker> = withContext(Dispatchers.IO) {
        val list: ArrayList<MapMarker> = ArrayList()
        val cams: ArrayList<IpCamera> = cameraList.values.filter {
            return@filter it.floor == floor
        } as ArrayList<IpCamera>

        for (cam in cams)
            list.add(MapMarker(cam, (cam.position ?: Point(0, 0))))
        return@withContext list
    }

    suspend fun getReserveList(states: Int = -1): List<Reserve>? =
        withContext(Dispatchers.IO) {
            val response: PortalResponse =
                portalService.staffListReserve(
                    user?.token ?: return@withContext null,
                    config?.storeId ?: return@withContext null,
                    states
                )

            return@withContext if (response.status == PortalResponse.STATUS_OK)
                (response.data as List<*>).filterIsInstance(Reserve::class.java).sortedWith(
                    compareBy({ -it.checkInTimestamp }, { -it.timestamp })
                )
            else
                null
        }

    suspend fun reserveCheckIn(reserveId: Int): Boolean = withContext(Dispatchers.IO) {
        val response: PortalResponse =
            portalService.staffReserveCheckIn(user?.token ?: return@withContext false, reserveId)

        return@withContext response.status == PortalResponse.STATUS_OK
    }

    suspend fun reserveCancel(reserveId: Int): Boolean = withContext(Dispatchers.IO) {
        val response: PortalResponse =
            portalService.staffReserveCancel(user?.token ?: return@withContext false, reserveId)

        return@withContext response.status == PortalResponse.STATUS_OK
    }

    suspend fun openBox(box: Box, reserve: Reserve, type: Int, duration: Int): Boolean =
        withContext(Dispatchers.IO) {
            val response: PortalResponse = portalService.staffOpenBox(
                user?.token ?: return@withContext false,
                box.id,
                reserve.id,
                type,
                duration,
                Date().time
            )

            return@withContext if (response.status == PortalResponse.STATUS_OK) {
                boxMap[box.id]?.state = Room.STATE_ON_USE
                openMap[box] = reserve
                true
            } else {
                false
            }
        }

    suspend fun staffCheckBill(box: Box, price: Int, remark: String): Boolean =
        withContext(Dispatchers.IO) {
            val reserve: Reserve = openMap[box] ?: return@withContext false

            val response: PortalResponse = portalService.staffSubmitPay(
                user?.token ?: return@withContext false,
                box.id,
                reserve.memberId,
                price,
                remark
            )

            return@withContext if (response.status == PortalResponse.STATUS_OK) {
                boxMap[box.id]?.state = Room.STATE_ON_CHECKED
                openMap.remove(box)
                true
            } else
                false
        }

    suspend fun closeBox(box: Box): Boolean = withContext(Dispatchers.IO) {

        val response: PortalResponse =
            portalService.staffCloseBox(user?.token ?: return@withContext false, box.id)



        return@withContext if (response.status == PortalResponse.STATUS_OK) {
            boxMap[box.id]?.state = Room.STATE_IDLE
            true
        } else {
            false
        }
    }
}