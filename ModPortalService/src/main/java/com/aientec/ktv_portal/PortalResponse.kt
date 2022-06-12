package com.aientec.ktv_portal

class PortalResponse {
    companion object{
        const val STATUS_OK = 1
        const val STATUS_API_ERROR = 2
        const val STATUS_SERVER_ERROR = 3
        const val STATUS_JSON_ERROR = 4
    }

    var status:Int = -1

    var message:String = ""

    var data:Any? = null
}