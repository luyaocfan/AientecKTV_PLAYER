package com.aientec.ktv_vod.module

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.aientec.ktv_portal2.PortalResponse
import com.aientec.ktv_portal2.PortalService2
import com.aientec.ktv_vod.common.impl.ModuleImpl
import com.aientec.ktv_vod.common.structure.Configuration
import com.aientec.ktv_vod.structure.Singer
import com.aientec.ktv_wifiap.BuildConfig
import com.aientec.ktv_wifiap.RoomWifiService
import com.aientec.ktv_wifiap.commands.*
import com.aientec.structure.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.collections.ArrayList


class Repository(context: Context) : ModuleImpl(context) {
      companion object {
            private const val QR_CODE_FILE = "qr_code.png"
      }

      enum class InputType(val code: String) {
            PHONETIC("C01"),
            SPELLING("C08"),
            OTHER("CFF")
      }

      private val portalService2: PortalService2 = PortalService2.getInstance(context)

      private val wifiService: RoomWifiService = RoomWifiService.getInstance(context)

      private var configuration: Configuration? = null

      val roomOpen: MutableLiveData<Boolean> = MutableLiveData(false)

      val users: MutableLiveData<List<User>> = MutableLiveData(ArrayList())

      val playingTracks: MutableLiveData<ArrayList<Track>> = MutableLiveData()

      val currantPlayingTrack: MutableLiveData<Track?> = MutableLiveData()

      val prepareTrack: MutableLiveData<Track?> = MutableLiveData()

      val qrCodeData: MutableLiveData<String> = MutableLiveData()

      val roomInfo: MutableLiveData<Room> = MutableLiveData()

      val albums: MutableLiveData<List<Album>> = MutableLiveData()

      val idleTracks: MutableLiveData<List<Track>> = MutableLiveData()

      private val timerService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

      private val databaseReader: KtvDatabaseReader = KtvDatabaseReader.getInstance()

      private var startTime: Long = -1L

      private var endTime: Calendar? = null

      private var timer: Timer? = null

      private var addTrackRunnable: AddTrackRunnable? = null

      private var addThread: ExecutorService = Executors.newSingleThreadExecutor()

      private val mPlayingTracks: ArrayList<Track> = ArrayList()

      private val mUsers: ArrayList<User> = ArrayList()

      private lateinit var userImgFilePath: File

      lateinit var uuid: String

      val roomId: Int
            get() = configuration?.roomId ?: -1

      var dataBaseRemoteRoot: String = ""

      override fun init() {

            if (BuildConfig.DEBUG) {
                  users.value = ArrayList<User>().apply {
                        add(User(0, "User0"))
                        add(User(1, "User1"))
                        add(User(2, "User2"))
                        add(User(3, "User3"))
                        add(User(4, "User4"))
                        add(User(5, "User5"))
                        add(User(6, "User6"))
                        add(User(7, "User7"))
                  }
            }

            loadUUID()

            userImgFilePath = File(contextRef.get()!!.cacheDir, "user")

            wifiService.init()

            portalService2.init()

            wifiService.addListener(this, listener)
      }


      override fun release() {
            timerService.shutdownNow()
      }

      suspend fun updateConfiguration(config: Configuration): Boolean =
            withContext(Dispatchers.IO) {
                  val sharedPreferences: SharedPreferences =
                        contextRef.get()!!.getSharedPreferences("vod_params", Context.MODE_PRIVATE)

                  configuration = config

                  if (configuration!!.writToSharedPreferences(sharedPreferences)) {
                        roomInfo.postValue(Room(config.roomId, config.roomName))
                        return@withContext true
                  }

                  return@withContext false
            }

