package com.aientec.ktv_vod.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.aientec.ktv_vod.common.impl.ActivityImpl
import com.aientec.ktv_vod.databinding.ActivityIdleBinding
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.viewmodel.RoomViewModel
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec

class IdleActivity : ActivityImpl() {
    private lateinit var binding: ActivityIdleBinding

    private val roomViewModel: RoomViewModel by viewModels()

    private val systemViewModel: SystemViewModel by viewModels()

    private val trackViewModel: TrackViewModel by viewModels()

    private lateinit var disconnectDialog: AlertDialog

    private lateinit var player: ExoPlayer

    override fun initViews(savedInstanceState: Bundle?) {
        player = ExoPlayer.Builder(this).build()

        player.repeatMode = ExoPlayer.REPEAT_MODE_ALL

        player.playWhenReady = true

        binding = ActivityIdleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.skip.setOnClickListener {
            systemViewModel.debugOpen()
        }

        disconnectDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Data server disconnected")
            .setCancelable(false)
            .create()


        player.setVideoSurfaceView(binding.display)

        val uri: Uri = Uri.parse("asset:///idle_video.mp4")

        val dataSpec: DataSpec = DataSpec(uri)

        val dataSource: AssetDataSource = AssetDataSource(this)

        val fac: DataSource.Factory = DataSource.Factory {
            return@Factory dataSource
        }

        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(fac).createMediaSource(
            MediaItem.fromUri(uri)
        )

        dataSource.open(dataSpec)

        player.addMediaSource(mediaSource)

        systemViewModel.isOpen.observe(this) {
            if (it) {
                player.stop()
                player.release()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        player.prepare()

        systemViewModel.connectState.observe(this) {
            if (it == null) return@observe
            if (it)
                onReconnected()
            else
                onDisconnected()
        }

        systemViewModel.checkOpenInfo()
    }


    override fun onServiceConnect(service: VodService) {
        roomViewModel.onServiceConnected(service)

        systemViewModel.onServiceConnected(service)

        trackViewModel.onServiceConnected(service)
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
}