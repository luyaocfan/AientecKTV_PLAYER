package com.aientec.appplayer.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.aientec.appplayer.databinding.ActivityLauncherBinding
import idv.bruce.ui.osd.container.MarqueView

class LauncherActivity : AppCompatActivity() {
      private lateinit var binding: ActivityLauncherBinding

      private val permissions: Array<String> = arrayOf(android.Manifest.permission.DUMP)

      private val REQ_CODE: Int = 1234

      private lateinit var connectivityManager: ConnectivityManager

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            connectivityManager =
                  getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      }

      override fun onResume() {
            super.onResume()
            if (checkPermissions()) {
                  if (checkNetworkConnected()) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                  } else {
                        onNetworkNotAvailable()
                  }
            } else {
                  requestPermissions(permissions, REQ_CODE)
            }
      }

      private fun onNetworkNotAvailable() {


            val networkRequest = NetworkRequest.Builder()
                  .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                  .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                  .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                  .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
      }

      private fun checkPermissions(): Boolean {
            return true
      }

      @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
      private fun checkNetworkConnected(): Boolean {
            var result = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  val networkCapabilities = connectivityManager.activeNetwork ?: return false
                  val actNw =
                        connectivityManager.getNetworkCapabilities(networkCapabilities)
                              ?: return false
                  result = when {
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                  }
            } else {
                  connectivityManager.run {
                        connectivityManager.activeNetworkInfo?.run {
                              result = when (type) {
                                    ConnectivityManager.TYPE_WIFI -> true
                                    ConnectivityManager.TYPE_MOBILE -> true
                                    ConnectivityManager.TYPE_ETHERNET -> true
                                    else -> false
                              }

                        }
                  }
            }

            return result
      }

      private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                  super.onAvailable(network)
                  if (checkNetworkConnected()) {
                        startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                        finish()
                        connectivityManager.unregisterNetworkCallback(this)
                  }
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                  network: Network,
                  networkCapabilities: NetworkCapabilities
            ) {
                  super.onCapabilitiesChanged(network, networkCapabilities)
            }

            // lost network connection
            override fun onLost(network: Network) {
                  super.onLost(network)
            }
      }
}