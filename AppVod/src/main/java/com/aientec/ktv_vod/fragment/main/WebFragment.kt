package com.aientec.ktv_vod.fragment.main

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.aientec.ktv_vod.databinding.FragmentWebBinding
import java.net.URI
import java.net.URL
import kotlin.concurrent.timerTask

class WebFragment : Fragment() {
      private lateinit var binding: FragmentWebBinding

      private var url: String = ""

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentWebBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.root.settings.apply {
                  javaScriptEnabled = true
                  javaScriptCanOpenWindowsAutomatically = true
                  pluginState = WebSettings.PluginState.ON
                  setSupportZoom(false)
                  cacheMode = WebSettings.LOAD_NO_CACHE
                  mediaPlaybackRequiresUserGesture = false

            }

            binding.root.setBackgroundColor(Color.TRANSPARENT)

            binding.root.webViewClient = object : WebViewClient() {
                  override fun onPageFinished(view: WebView?, url: String?) {

                  }

                  override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                  ): Boolean {
                        val uri: Uri? = request?.url

                        Log.d("Trace", "Uri : $uri")

                        binding.root.loadUrl(uri.toString())
                        return true
                  }
            }
            binding.root.webChromeClient = object : WebChromeClient() {


                  override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("Trace", "onConsoleMessage : ${consoleMessage?.message()}")
                        return super.onConsoleMessage(consoleMessage)
                  }

            }
      }


      override fun onResume() {
            super.onResume()

            if (arguments != null) {
                  url = requireArguments().getString("url") ?: "error"
                  Log.d("Trace", "onResume url : $url")
//                  binding.root.loadUrl(url)
            } else
                  Log.d("Trace", "onResume url : null")


            binding.root.loadUrl(url)
      }

      fun back(): Boolean {
            return if (binding.root.canGoBack()) {
                  binding.root.goBack()
                  false
            } else {
                  true
            }
      }
}