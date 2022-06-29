package com.aientec.player2.ui.componants

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.aientec.player2.BuildConfig
import com.aientec.player2.R
import com.aientec.player2.data.PlayerControl
import com.aientec.player2.viewmodel.PlayerViewModel
import com.aientec.structure.Track
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.ine.ktv.playerengine.InePlayerController
import java.util.*

private val playList: List<String> = listOf(
    "https://www.hassen.myds.me/h265_60/CM100001_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100002_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100003_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100004_re.mp4"
)

const val MAXIMUM_CACHE_COUNT: Int = 4

const val MAXIMUM_CACHE_SIZE: Int = 1024 * 1024 * 16

const val PLAYING_BUFFER_SIZE: Int = MAXIMUM_CACHE_SIZE * 2

val CACHE_BANDWIDTH_KBS = intArrayOf(4096, 1024, 512, 256)

const val TAG: String = "MTVContainer"

@Composable
fun MTVContainer(viewModel: PlayerViewModel = PlayerViewModel()) {

    val mContext: Context = LocalContext.current

    var controller: InePlayerController

    val config: InePlayerController.InePlayerControllerConfigure =
        InePlayerController.InePlayerControllerConfigure().apply {
            this.context = mContext
            maxCacheCount = MAXIMUM_CACHE_COUNT
            itemCacheSize = MAXIMUM_CACHE_SIZE
            cacheBandwidthKBS = CACHE_BANDWIDTH_KBS
            listener = object : InePlayerController.EventListen {

            }
        }

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(key1 = mContext) {

        controller = initPlayer(config)

        viewModel.idleMTVList.observe(lifecycleOwner) {
            if (it != null) {
                updateIdleMtvList(controller, it)
            }
        }

        viewModel.nextTrack.observe(lifecycleOwner) {
            if (it != null) {
                updateOrderSong(controller, it)
            }
        }

        viewModel.playerControl.observe(lifecycleOwner) {
            if (it != null) {
                when (it) {
                    PlayerControl.CUT -> controller.cut()
                    is PlayerControl.MUTE -> {}
                    PlayerControl.PAUSE -> controller.pause()
                    PlayerControl.REPLAY -> controller.replay()
                    PlayerControl.RESUME -> controller.pause()
                    is PlayerControl.VOCAL -> {
                        when (it.type) {
                            1 -> controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_LeftMono)
                            2 -> controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_RightMono)
                        }
                    }
                }
            }
        }

        viewModel.onNextReq()

    }

    AndroidView(
        factory = {
            SurfaceView(it).apply {
                config.orderSongView = this
            }
        }, modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    )

    AndroidView(
        factory = {
            SurfaceView(it).apply {
                config.publicVideoView = this
            }
        }, modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    )

    OsdContainer(viewModel)

}

private fun updateIdleMtvList(
    controller: InePlayerController,
    list: List<Track>
) {
    for (track in list) {
        Log.d("Trace", "updateIdleMtvList : ${track.name}")
        controller.AddPubVideo(
            String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
            track.name
        )
    }

    controller.open()
}

private fun updateOrderSong(controller: InePlayerController, track: Track) {
    val playList = controller.GetOrderSongPlayList()

    Log.d(
        TAG,
        "${track.name} : ${
            String.format(
                Locale.TAIWAN,
                BuildConfig.MTV_URL,
                track.fileName
            )
        }"
    )

    if (playList.size > 1)
        controller.DeleteOrderSong(1)

    controller.AddOrderSong(
        String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
        track.name,
        PLAYING_BUFFER_SIZE
    )
}

private fun initPlayer(configure: InePlayerController.InePlayerControllerConfigure): InePlayerController {

    val controller: InePlayerController = InePlayerController(configure)

    controller.SetVODServerList(Array<String>(1) {
        BuildConfig.MTV_URL.replace("%s", "")
    })

    return controller
}

private val eventListener:InePlayerController.EventListen = object :InePlayerController.EventListen{
    override fun onPlayListChange(controller: InePlayerController?) {
        super.onPlayListChange(controller)
    }

    override fun onOrderSongFinish(controller: InePlayerController?) {
        super.onOrderSongFinish(controller)
    }

    override fun onStop(controller: InePlayerController?, Name: String?, isPublicVideo: Boolean) {
        super.onStop(controller, Name, isPublicVideo)
    }

    override fun onNext(controller: InePlayerController?, Name: String?, isPublicVideo: Boolean) {

    }

    override fun onNextSongDisplay(controller: InePlayerController?, Name: String?) {
        super.onNextSongDisplay(controller, Name)
    }

    override fun onLoadingError(controller: InePlayerController?, Name: String?) {
        super.onLoadingError(controller, Name)
    }

    override fun onPlayingError(controller: InePlayerController?, Name: String?, Message: String?) {
        super.onPlayingError(controller, Name, Message)
    }

    override fun onAudioChannelMappingChanged(controller: InePlayerController?, type: Int) {
        super.onAudioChannelMappingChanged(controller, type)
    }
}