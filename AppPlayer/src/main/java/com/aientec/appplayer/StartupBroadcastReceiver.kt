package com.aientec.appplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aientec.appplayer.activity.LauncherActivity

class StartupBroadcastReceiver : BroadcastReceiver() {

      override fun onReceive(context: Context, intent: Intent) {
            // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
            Log.i("Trace", "Action : ${intent.action}")
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                  val serviceIntent = Intent(context, LauncherActivity::class.java)
                  context.startActivity(serviceIntent)
            }
      }
}