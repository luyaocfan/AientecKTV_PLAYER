package com.aientec.ktv_vod.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.impl.ActivityImpl
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.databinding.ActivityMainBinding
import com.aientec.ktv_vod.fragment.main.WebFragment
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.viewmodel.*

class MainActivity : ActivityImpl() {
      private lateinit var binding: ActivityMainBinding

      private val pages: ArrayList<Fragment> = ArrayList()

      private val roomViewModel: RoomViewModel by viewModels()

      private val controlViewModel: ControlViewModel by viewModels()

      private val systemViewModel: SystemViewModel by viewModels()

      private val trackViewModel: TrackViewModel by viewModels()

      private val uiViewModel: UiViewModel by viewModels()

      private val userViewModel: UserViewModel by viewModels()

      private val albumViewModel: AlbumViewModel by viewModels()

      private val singerViewModel: SingerViewModel by viewModels()

      private val searchViewModel: SearchViewModel by viewModels()

      private lateinit var disconnectDialog: AlertDialog

      override fun initViews(savedInstanceState: Bundle?) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            disconnectDialog = AlertDialog.Builder(this)
                  .setTitle("Error")
                  .setMessage("Data server disconnected")
                  .setCancelable(false)
                  .create()


            ViewModelImpl.toast.observe(this) {
                  if (it == null) return@observe
                  Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }

            systemViewModel.connectState.observe(this) {
                  if (it == null) return@observe
                  if (it)
                        onReconnected()
                  else
                        onDisconnected()
            }

            findNavController(R.id.nav_fragment_main).addOnDestinationChangedListener { _, destination, _ ->
//                  uiViewModel.onMainPageUpdate(destination.id)

                  showBackButton(destination.id != R.id.homeFragment)


            }

            uiViewModel.controlAction.observe(this) {
                  onControlActionEvent(it.first, it.second)
            }

            binding.back.setOnClickListener {
                  onBackPressed()
            }
      }


      override fun onServiceConnect(service: VodService) {
            roomViewModel.onServiceConnected(service)

            controlViewModel.onServiceConnected(service)

            systemViewModel.onServiceConnected(service)

            trackViewModel.onServiceConnected(service)

            userViewModel.onServiceConnected(service)

            albumViewModel.onServiceConnected(service)

            singerViewModel.onServiceConnected(service)

            searchViewModel.onServiceConnected(service)

            systemViewModel.refreshWifiAp()

            systemViewModel.readConfiguration()


            systemViewModel.isOpen.observe(this) {
                  if (it == false) {
                        startActivity(Intent(this, IdleActivity::class.java))
                        finish()
                  }
            }
      }

      override fun onServiceDisconnect() {
      }

      override fun getServiceStartFlag(): Int {
            return SERVICE_START_MODE_BIND
      }

      override fun getViewInitFlag(): Int {
            return UI_MODE_SET_ON_SERVICE_CONNECTED
      }

      private fun onReconnected() {
            if (disconnectDialog.isShowing)
                  disconnectDialog.dismiss()
      }

      private fun onDisconnected() {
            if (!disconnectDialog.isShowing)
                  disconnectDialog.show()
      }

      private fun onControlActionEvent(action: String, bundle: Bundle?) {
            Log.d("Trace", "Action : $action")
            showBackButton(true)
            when (action.removePrefix("ACTION:")) {
                  "HOME" -> {
                        showBackButton(false)
                        updateContent(R.id.homeFragment, bundle)
                  }
                  "FOOD_ORDER" -> {
                        val roomId = systemViewModel.roomId

                        val uuid = systemViewModel.uuid

                        val mBundle: Bundle =
                              bundleOf("url" to "${BuildConfig.WEB_ROOT}index.php/VodFood/index?boxId=$roomId&devId=$uuid")

                        updateContent(R.id.webFragment, mBundle)
                  }
                  "DISCOUNT" -> {
                        val mBundle: Bundle =
                              bundleOf("url" to "${BuildConfig.WEB_ROOT}index.php/VodDiscount")

                        updateContent(R.id.webFragment, mBundle)
                  }
                  "SCREEN_MIRROR" -> {
                        val roomId = systemViewModel.roomId

                        val srcType = controlViewModel.getAudioSrc()
                        val mBundle: Bundle =
                              bundleOf("url" to "${BuildConfig.WEB_ROOT}index.php/VodProjteaching?boxId=$roomId&vstate=$srcType")

                        updateContent(R.id.webFragment, mBundle)
                  }
                  "SONG_SEARCHING" -> {
                        updateContent(R.id.searchMenuFragment)
                  }
                  "SONG_SEARCHING_RESULT" -> {
                        updateContent(R.id.searchResultFragment)
                  }
                  "SONG_CHARTS" -> {
                        val mBundle: Bundle =
                              bundleOf("type" to 2)
                        updateContent(R.id.songListFragment, mBundle)
                  }
                  "NEW_SONGS" -> {
                        val mBundle: Bundle =
                              bundleOf("type" to 3)
                        updateContent(R.id.songListFragment, mBundle)
                  }
                  "SONG_THEMES" -> {
                        val mBundle: Bundle =
                              bundleOf("type" to 4)
                        updateContent(R.id.songListFragment, mBundle)
                  }
                  "ED_SONG_CHARTS" -> {
                        val mBundle: Bundle =
                              bundleOf("type" to 5)
                        updateContent(R.id.songListFragment, mBundle)
                  }
                  "TRACKS" -> {
                        updateContent(R.id.trackListFragment2)
                  }
                  "PLAY_LIST" -> {
                        updateContent(R.id.playlistFragment)
                  }
            }
      }

      override fun onBackPressed() {
            if (findNavController(R.id.nav_fragment_main).currentDestination?.id == R.id.webFragment) {
                  val navHostFragment: Fragment? =
                        supportFragmentManager.findFragmentById(R.id.nav_fragment_main)

                  if ((navHostFragment?.childFragmentManager?.fragments?.get(0) as WebFragment).back())
                        super.onBackPressed()

            } else {
                  super.onBackPressed()
            }
      }

      private fun updateContent(distId: Int, bundle: Bundle? = null) {
            findNavController(R.id.nav_fragment_main).navigate(distId, bundle)
      }

      private fun showBackButton(canBack: Boolean) {
            binding.back.visibility = if (canBack) View.VISIBLE else View.GONE
      }
}