      suspend fun getConfiguration(): Configuration? = withContext(Dispatchers.IO) {
            if (configuration != null) return@withContext configuration

            val sharedPreferences: SharedPreferences =
                  contextRef.get()!!.getSharedPreferences("vod_params", Context.MODE_PRIVATE)

            configuration = Configuration()

            if (!configuration!!.readFromSharedPreferences(sharedPreferences))
                  configuration = null

            if (configuration != null)
                  roomInfo.postValue(Room(configuration!!.roomId, configuration!!.roomName))

            return@withContext configuration
      }

      suspend fun refreshWifiConfig() = withContext(Dispatchers.IO) {
            val config: Configuration = configuration ?: return@withContext
            config.generateRandomWifi()
            Log.d("Trace", "Refresh wifi : ${config.wifiSsid}, ${config.password}")
            if (BuildConfig.DEBUG) {
                  config.wifiSsid = "BruceWifi"
                  config.password = "0987935857"
            } else {
                  config.wifiSsid = "theLOOPKTV_BOX102_5G"
                  config.password = "sunriver@123"
            }
            wifiService.updateWifiInformation(config.wifiSsid, config.password)
            qrCodeData.postValue(getConfigQrcodeImage())
      }

      suspend fun synDatabase(): Boolean = withContext(Dispatchers.IO) {
            val downloadUrl: String = "${dataBaseRemoteRoot}public/db/ktvsong.db"

            Log.d("Trace", "DB : $downloadUrl")

            val file: File = File(contextRef.get()!!.cacheDir, "ktvsong.db")

            if (file.exists())
                  file.delete()

            try {
                  val connection: HttpURLConnection =
                        (URL(downloadUrl).openConnection()) as HttpURLConnection

                  connection.connect()

                  val outputStream: FileOutputStream = FileOutputStream(file)

                  connection.inputStream.copyTo(outputStream)

                  connection.disconnect()

                  outputStream.close()

                  if (databaseReader.openDatabase(file.absolutePath)) {
//                idleTracks.postValue(databaseReader.getIdleTracks())
                        albums.postValue(databaseReader.getTrackLists())
                        Log.e("Trace", "Data syn success")
                  } else {
                        Log.e("Trace", "Data syn failed")
                        return@withContext false
                  }
            } catch (e: IOException) {
                  e.printStackTrace()
                  return@withContext false
            } catch (e: SocketTimeoutException) {
                  e.printStackTrace()
                  return@withContext false
            } catch (e: FileNotFoundException) {
                  e.printStackTrace()
                  return@withContext false
            }



            return@withContext true

//        try {
//            val assetManager: AssetManager = contextRef.get()?.assets ?: return@withContext false
//
//            val inStream: InputStream = assetManager.open("ktvsong.db")
//
//            val outStream: FileOutputStream = FileOutputStream(file, false)
//
//            val count: Long = inStream.copyTo(outStream)
//
//            Log.d("Trace", "Copy : $count bytes")
//
//            outStream.close()
//            inStream.close()
//
//            if (databaseReader.openDatabase(file.absolutePath)) {
//                idleTracks.postValue(databaseReader.getIdleTracks())
//                albums.postValue(databaseReader.getTrackLists())
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return@withContext false
//        }
//
//        return@withContext true
      }

      suspend fun getStores(): List<Store>? = withContext(Dispatchers.IO) {

            val response: PortalResponse = portalService2.Store.getStoreList()

            return@withContext when (response) {
                  is PortalResponse.Fail -> null
                  is PortalResponse.Success -> (response.data as List<*>).filterIsInstance(
                        Store::class.java
                  )
            }
      }

      suspend fun getRooms(storeId: Int): List<Room>? = withContext(Dispatchers.IO) {
            val response: PortalResponse = portalService2.Store.getRoomList(storeId)

            return@withContext when (response) {
                  is PortalResponse.Fail -> null
                  is PortalResponse.Success -> (response.data as List<*>).filterIsInstance(Room::class.java)
            }
      }


