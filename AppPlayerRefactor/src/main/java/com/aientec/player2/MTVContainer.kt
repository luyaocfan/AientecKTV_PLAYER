package com.aientec.player2

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

private val playList: List<String> = listOf(
    "https://www.hassen.myds.me/h265_60/CM100001_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100002_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100003_re.mp4",
    "https://www.hassen.myds.me/h265_60/CM100004_re.mp4"
)

@Composable
fun MTVContainer() {
    val mContext: Context = LocalContext.current

    val mPlayer = remember(mContext) {
        ExoPlayer.Builder(mContext).build().apply {
            val concatenatingMediaSource = ConcatenatingMediaSource().apply {
                for (url in playList)
                    addMediaSource(generateMediaSource(mContext, url))
            }

            this.playWhenReady = true

            this.repeatMode = Player.REPEAT_MODE_ALL

            this.addMediaSource(concatenatingMediaSource)

            this.prepare()
        }
    }

    AndroidView(factory = {
        SurfaceView(it).apply {
            mPlayer.setVideoSurfaceView(this)
        }
    }, modifier = Modifier.fillMaxSize())
}

private fun generateMediaSource(context: Context, url: String): MediaSource =
    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        .createMediaSource(MediaItem.fromUri(Uri.parse(url)))