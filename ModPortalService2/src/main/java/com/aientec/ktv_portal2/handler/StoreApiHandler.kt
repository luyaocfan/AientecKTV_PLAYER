package com.aientec.ktv_portal2.handler

import com.aientec.ktv_portal2.PortalResponse
import com.aientec.ktv_portal2.PortalService2
import com.aientec.ktv_portal2.api.StoreWebServiceApi
import com.aientec.ktv_portal2.util.toMd5
import com.aientec.ktv_portal2.util.toRequestBody
import com.aientec.structure.*
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class StoreApiHandler internal constructor(apiInstance: StoreWebServiceApi) :
    ApiHandler<StoreWebServiceApi>(apiInstance) {
    override val tag: String = "Store"


    fun login(account: String, password: String): PortalResponse {
        val requestBody: RequestBody = JSONObject().apply {
            put("userCode", account)
            put("userPwd", password.toMd5())
        }.toRequestBody()

        val apiCallback: ApiCallback = apiExecute(
            requestBody,
            apiInstance::actionLogin
        )

        return when (apiCallback) {
            is ApiCallback.Error -> {
                PortalResponse.Fail(apiCallback.msg)
            }
            is ApiCallback.Complete -> {
                val user: User = User(
                    0,
                    apiCallback.resObj.getString("UserName"),
                    apiCallback.resObj.getString("Token")
                )
                PortalResponse.Success(user)
            }
        }
    }

    fun getStoreList(): PortalResponse {
        val requestBody: RequestBody = JSONObject().toRequestBody()

        val apiCallback: ApiCallback = apiExecute(requestBody, apiInstance::getStoreList)

        return when (apiCallback) {
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
            is ApiCallback.Complete -> {
                val objArr: JSONArray = apiCallback.resObj.getJSONArray("BranchList")

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

                PortalResponse.Success(list)
            }
        }
    }

    fun getRoomList(storeId: Int): PortalResponse {
        val requestBody = JSONObject().apply {
            this.put("branchId", storeId)
        }.toRequestBody()

        val apiCallback: ApiCallback =
            apiExecute(requestBody, apiInstance::getRoomList)

        return when (apiCallback) {
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
            is ApiCallback.Complete -> {
                val objArr: JSONArray = apiCallback.resObj.getJSONArray("BoxList")

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

                PortalResponse.Success(list)
            }
        }
    }

    fun getRoomTypeList(): PortalResponse {

        val requestBody: RequestBody = JSONObject().toRequestBody()

        val apiCallback: ApiCallback =
            apiExecute(requestBody, apiInstance::getRoomTypeList)

        return when (apiCallback) {
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
            is ApiCallback.Complete -> {
                val objArr: JSONArray = apiCallback.resObj.getJSONArray("TypeList")

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

                PortalResponse.Success(list)
            }
        }
    }

    fun getUserInfo(token: String): PortalResponse {
        val requestBody: RequestBody = JSONObject().apply {
            put("token", token)
        }.toRequestBody()

        val apiCallback: ApiCallback = apiExecute(requestBody, apiInstance::getUserInfo)

        return when (apiCallback) {
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
            is ApiCallback.Complete -> {
                val user: User
                apiCallback.resObj.getJSONObject("MemberInfo").apply {
                    user = User(
                        getInt("MemberId"), getString("MemberName"), token, getString("HeadIcon")
                    )
                }
                PortalResponse.Success(user)
            }
        }
    }

    fun getRoomOpenInfo(token: String, boxId: Int): PortalResponse {
        val requestBody: RequestBody = JSONObject().apply {
            put("token", token)
            put("boxId", boxId)
        }.toRequestBody()

        val apiCallback: ApiCallback = apiExecute(requestBody, apiInstance::getOpenInfo)

        return when (apiCallback) {
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
            is ApiCallback.Complete -> {
                val info: OpenInfo = OpenInfo(
                    apiCallback.resObj.getInt("BoxStatus"),
                    apiCallback.resObj.getInt("MemberId"),
                    apiCallback.resObj.getString("MemberName"),
                    apiCallback.resObj.getString("MemberIcon")
                )
                PortalResponse.Success(info)
            }
        }
    }

    fun getAdsTrackList(): PortalResponse {
        val requestBody: RequestBody = JSONObject().put("token", "").toRequestBody()

        val apiCallback: ApiCallback = apiExecute(requestBody, apiInstance::getIdleTrackList)

        return when (apiCallback) {
            is ApiCallback.Complete -> {

                val listObjArray: JSONArray = apiCallback.resObj.getJSONArray("SongList")

                val count: Int = listObjArray.length()

                val list: ArrayList<Track> = ArrayList()

                for (i in 0 until count) {
                    val trackObj: JSONObject = listObjArray.getJSONObject(i)

                    list.add(
                        Track(
                            trackObj.getInt("Id"),
                            trackObj.getString("SongId"),
                            trackObj.getString("SongName"),
                            trackObj.getString("Singer"),
                            "",
                            "",
                            "",
                            trackObj.getString("SongImg"),
                            Track.State.NONE,
                            "",
                            trackObj.getString("SongFile")
                        )
                    )
                }

                PortalResponse.Success(list)
            }
            is ApiCallback.Error -> PortalResponse.Fail(apiCallback.msg)
        }
    }

}