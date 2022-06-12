package com.aientec.ktv_pantry.model

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import com.aientec.structure.Meals
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.usb.UsbConnection
import idv.bruce.common.impl.ModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Hardware private constructor(context: Context) : ModelImpl(context) {
    companion object {
        private var instance: Hardware? = null

        fun getInstance(context: Context): Hardware {
            if (instance == null)
                instance = Hardware(context)
            return instance!!
        }
    }


    override val tag: String
        get() = "HW"

    private lateinit var usbManager: UsbManager

    private var linkedPrinter: UsbDevice? = null

    private var printerInstance: EscPosPrinter? = null

    init {


    }

    override fun init() {
        usbManager = contextRef.get()!!.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    override fun release() {

    }

    suspend fun linkPrinter(): Boolean = suspendCoroutine {


        val deviceList = usbManager.deviceList

        for (dev in deviceList.values) {
            if (checkUsbClass(dev, 7)) {
                linkedPrinter = dev
                break
            }
        }

        if (linkedPrinter == null) {
            it.resume(false)
            return@suspendCoroutine
        }

        if (!usbManager.hasPermission(linkedPrinter!!)) {
            val usbPermission = "com.android.example.USB_PERMISSION"

            val pendingIntent = PendingIntent.getBroadcast(
                contextRef.get()!!, 8891, Intent(usbPermission), PendingIntent.FLAG_UPDATE_CURRENT
            )

            val filter: IntentFilter = IntentFilter(usbPermission)

            contextRef.get()!!.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    contextRef.get()!!.unregisterReceiver(this)

                    var res: Boolean =
                        p1!!.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                    if (res) {
                        res = bindPrinterInstance(linkedPrinter!!)
                    }

                    it.resume(res)
                }
            }, filter)

            usbManager.requestPermission(linkedPrinter!!, pendingIntent)
        } else {
            bindPrinterInstance(linkedPrinter!!)
            it.resume(true)
        }
    }

    suspend fun printMealItem(vararg meals: Meals) = withContext(Dispatchers.IO) {
        Log.d(tag, "Print : ${printerInstance.toString()}, $linkedPrinter")
        val stringBuilder: StringBuilder = StringBuilder()

        for (item in meals) {
            stringBuilder.append(
                "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n" +
                        "[L]<b>${item.name} : ${item.count}</b>\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n"
            )
        }
        Log.d(tag, "Print : ${stringBuilder.toString()}")


        printerInstance?.printFormattedTextAndCut(
            stringBuilder.toString()
        )
    }


    private fun bindPrinterInstance(device: UsbDevice): Boolean {
        printerInstance = EscPosPrinter(
            UsbConnection(usbManager, device),
            203,
            48f,
            32, EscPosCharsetEncoding("GB2312", 16)
        )

        printerInstance?.printFormattedTextAndCut(
            "[L]\n" +
                    "[C]================================\n" +
                    "[L]\n" +
                    "[L]<b>SYSTEM READY</b>\n" +
                    "[L]\n" +
                    "[C]================================\n" +
                    "[L]\n"
        )

        return true
    }

    private fun checkUsbClass(device: UsbDevice, subClass: Int): Boolean {
        if (device.deviceSubclass == subClass) return true
        if (device.deviceSubclass != 0 && device.deviceSubclass != 2) return false
        var flag: Boolean = false
        var usbInterface: UsbInterface
        for (i in 0 until device.interfaceCount) {
            usbInterface = device.getInterface(i)
            if (usbInterface.interfaceClass == subClass) {
                flag = true
                break
            }
        }
        return flag
    }
}