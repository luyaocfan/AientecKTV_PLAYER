package com.aientec.player2.ui.componants

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.exifinterface.media.ExifInterface
import com.aientec.player2.data.MessageBundle
import com.aientec.player2.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.linecorp.apng.ApngDrawable
import java.io.File
import java.io.IOException
import java.util.*

/**
 * OSD展示頁面
 */
@Composable
fun OSDContainer(viewModel: PlayerViewModel = PlayerViewModel()) {

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
        getCorrectBitmapByExifOrientation(File(url))
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillHeight,
        modifier = Modifier
            .fillMaxHeight(0.7f)
    )
}

@SuppressLint("Range")
@Composable
private fun OsdApng(apngDrawable: ApngDrawable, onDone: () -> Unit) {
    val size = with(LocalDensity.current) {
        600.dp.toPx()
    }

    apngDrawable.bounds = Rect(0, 0, size.toInt(), size.toInt())
    val infiniteTransition = rememberInfiniteTransition()

    val index by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = apngDrawable.frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(apngDrawable.durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    var count :Int = 0

    Canvas(modifier = Modifier.size(600.dp)) {
        drawIntoCanvas {
            if (count >= 5)
                onDone()
            if (index == 0)
                count++
            apngDrawable.loopCount = 1
            apngDrawable.seekToFrame(0, index)
            apngDrawable.draw(it.nativeCanvas)
        }
    }
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

/**
 * 根据图片exif信息纠正图片方向
 */
private fun getCorrectBitmapByExifOrientation(file: File): Bitmap {
    val absolutePath = file.absolutePath
    val exifInterface = ExifInterface(absolutePath)
    val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    var bitmap = BitmapFactory.decodeFile(absolutePath)

    if (orientation == ExifInterface.ORIENTATION_UNDEFINED || orientation == ExifInterface.ORIENTATION_NORMAL) {
        return bitmap
    }
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
    }
    bitmap = BitmapFactory.decodeFile(absolutePath)
    val width = bitmap.width
    val height = bitmap.height
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    return bitmap
    }