      private fun configurationToJson(config: Configuration?): String? {
            if (config == null) return null
            val jsonObj = JSONObject().apply {
                  this.put("ROOM_ID", config.roomId)
                  this.put("ROOM", config.roomName)
                  this.put("SSID", config.wifiSsid)
                  this.put("PASSWORD", config.password)
                  val ip: String = wifiService.findServer()!!.split(":")[0]
                  val port: Int = wifiService.findServer()!!.split(":")[1].toInt()
                  this.put("SERVER_IP", ip)
//            this.put("SERVER_IP", "106.104.151.145")
                  this.put("SERVER_PORT", port)
                  this.put("POS_SERVER", "http://106.104.151.145:10005/index.php/")
            }
            Log.d("Trace", jsonObj.toString())
            return "{\"ROOM_ID\":19,\"ROOM\":\"601\",\"SSID\":\"theLOOPKTV_BOX102_5G\",\"PASSWORD\":\"sunriver@123\",\"SERVER_IP\":\"10.10.10.1\",\"SERVER_PORT\":40051,\"ROOM_TYPE\":\"VIP01(10-20人)\"}"
//        return jsonObj.toString()
      }

      suspend fun getSingerList(): List<Singer>? = withContext(Dispatchers.IO) {
            return@withContext databaseReader.getSingerList()
      }

      suspend fun searchSingers(type: Int, searchingKey: String, keyType: Int): List<Singer>? =
            withContext(Dispatchers.IO) {
                  return@withContext databaseReader.searchSingers(type, searchingKey, keyType)
            }

      suspend fun searchTracks(searchingKey: String, keyType: Int): List<Track>? =
            withContext(Dispatchers.IO) {
                  return@withContext databaseReader.searchTracks(searchingKey, keyType)
            }

      suspend fun getAlbums(type: Int): List<Album>? = withContext(Dispatchers.IO) {
            return@withContext databaseReader.getAlbums(type)
      }

      suspend fun quickFilterTracks(inputType: InputType, code: String): List<Track>? =
            withContext(Dispatchers.IO) {
                  val queryCmd: String =
                        "SongNo in (select songcode.SongNo from songcode where songcode.CodeType = '${inputType.code}' and songcode.Code like '${code}%')"
                  return@withContext databaseReader.searchTracks(queryCmd)
            }

      suspend fun quickFilterKeys(keys: String, searchType: Int, keyType: Int): List<String>? =
            withContext(Dispatchers.IO) {
                  return@withContext databaseReader.getSearchKeys(
                        keys,
                        keys.length,
                        searchType,
                        keyType
                  )
            }

      suspend fun trackCommand(track: Track, command: Int): Boolean = withContext(Dispatchers.IO) {
            return@withContext if (command == 1) {
                  wifiService.addTrack(track)
            } else
                  wifiService.insertTrack(track)
      }


      suspend fun getAllTracks(): List<Track>? = withContext(Dispatchers.IO) {
            return@withContext databaseReader.searchTracks("Id > 62")
      }

      suspend fun getTracksFromAlbum(id: Int): List<Track>? = withContext(Dispatchers.IO) {
            return@withContext databaseReader.getAlbumTracks(id)
      }

      suspend fun getTracksFromSinger(singer: Singer): List<Track>? = withContext(Dispatchers.IO) {
            return@withContext databaseReader.getSingerTracks(singer.id)
      }

      suspend fun playlistInsert(track: Track): Boolean = withContext(Dispatchers.IO) {
            val list: ArrayList<Track> = (mPlayingTracks)


            if (list.count { it.state != Track.State.DONE } < 4)
                  return@withContext trackCommand(track, 1)


            val oldIndex: Int =
                  list.indexOfFirst { it.state == Track.State.QUEUE && it.id == track.id }

            if (oldIndex == -1) return@withContext false

            val newIndex: Int = list.indexOfFirst { it.state == Track.State.QUEUE }

            val targetId: Int = list[newIndex].id

            list.removeAt(oldIndex)

            list.add(newIndex, track)

            playingTracks.postValue(list)

            Log.d(
                  "Trace",
                  "OldInd : $oldIndex, NewInd : $newIndex, Track : ${track.id}, ${track.name}"
            )

            return@withContext wifiService.moveTrack(
                  oldIndex + 1,
                  newIndex + 1,
                  track.id,
                  targetId
            )
      }

