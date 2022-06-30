package com.aientec.player2.ui.componants

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.SurfaceView
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import com.aientec.player2.data.MessageBundle
import com.aientec.player2.ui.theme.AientecKTV_PLAYERTheme
import com.aientec.player2.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.linecorp.apng.ApngDrawable
import com.linecorp.apng.RepeatAnimationCallback
import java.io.File
import java.io.IOException
import java.util.*

@Composable
fun OSDContainer(viewModel: PlayerViewModel = PlayerViewModel()) {
    val mLifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val osdMessage by viewModel.osdMessage.observeAsState(null)

    when (osdMessage?.type ?: return) {
        MessageBundle.Type.IMAGE -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                OsdPicture(osdMessage!!.data as String)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        viewModel.onOsdDone()
                    }
                }, 5000L)
            }
        }
        MessageBundle.Type.VIDEO -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                OsdVideo(osdMessage!!.data as String) {
                    viewModel.onOsdDone()
                }
            }
        }
        MessageBundle.Type.EMOJI -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                OsdApng(osdMessage!!.data as ApngDrawable) {
                    viewModel.onOsdDone()
                }
            }
        }
    }
}

@Composable
private fun OsdPicture(url: String) {
    val bitmap: Bitmap = remember {
        BitmapFactory.decodeFile(url)
    }

    val rotate: Int = remember {
        getRotate(File(url))
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillHeight,
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .rotate(rotate.toFloat() + 90f)
    )
}

@Composable
private fun OsdApng(apngDrawable: ApngDrawable, onDone: () -> Unit) {
    val drawable: ApngDrawable = remember {
        apngDrawable
    }

    AndroidView(
        factory = {
            ImageView(it).apply {
                scaleType = ImageView.ScaleType.FIT_XY

                setImageDrawable(drawable)


                drawable.registerRepeatAnimationCallback(object : RepeatAnimationCallback {
                    override fun onAnimationRepeat(drawable: ApngDrawable, nextLoopIndex: Int) {
                        super.onAnimationRepeat(drawable, nextLoopIndex)
                        if (nextLoopIndex > 3) {
                            drawable.recycle()
                            onDone()
                        }
                    }

                })

                drawable.start()
            }
        }, modifier = Modifier
            .width(800.dp)
            .height(800.dp)
    )
}

@Composable
private fun OsdVideo(url: String, onDone: () -> Unit) {
    AndroidView(
        factory = {
            PlayerView(it).apply {
                val player: SimpleExoPlayer = SimpleExoPlayer.Builder(it).build()

                this.player = player

                this.useController = false

                player.playWhenReady = true

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        if (state == ExoPlayer.STATE_ENDED) {
                            player.release()
                            onDone()
                        }
                    }
                })

                player.addMediaItem(MediaItem.fromUri(Uri.parse(url)))

                player.prepare()
            }
        }, modifier = Modifier
            .width(800.dp)
            .height(800.dp)
    )
}

private fun getRotate(file: File): Int {
    return try {
        val exifInterface: ExifInterface = ExifInterface(file.absolutePath)
        exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } catch (e: IOException) {
        Log.e("OSD", e.message.toString())
        -90
    }
}