package com.aientec.ktv_pos_tablet.activity

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.navigation.findNavController
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.ActivityHostBinding
import com.aientec.ktv_pos_tablet.service.KTVService
import com.aientec.ktv_pos_tablet.viewmodel.*
import com.aientec.structure.Reserve

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class HostActivity : AppCompatActivity() {
    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    private lateinit var binding: ActivityHostBinding

    private val hideHandler = Handler()

    private var isViewInit: Boolean = false

    private val boxViewModel: BoxViewModel by viewModels()

    private val floorViewModel: FloorViewModel by viewModels()

    private val markerViewModel: MarkerViewModel by viewModels()

    private val typeViewModel: TypeViewModel by viewModels()

    private val reserveViewModel: ReserveViewModel by viewModels()

    private val orderViewModel: OrderViewModel by viewModels()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {

            val service: KTVService = (binder as KTVService.ServiceBinder).service
            boxViewModel.onServiceConnected(service)
            floorViewModel.onServiceConnected(service)
            markerViewModel.onServiceConnected(service)
            typeViewModel.onServiceConnected(service)
            reserveViewModel.onServiceConnected(service)
            orderViewModel.onServiceConnected(service)

            initView()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(100)
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) != 0)
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, KTVService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }


    private fun initView() {
        if (isViewInit) return

        binding = ActivityHostBinding.inflate(layoutInflater)

        setContentView(binding.root)
        isFullscreen = true

        binding.navigationList.exit.setOnClickListener {
            finish()
        }

        binding.navigationList.menus.setOnCheckedChangeListener { _, checkedId ->
            val dest: Int =
                when (checkedId) {
                    R.id.menu_room -> R.id.roomFragment
                    R.id.menu_order -> R.id.orderFragment
                    R.id.menu_reserve -> R.id.reserveFragment
                    else -> return@setOnCheckedChangeListener
                }

            findNavController(R.id.nav_fragment_host).navigate(dest)
        }

        binding.navigationList.menus.check(R.id.menu_room)

        binding.root.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                ViewModelImpl.toggleNavigationShown(p1 == R.id.start)
            }
        })

        isViewInit = true
    }

    private fun hide() {
        // Hide UI first
        isFullscreen = false
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }


}