package com.aientec.player2.ui.componants

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.aientec.player2.BuildConfig
import com.aientec.player2.data.PlayerControl
import com.aientec.player2.viewmodel.PlayerViewModel
import com.aientec.structure.Track
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
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

/**
 * 播放頁面
 */
@Composable
fun MTVContainer(viewModel: PlayerViewModel = PlayerViewModel()) {

    val mContext: Context = LocalContext.current

    val audioManager: AudioManager =
        LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    adjustAudioVolume(audioManager, true)

    var mController: InePlayerController? = null

    val config: InePlayerController.InePlayerControllerConfigure =
        InePlayerController.InePlayerControllerConfigure().apply {
            this.context = mContext
            maxCacheCount = MAXIMUM_CACHE_COUNT
            itemCacheSize = MAXIMUM_CACHE_SIZE
            cacheBandwidthKBS = CACHE_BANDWIDTH_KBS
            listener = InePlayerEventListener(viewModel, audioManager)
        }

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(key1 = mContext) {


        mController = initPlayer(config)

        val controller = mController ?: return@LaunchedEffect

        viewModel.idleMTVList.observe(lifecycleOwner) {
            if (it != null) {
                updateIdleMtvList(controller, it)
            }
        }

        viewModel.isRoomOpened.observe(lifecycleOwner) {
            if (it) {
                resetPlayer(controller)
                viewModel.nextTrackRequest()
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
                    PlayerControl.CUT -> {
                        if (!controller.isInPublicVideo) {
                            viewModel.onPlayerCut()
                            controller.cut()
                        }
                    }
                    is PlayerControl.MUTE -> {

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            if (it.mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                            0
                        )
                        viewModel.onPlayerMuteToggle(it.mute)
                    }
                    is PlayerControl.PAUSE -> {
                        if (!controller.isPaused) {
                            controller.pause()
                            if (it.osd)
                                viewModel.onPlayerPause(controller)
                            else
                                viewModel.onPlayerPause(null)
                        }
                    }
                    PlayerControl.REPLAY -> {
                        if (!controller.isInPublicVideo) {
                            controller.replay()
                            viewModel.onPlayerReplay()
                        }
                    }
                    PlayerControl.RESUME -> {
                        if (controller.isPaused) {
                            controller.resume()
                            viewModel.onPlayerResume()
                        }
                    }
                    is PlayerControl.VOCAL -> {
                        when (it.type) {
                            1 -> controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_LeftMono)
                            2 -> controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_RightMono)
                        }
                        viewModel.onPlayerVocalChanged(it.type)
                    }
                    is PlayerControl.RATING -> {
                        viewModel.onPlayerRatingToggle(it.enable)
                    }
                }
            }
        }

        viewModel.nextTrackRequest()

    }

    DisposableEffect(mContext) {
        this.onDispose {
            mController?.close()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                config.publicVideoView = this
                this.useController = false
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }, modifier = Modifier
            .fillMaxSize()
    )

    AndroidView(
        factory = {
            PlayerView(it).apply {
                config.orderSongView = this
                this.useController = false
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }, modifier = Modifier
            .fillMaxSize()
    )
//


    DisplayContainer(viewModel)
}

private fun updateIdleMtvList(
    controller: InePlayerController,
    list: List<Track>
) {
    for (track in list) {
        Log.d("Trace", "updateIdleMtvList : ${track.name}")
        controller.AddPubVideo(
            String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
            "${track.name}::${track.performer}::${track.sn}"
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

private fun resetPlayer(controller: InePlayerController) {
    for (i in 1 until controller.GetOrderSongPlayList().size)
        controller.DeleteOrderSong(i)
    controller.cut()
}

private class InePlayerEventListener(val viewModel: PlayerViewModel, val am: AudioManager) :
    InePlayerController.EventListen {


    override fun onOrderSongFinish(controller: InePlayerController?) {
        super.onOrderSongFinish(controller)
    }

    override fun onStop(
        controller: InePlayerController?,
        Name: String?,
        isPublicVideo: Boolean
    ) {
        super.onStop(controller, Name, isPublicVideo)
        Log.e(TAG, "onStop : $Name, $isPublicVideo")
        if (!isPublicVideo) {
            viewModel.onPlayerEnd()
            adjustAudioVolume(am, true)
        }
    }

    override fun onNext(
        controller: InePlayerController?,
        Name: String?,
        isPublicVideo: Boolean
    ) {
        super.onNext(controller, Name, isPublicVideo)
        Log.e(TAG, "onNext : $Name, $isPublicVideo")
        if (!isPublicVideo) {
            viewModel.onPlayerStart()
            adjustAudioVolume(am, false)
        }
    }

    override fun onNextSongDisplay(controller: InePlayerController?, Name: String?) {
        super.onNextSongDisplay(controller, Name)
        Log.e(TAG, "onNextSongDisplay : $Name")
    }

    override fun onPlayListChange(controller: InePlayerController?, isPublicVideo: Boolean) {
        super.onPlayListChange(controller, isPublicVideo)
    }

    override fun onLoadingError(
        controller: InePlayerController?,
        Name: String?,
        isPublicVideo: Boolean
    ) {
        super.onLoadingError(controller, Name, isPublicVideo)
        viewModel.onToast("onLoadingError ${if (isPublicVideo) "公播" else "點播"} $Name")
        if (!isPublicVideo) {
            //controller?.cut()
            //viewModel.onPlayerCut()
            viewModel.onPlayerEnd()
            adjustAudioVolume(am, true)
        }
    }

    override fun onPlayingError(
        controller: InePlayerController?,
        Name: String?,
        Message: String?,
        isPublicVideo: Boolean
    ) {
        super.onPlayingError(controller, Name, Message, isPublicVideo)
        viewModel.onToast("onPlayingError ${if (isPublicVideo) "公播" else "點播"} $Name : $Message")
        if (!isPublicVideo) {
            //controller?.cut()
            //viewModel.onPlayerCut()
            viewModel.onPlayerEnd()
            adjustAudioVolume(am, true)
        }
    }

    override fun onRemovePrepareErrorOrderSong(
        controller: InePlayerController?,
        Name: String?,
        url: String?
    ) {
        super.onRemovePrepareErrorOrderSong(controller, Name, url)
        viewModel.onToast("onRemovePrepareErrorOrderSong $Name, $url")
    }

    override fun onRemovePrepareErrorPublicVideo(
        controller: InePlayerController?,
        Name: String?,
        url: String?
    ) {
        super.onRemovePrepareErrorPublicVideo(controller, Name, url)
        viewModel.onToast("onRemovePrepareErrorPublicVideo $Name, $url")
    }

    override fun onOrderSongAudioChannelMappingChanged(
        controller: InePlayerController?,
        type: Int
    ) {
        super.onOrderSongAudioChannelMappingChanged(controller, type)
    }
}

private fun adjustAudioVolume(am: AudioManager, isIdle: Boolean) {
    val isMute = am.isStreamMute(AudioManager.STREAM_MUSIC)

    val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val volume: Int = if (isIdle) (0.5f * maxVolume.toFloat()).toInt() else maxVolume

    am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

    am.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        if (isMute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
        0
    )
}
