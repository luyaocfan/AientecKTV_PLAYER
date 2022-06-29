package com.aientec.player2.ui.componants

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.aientec.player2.BuildConfig
import com.aientec.player2.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import java.util.*

private val playList: List<String> = listOf(
    "https://www.hassen.myds.me/h265_60/CM100001_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100002_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100003_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100004_re.mp4"
)

@Composable
fun MTVContainer(viewModel: PlayerViewModel = PlayerViewModel()) {


    val mContext: Context = LocalContext.current

    val mPlayer = remember(mContext) {
        ExoPlayer.Builder(mContext).build().apply {


            this.playWhenReady = true

            this.repeatMode = Player.REPEAT_MODE_ALL

            Log.d("Trace", "Exo Create")
        }
    }

    viewModel.idleMTVList.observe(LocalLifecycleOwner.current) {
        if (it != null) {

            if (mPlayer.isPlaying) {
                mPlayer.stop()
                mPlayer.clearMediaItems()
            }

            val concatenatingMediaSource = ConcatenatingMediaSource().apply {
                for (track in it) {
                    addMediaSource(
                        generateMediaSource(
                            mContext,
                            String.format(
                                Locale.TAIWAN,
                                BuildConfig.MTV_URL,
                                track.fileName.replace("_re", "")
                            )
                        )
                    )
                }
            }

            mPlayer.addMediaSource(concatenatingMediaSource)

            Log.d("Trace", "Exo Prepare")
            mPlayer.prepare()
        }
    }

    AndroidView(factory = {
        SurfaceView(it).apply {
            Log.d("Trace", "Exo setVideoSurfaceView")
            mPlayer.setVideoSurfaceView(this)
        }
    }, modifier = Modifier.fillMaxSize())
}

private fun generateMediaSource(context: Context, url: String): MediaSource =
    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        .createMediaSource(MediaItem.fromUri(Uri.parse(url)))