      suspend fun playlistDel(track: Track): Boolean = withContext(Dispatchers.IO) {
            val list: ArrayList<Track> = mPlayingTracks

            if (list.count { it.state != Track.State.DONE } < 3) return@withContext false

            val index: Int =
                  list.indexOfFirst { it.id == track.id && it.state == Track.State.QUEUE }

            if (index == -1) return@withContext false

            list.removeAt(index)

            playingTracks.postValue(list)

            return@withContext wifiService.delTrack(index + 1, track.id)

      }


      suspend fun debugOpen(): Boolean = withContext(Dispatchers.IO) {
            roomOpen.postValue(true)
            return@withContext true
      }

      suspend fun checkOpenInfo(): Boolean = withContext(Dispatchers.IO) {
            val res: Boolean = getOpenInfo()
            roomOpen.postValue(res)
            return@withContext res
      }

      private fun getOpenInfo(): Boolean {
            val response: PortalResponse = portalService2.Store.getRoomOpenInfo(
                  "",
                  configuration?.roomId ?: return false
            )

            return when (response) {
                  is PortalResponse.Fail -> false
                  is PortalResponse.Success -> {
                        (response.data as OpenInfo).run {

                              if (this.status == 2) {
                                    mUsers.clear()
                                    mUsers.add(
                                          User(
                                                memberId,
                                                memberName,
                                                null,
                                                memberIcon
                                          )
                                    )

                                    true
                              } else {
                                    false
                              }
                        }
                  }
            }
      }

      private fun getConfigQrcodeImage(): String? {
            val data: String = configurationToJson(configuration) ?: return null

            val hashMap: EnumMap<EncodeHintType, Any> =
                  EnumMap(com.google.zxing.EncodeHintType::class.java)
            hashMap[EncodeHintType.CHARACTER_SET] = "utf-8"
            hashMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            hashMap[EncodeHintType.MARGIN] = 0

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 256, 256, hashMap)

            val imageFile: File = File(contextRef.get()!!.cacheDir, QR_CODE_FILE)

