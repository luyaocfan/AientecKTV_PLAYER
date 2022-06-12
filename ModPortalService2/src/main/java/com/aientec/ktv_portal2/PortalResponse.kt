package com.aientec.ktv_portal2

sealed class PortalResponse {
    data class Success(val data: Any?) : PortalResponse()
    data class Fail(val msg: String) : PortalResponse()
}
