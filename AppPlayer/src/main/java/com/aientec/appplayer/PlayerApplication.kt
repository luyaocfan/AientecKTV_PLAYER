package com.aientec.appplayer

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aientec.appplayer.activity.LauncherActivity
import java.util.*

class PlayerApplication : Application() {
      override fun attachBaseContext(base: Context?) {
            super.attachBaseContext(base)
//        if (BuildConfig.FLAVOR != "Dev") {
//            initAcra{
//                buildConfigClass = BuildConfig::class.java
//
//                reportFormat = StringFormat.JSON
//
////                mailSender {
////                    val dateFormat: SimpleDateFormat =
////                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
////
////
////                    mailTo = "magician19841125@gmail.com"
////
////                    reportAsFile = true
////
////                    reportFileName = "KTV_PLAYER_REPORT.json"
////
////                    val dateStr: String = dateFormat.format(Date())
////
////                    subject = "App KTV player crash log : $dateStr"
////                }
//            }
//        }

            val intent = Intent(this, LauncherActivity::class.java).apply {
                  addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                  e.printStackTrace()

                  val pendingIntent = PendingIntent.getActivity(
                        this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                  )

                  val mgr: AlarmManager =
                        getSystemService(ALARM_SERVICE) as AlarmManager
                  mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)

                  System.exit(2)
            }
      }
}