            val w: Int = bitMatrix.width
            val h: Int = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                  val offset = y * w
                  for (x in 0 until w) {
                        pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                  }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, 256, 0, 0, w, h)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(imageFile))

            bitmap.recycle()

            return imageFile.absolutePath
      }

      private val listener: RoomWifiService.EventListener =
            RoomWifiService.EventListener { event, dsData ->
                  Log.d("Repo", "Event : $dsData")
                  when (event) {
                        RoomWifiService.Event.ROOM_SWITCH -> {

                              val d = dsData as RoomSwitchData
                              Log.d("Repo", "RoomSwitchData : ${d.switch}")
                              roomOpen.postValue(
                                    if (d.switch) {
                                          getOpenInfo()
                                    } else {
                                          mPlayingTracks.clear()
                                          playingTracks.postValue(mPlayingTracks)
                                          timer?.cancel()
                                          false
                                    }
                              )
                        }
                        RoomWifiService.Event.TRACKS_UPDATE -> {
                              val data = dsData as PlayListData
                              val list = data.list

                              addTrackRunnable?.isListRefreshed = true

                              mPlayingTracks.clear()

                              mPlayingTracks.addAll(list)

                              playingTracks.postValue(mPlayingTracks)
                              currantPlayingTrack.postValue(list.find { it.state == Track.State.PLAYING })
                              prepareTrack.postValue(list.find { it.state == Track.State.NEXT })
                        }
                        RoomWifiService.Event.ADD_SONG -> {

                              val data = dsData as TrackReqData

                              if (addTrackRunnable == null) {
                                    addTrackRunnable = AddTrackRunnable()
                                    addThread.execute(addTrackRunnable)
                              }

                              addTrackRunnable!!.addTrack(data.track ?: return@EventListener true)
                        }
                        RoomWifiService.Event.INSERT_SONG -> {
                              val data = dsData as TrackInsertData
                              val list: ArrayList<Track> = mPlayingTracks
                              val index: Int = if (list.size > 0)
                                    list.indexOfFirst { it.state == Track.State.QUEUE }
                              else
                                    -1

                              if (index < 1)
                                    list.add(data.track ?: return@EventListener true)
                              else {
                                    try {
                                          if (index > 0 && list[index - 1].state == Track.State.PLAYING)
                                                data.track?.state = Track.State.NEXT
                                    } catch (e: IndexOutOfBoundsException) {
                                          e.printStackTrace()
                                    }
                                    list.add(index, data.track ?: return@EventListener true)
                              }
                              playingTracks.postValue(list)
                        }
                        RoomWifiService.Event.DEL_SONG -> {
                              if (mPlayingTracks.isNotEmpty()) {
                                    val data = dsData as TrackDelData

                                    val index: Int = data.index.toInt()

                                    if (index <= mPlayingTracks.lastIndex) {
                                          val item: Track = mPlayingTracks[index]

                                          if (item.id == data.id.toInt()) {
                                                mPlayingTracks.removeAt(index)

                                                playingTracks.postValue(mPlayingTracks)
                                          } else {
                                                MainScope().launch {
                                                      Toast.makeText(
                                                            contextRef.get()!!,
                                                            "Delete song error : ($index : ${item.id})",
                                                            Toast.LENGTH_SHORT
                                                      ).show()
                                                }
                                          }
                                    }
                              }
                        }
                        RoomWifiService.Event.MOVE_SONG -> {
                              if (mPlayingTracks.isNotEmpty()) {

                                    val data = dsData as TrackMoveData

                                    var oldInd: Int = data.oldIndex.toInt() - 1

                                    val newInd: Int = data.newIndex.toInt() - 1

                                    val oldItem: Track = mPlayingTracks[oldInd]

                                    val newItem: Track = mPlayingTracks[newInd]

                                    if (oldItem.id == data.id.toInt() && newItem.id == data.afterId.toInt()) {

                                          mPlayingTracks.add(newInd, oldItem)

                                          if (newInd < oldInd)
                                                oldInd++

                                          mPlayingTracks.removeAt(oldInd)

                                          playingTracks.postValue(mPlayingTracks)
                                    } else {
                                          MainScope().launch {
                                                Toast.makeText(
                                                      contextRef.get()!!,
                                                      "Move song error : ($oldInd : ${oldItem.id}) to ($newInd, ${newItem.id}",
                                                      Toast.LENGTH_SHORT
                                                ).show()
                                          }
                                    }
                              }

                        }
                        RoomWifiService.Event.VOD_PLAY_CTRL -> {
                              val data = dsData as PlayerData
                              when (data.func) {
                                    DSData.PlayerFunc.NEXT -> {
                                          val list: ArrayList<Track> = mPlayingTracks

                                          val index: Int = list.indexOf(currantPlayingTrack.value)

                                          if (index == -1) {
                                                val playIndex: Int =
                                                      list.indexOfFirst { it.state == Track.State.QUEUE }

                                                if (playIndex != -1) {
                                                      list[playIndex].state = Track.State.PLAYING
                                                      currantPlayingTrack.postValue(list[playIndex])
                                                }

                                                if (playIndex < list.lastIndex) {
                                                      list[playIndex + 1].state = Track.State.NEXT
                                                      prepareTrack.postValue(list[playIndex + 1])
                                                }

                                          } else {
                                                list[index].state = Track.State.DONE

                                                if (index + 1 <= list.lastIndex) {
                                                      list[index + 1].state = Track.State.PLAYING
                                                      currantPlayingTrack.postValue(list[index + 1])
                                                } else {
                                                      currantPlayingTrack.postValue(null)
                                                }

                                                if (index + 2 <= list.lastIndex) {
                                                      list[index + 2].state = Track.State.NEXT
                                                      prepareTrack.postValue(list[index + 2])
                                                } else {
                                                      prepareTrack.postValue(null)
                                                }

                                          }
                                          playingTracks.postValue(list)
                                    }
                                    DSData.PlayerFunc.DONE -> {
                                          var list: ArrayList<Track>? = null
                                          try {
                                                list = mPlayingTracks

                                                list.forEach { it.state = Track.State.DONE }

                                          } catch (e: Exception) {
                                                e.printStackTrace()
                                          }

                                          prepareTrack.postValue(null)

                                          currantPlayingTrack.postValue(null)

                                          playingTracks.postValue(list ?: ArrayList())
                                    }
                              }
                              return@EventListener false
                        }
                        RoomWifiService.Event.MEMBER_JOIN -> {
                              val data = dsData as StateData
                              memberJoin(
                                    data.state == StateData.State.ONLINE,
                                    data.userId.toInt(),
                                    data.token
                              )
                        }
                  }
                  return@EventListener true
            }

      private fun memberJoin(isOnline: Boolean, userId: Int, token: String?) {
            if (isOnline) {
                  val portalResponse: PortalResponse =
                        portalService2.Store.getUserInfo(token!!)

                  when (portalResponse) {
                        is PortalResponse.Fail -> {}
                        is PortalResponse.Success -> {
                              val user: User = portalResponse.data as User

//                              downloadPicture(user)

                              if (!mUsers.contains(user))
                                    mUsers.add(user)
                        }
                  }
            } else {
                  mUsers.removeIf {
                        it.id == userId
                  }
            }
            users.postValue(mUsers)
      }

      private fun loadUUID() {
            val sharedPreferences: SharedPreferences =
                  contextRef.get()!!.getSharedPreferences("uuid", Context.MODE_PRIVATE)
            uuid = sharedPreferences.getString("uuid", UUID.randomUUID().toString())!!

            Log.d("Trace", "UUID : $uuid")
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("uuid", uuid)
            editor.apply()
      }

      private fun downloadPicture(user: User) {
            val url: String = user.icon
            if (url.isEmpty()) return

            val file: File = File(userImgFilePath, "${user.id}.tmp")

            val connection: HttpURLConnection = (URL(url).openConnection()) as HttpURLConnection

            connection.connect()

            val inputStream: InputStream = connection.inputStream

            val outputStream: FileOutputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            inputStream.close()

            outputStream.close()

            file.renameTo(File(userImgFilePath, "${user.id}.jpg"))

      }

      private inner class AddTrackRunnable : Runnable {
            private var runTime: Long = System.currentTimeMillis()

            private val list: ArrayList<Track> = mPlayingTracks

            private var currantTime: Long = -1L

            private val lastTrack: Track?
                  get() = if (list.size > 0) list[list.lastIndex] else null


            var isListRefreshed: Boolean = false

            override fun run() {

                  Log.d("Trace", "Waiting...")
                  while (currantTime - runTime < 500) {
                        currantTime = System.currentTimeMillis()
                        Thread.yield()
                  }

                  Log.d("Trace", "Update...")
                  if (!isListRefreshed)
                        playingTracks.postValue(list)

                  addTrackRunnable = null
            }

            fun addTrack(track: Track) {
                  runTime = System.currentTimeMillis()

                  if (lastTrack == null || lastTrack?.state == Track.State.PLAYING) {
                        track.state = Track.State.NEXT
                        prepareTrack.postValue(track)
                  } else {
                        track.state = Track.State.QUEUE
                  }

                  list.add(track)
            }
      }
}