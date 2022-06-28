package com.aientec.appplayer.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.aientec.appplayer.databinding.ActivityMainBinding
import com.aientec.appplayer.model.Repository
import com.aientec.appplayer.viewmodel.DebugViewModel
import com.aientec.appplayer.viewmodel.OsdViewModel
import com.aientec.appplayer.viewmodel.SystemViewModel
import com.aientec.appplayer.viewmodel.PlayerViewModel
import org.acra.util.ToastSender

class MainActivity : AppCompatActivity() {
      private lateinit var binding: ActivityMainBinding

      private lateinit var repository: Repository

      private val systemViewModel: SystemViewModel by viewModels()

      private val playerViewModel: PlayerViewModel by viewModels()

      private val osdViewModel: OsdViewModel by viewModels()

      private val debugViewModel: DebugViewModel by viewModels()

      private lateinit var disconnectDialog: AlertDialog


      private fun hideSystemBars() {
//            val windowInsetsController =
//                  ViewCompat.getWindowInsetsController(window.decorView) ?: return
//            // Configure the behavior of the hidden system bars
//            windowInsetsController.systemBarsBehavior =
//                  WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            // Hide both the status bar and the navigation bar
//            windowInsetsController.isAppearanceLightNavigationBars = false
//            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

            window.decorView.apply {
                  // Hide both the navigation bar and the status bar.
                  // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
                  // a general rule, you should design your app to hide the status bar whenever you
                  // hide the navigation bar.
                  systemUiVisibility =
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
      }

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)


            hideSystemBars()


            repository = Repository.getInstance(applicationContext)

            repository.init()

            systemViewModel.onRepositoryAttach(repository)

            playerViewModel.onRepositoryAttach(repository)

//            osdViewModel.onRepositoryAttach(repository)
//
//            debugViewModel.onRepositoryAttach(repository)

            systemViewModel.isDataSyn.observe(this) {
                  val res: Boolean = it ?: return@observe
                  if (res) {
                        binding = ActivityMainBinding.inflate(layoutInflater)

                        setContentView(binding.root)

                        binding.test.setOnClickListener {
                              osdViewModel.test()
                        }

                        disconnectDialog = AlertDialog.Builder(this)
                              .setTitle("Error")
                              .setMessage("Data server disconnected")
                              .setCancelable(false)
                              .create()

                        systemViewModel.connectionState.observe(this) { state ->
                              if (state == null) return@observe
                              if (state)
                                    onReconnected()
                              else
                                    onDisconnected()
                        }
                  } else {
                        Toast.makeText(this, "Server connect failed.", Toast.LENGTH_SHORT).show()
                  }
            }


      }

      override fun onResume() {
            super.onResume()

            systemViewModel.onApplicationInit()
      }

      private fun onReconnected() {
            if (disconnectDialog.isShowing)
                  disconnectDialog.dismiss()
      }

      private fun onDisconnected() {
            if (!disconnectDialog.isShowing)
                  disconnectDialog.show()
      }
}