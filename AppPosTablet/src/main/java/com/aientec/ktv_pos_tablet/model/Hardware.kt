package com.aientec.ktv_pos_tablet.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import be.teletask.onvif.DiscoveryManager
import be.teletask.onvif.listeners.DiscoveryListener
import be.teletask.onvif.models.Device
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.ktv_pos_tablet.structure.Order
import com.aientec.structure.Reserve
import com.aientec.structure.Room
import com.sunmi.peripheral.printer.*
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log

class Hardware(context: Context) : Model(context) {
    var cams: MutableList<Device>? = null

    private var printer: SunmiPrinterService? = null

    private lateinit var logo: Bitmap

    override fun init() {
        val result = InnerPrinterManager.getInstance().bindService(
            contextRef.get()!!,
            object : InnerPrinterCallback() {
                override fun onConnected(service: SunmiPrinterService?) {
                    printer = service
                    printer?.printerInit(null)
                }

                override fun onDisconnected() {

                }
            })


        logo = BitmapFactory.decodeStream(contextRef.get()!!.assets.open("img_bill_logo_l.png"))

        Log.d("Trace", "Logo : ${logo.width}, ${logo.height}, ${logo.byteCount}")
    }

    override fun release() {

    }

    suspend fun printOrder(box: Box, reserve: Reserve, type: Room.Type): Boolean =
        suspendCoroutine {
            try {
                if (printer == null) return@suspendCoroutine it.resume(false)
                val dateFormat: SimpleDateFormat =
                    SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.TAIWAN)
                printer!!.apply {
                    printerInit(null)
                    enterPrinterBuffer(true)
                    setPrinterStyle(WoyouConsts.ENABLE_ANTI_WHITE, WoyouConsts.ENABLE)
                    setAlignment(1, null)
                    setFontSize(52.0f, null)
                    printText("     theLOOP KTV     \n", null)
                    lineWrap(2, null)

                    setPrinterStyle(WoyouConsts.ENABLE_ANTI_WHITE, WoyouConsts.DISABLE)
                    setAlignment(0, null)
                    setFontSize(48.0f, null)
                    printText("台北店貴賓新開立包廂\n", null)

                    setFontSize(24.0f, null)
                    setAlignment(0, null)
                    lineWrap(1, null)
                    printText("::::::::::::::::::::::::::::::::::::::::::::::::\n", null)
                    setFontSize(12.0f, null)
                    lineWrap(1, null)

                    setFontSize(48.0f, null)
                    printText("時間: ${dateFormat.format(Date())}\n", null)

                    setFontSize(24.0f, null)
                    printText("::::::::::::::::::::::::::::::::::::::::::::::::\n", null)
                    lineWrap(1, null)

                    setFontSize(48.0f, null)
                    printText("包廂號碼: ", null)
                    setFontSize(108.0f, null)
                    printText("${box.name}\n", null)
                    setFontSize(48.0f, null)
                    printText("樓    層: ", null)
                    setFontSize(108.0f, null)
                    printText("${box.floor}F\n", null)
                    setFontSize(48.0f, null)

                    val jsonObj: JSONObject = JSONObject().apply {
                        this.put("ROOM", box.name)
                        this.put("USER", reserve.memberName)
                    }
                    lineWrap(1, null)
                    printQRCode(jsonObj.toString(), 7, 3, null)
                    lineWrap(1, null)

                    printText("包廂密碼: 78763\n", null)
                    lineWrap(1, null)
                    printText("貴賓人數: ${reserve.personCount}\n", null)
                    lineWrap(1, null)
                    printText("預約編號: 30678\n", null)
                    lineWrap(1, null)
                    printText("包廂類型: ${type.name}\n", null)
                    lineWrap(1, null)

                    setFontSize(24.0f, null)
                    printText("------------------------------------------------\n", null)

                    setFontSize(24.0f, null)
                    printText("13:00 ~ 15:00 K歌段 4H/259(平日)\n", null)
                    setFontSize(24.0f, null)
                    printText("※  若因蛋糕，酒水等其他因素造成汙損，將酌收清潔費1000元起，造成不便請多包涵\n", null)
                    lineWrap(1, null)

                    setFontSize(36.0f, null)
                    printText(
                        "────────────────────────────────\n", null
                    )
                    setFontSize(36.0f, null)
                    printText("警政署提醒您:\n", null)
                    setFontSize(24.0f, null)
                    printText(
                        "※  凡接獲來電，以升級會員、被盜帳號、訂單金額錯誤等理由，要求您操作提款機ATM解除設定、線上進行轉帳、詢問信用卡或銀行帳戶等資訊，一定是詐騙，請立即掛斷電話或撥打「165」反詐騙專線查詢，謹慎以防被騙！\n",
                        null
                    )
                    lineWrap(2, null)
                    setFontSize(36.0f, null)
                    printText(
                        "────────────────────────────────\n", null
                    )
                    setFontSize(36.0f, null)
                    printText("提醒您:\n", null)
                    setFontSize(24.0f, null)
                    printText("※  本場所全面禁菸，經相關單位稽核違規者，逕處兩千至一萬元罰鍰。\n", null)

                    lineWrap(3, null)
                    cutPaper(null)

                    commitPrinterBufferWithCallback(callback)

                    it.resume(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                it.resume(false)
            }

        }

    suspend fun testPrint(): Boolean = suspendCoroutine {
        if (printer == null) return@suspendCoroutine it.resume(false)

        printer!!.apply {
            printerInit(null)
            enterPrinterBuffer(true)
            printText("Print 0", null)
            printBitmapCustom(logo, 0, null)
            printText("Print 1", null)
            printBitmapCustom(logo, 1, null)
            printText("Print 2", null)
            printBitmapCustom(logo, 2, null)
            lineWrap(3, null)
            cutPaper(null)
            commitPrinterBufferWithCallback(callback)
            it.resume(true)
        }
    }

    suspend fun printBill(box: Box): Boolean = suspendCoroutine {
        try {
            if (printer == null) return@suspendCoroutine it.resume(false)

            val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.TAIWAN)


            printer!!.apply {
                printerInit(null)
                enterPrinterBuffer(true)

                setPrinterStyle(WoyouConsts.ENABLE_ANTI_WHITE, WoyouConsts.ENABLE)
                setAlignment(1, null)
                setFontSize(52.0f, null)
                printText("     theLOOP KTV     \n", null)
                lineWrap(2, null)

                setPrinterStyle(WoyouConsts.ENABLE_ANTI_WHITE, WoyouConsts.DISABLE)
                setAlignment(0, null)
                setFontSize(48.0f, null)
                printText("台北店貴賓消費結帳單\n", null)
                setFontSize(24.0f, null)
                setAlignment(0, null)
                printText("::::::::::::::::::::::::::::::::::::::::::::::::\n", null)

                val format: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.TAIWAN)

                setFontSize(28.0f, null)
                printColumnsText(
                    arrayOf("櫃台: 03-早班", dateFormat.format(Date())),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )
                printColumnsText(
                    arrayOf("帳編: 210611021451", "操作: 收銀#3710"),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )
                printColumnsText(
                    arrayOf("包廂: ${box.name}", "人數: 5"),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )
                printColumnsText(
                    arrayOf(
                        "進場: ",
                        "退房: ${format.format(Date())}"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )
                printColumnsText(
                    arrayOf(
                        "起算: ",
                        "離場: ${format.format(Date())}"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )
                printColumnsText(
                    arrayOf(
                        "使用: ",
                        "續時: 00:00"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )

                printText("-----------------------------------------\n", null)

                printColumnsText(
                    arrayOf(
                        "歡唱: ${String.format(Locale.TAIWAN, "% 7d", 547)}",
                        "場租: ${String.format(Locale.TAIWAN, "% 7d", 0)}"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )

                printColumnsText(
                    arrayOf(
                        "餐飲: ${String.format(Locale.TAIWAN, "% 7d", 520)}",
                        "公賣: ${String.format(Locale.TAIWAN, "% 7d", 0)}"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )

                printColumnsText(
                    arrayOf(
                        "其他: ${String.format(Locale.TAIWAN, "% 7d", 0)}",
                        "清潔: ${String.format(Locale.TAIWAN, "% 7d", 0)}"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )

                printColumnsText(
                    arrayOf("服務: ${String.format(Locale.TAIWAN, "% 7d", 106)}"),
                    IntArray(1) { 24 },
                    IntArray(1) { 0 },
                    null
                )

                printColumnsText(
                    arrayOf("總額: ${String.format(Locale.TAIWAN, "% 7d", 1173)}"),
                    IntArray(1) { 20 },
                    IntArray(1) { 0 },
                    null
                )

                printColumnsText(
                    arrayOf("折扣: ${String.format(Locale.TAIWAN, "% 7d", 0)}"),
                    IntArray(1) { 20 },
                    IntArray(1) { 0 },
                    null
                )

                setFontSize(36.0f, null)
                printText(
                    "────────────────────────────────\n", null
                )

                printText("帳單金額: 1174\n", null)

                setFontSize(28.0f, null)
                printText("318 4115 明碼消費(平日)\n", null)
                printText("15:40 ~ 1740 \$537\n", null)

                lineWrap(1, null)
                setFontSize(36.0f, null)
                printText("應付金額: 1174 -\n", null)

                setFontSize(28.0f, null)
                printText("(支援多種支付方式)\n", null)

                printText(".........................................", null)

                printColumnsString(
                    arrayOf("菜單名稱", "數量", "金額"),
                    IntArray(3) { 13 },
                    IntArray(3) { 0 },
                    null
                )
                printColumnsString(
                    arrayOf("烏龍茶", "1", "220"),
                    IntArray(3) { 13 },
                    IntArray(3) { 0 },
                    null
                )
                printColumnsString(
                    arrayOf("啤酒", "3", "300"),
                    IntArray(3) { 13 },
                    IntArray(3) { 0 },
                    null
                )

                lineWrap(3, null)

                printText("收款人: ____________\n", null)

                lineWrap(1, null)
                printColumnsText(
                    arrayOf(
                        "現金  : ____________",
                        "信用卡: ____________"
                    ),
                    IntArray(2) { 20 },
                    IntArray(2) { 0 },
                    null
                )

                printText(":::::::::::::::::::::::::::::::::::::::::\n", null)

                printBarCode("210611021451", 4, 109, 2, 0, null)

                lineWrap(3, null)
                printQRCode("010611021451", 8, 3, null)

                lineWrap(1, null)

                printBitmapCustom(logo, 1, null)

                lineWrap(3, null)
                cutPaper(null)
                commitPrinterBufferWithCallback(callback)

                it.resume(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            it.resume(false)
        }
    }

    private val callback: InnerResultCallback = object : InnerResultCallback() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.d("Trace", "onRunResult : $isSuccess")

        }

        override fun onReturnString(result: String?) {
            Log.d("Trace", "onReturnString : $result")
        }

        override fun onRaiseException(code: Int, msg: String?) {
            Log.d("Trace", "onRaiseException : $code, $msg")
        }

        override fun onPrintResult(code: Int, msg: String?) {
            Log.d("Trace", "onPrintResult : $code, $msg")
        }


    }
}