package idv.bruce.ui.osd.items

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.video.VideoSize
import idv.bruce.ui.osd.OSDItem
import java.lang.ref.WeakReference


class OSDVideoItem(
    context: Context,
    var uri: String,
    locationPercent: PointF,
    sizePercent: SizeF,
    var fromAsset: Boolean = false
) :
    OSDItem(locationPercent, sizePercent) {

    private lateinit var mPlayer: SimpleExoPlayer

    private lateinit var mSurface: Surface

    private lateinit var mTextureView: TextureView

    private val contextRef: WeakReference<Context> = WeakReference(context)

    private var isEnd: Boolean = false

    private val window: Window = (context as Activity).window

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {

        return isEnd
    }

    override fun release() {

    }

    override fun onAttachToContainer(containerSize: Size) {
        val context: Activity = (contextRef.get() ?: return) as Activity

        val rectgle = Rect()

        window.decorView.getWindowVisibleDisplayFrame(rectgle)
        val statusBarH = rectgle.top
        val contentViewTop: Int = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
        val toolbarH = if (contentViewTop == 0) 0 else contentViewTop - statusBarH


        val drawSize: Size = Size(
            if (size.width != 0f)
                (containerSize.width * size.width).toInt()
            else
                (containerSize.height * size.height).toInt(),
            if (size.height != 0f)
                (containerSize.height * size.height).toInt()
            else
                (containerSize.width * size.width).toInt()
        )

        val point: Point = Point(
            if (locationPercent.x != -1f)
                (containerSize.width * locationPercent.x).toInt()
            else
                (containerSize.width - drawSize.width) / 2,
            if (locationPercent.y != -1f)
                (containerSize.height * locationPercent.y).toInt()
            else
                (containerSize.height - drawSize.height) / 2
        )

        mTextureView = TextureView(context)

        mTextureView.layoutParams =
            ViewGroup.MarginLayoutParams(drawSize.width, drawSize.height).apply {
                setMargins(point.x, point.y + toolbarH + statusBarH, 0, 0)
            }


        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.d("Trace", "onSurfaceTextureAvailable")
                mSurface = Surface(surface)
                mPlayer.setVideoSurface(mSurface)
                mPlayer.playWhenReady = true
                if (!fromAsset) {
                    mPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                    val mediaItem: MediaItem = MediaItem.fromUri(uri)
                    mPlayer.setMediaItem(mediaItem)
                } else {
                    mPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT

                    val mUri: Uri = Uri.parse(uri)

                    val dataSpec: DataSpec = DataSpec(mUri)

                    val dataSource: AssetDataSource = AssetDataSource(context)

                    val fac: DataSource.Factory = DataSource.Factory {
                        return@Factory dataSource
                    }

                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(fac).createMediaSource(
                            MediaItem.fromUri(mUri)
                        )

                    dataSource.open(dataSpec)

                    mPlayer.addMediaSource(mediaSource)

                }
                mPlayer.prepare()

            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.d("Trace", "onSurfaceTextureSizeChanged: $width, $height")

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d("Trace", "onSurfaceTextureDestroyed")
                mPlayer.release()
                mSurface.release()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                    Log.d("Trace", "onSurfaceTextureUpdated")
            }
        }

        mPlayer = SimpleExoPlayer.Builder(context).build()


        mPlayer.addListener(object : Player.Listener {

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                isEnd = true
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d("Trace", "onPlaybackStateChanged : $playbackState")
                if (playbackState == Player.STATE_READY) {

                }
                if (playbackState == Player.STATE_ENDED) {
                    isEnd = true
                    (window.decorView as ViewGroup).removeView(mTextureView)
                }

            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                Log.d("Trace", "onVideoSizeChanged 1: ${videoSize.width}, ${videoSize.height}")
                val layoutParam: ViewGroup.MarginLayoutParams =
                    mTextureView.layoutParams as ViewGroup.MarginLayoutParams

                val newWidth: Int =
                    (drawSize.height.toFloat() * videoSize.width.toFloat() / videoSize.height.toFloat()).toInt()

                val oldWidth: Int = layoutParam.width

                layoutParam.width = newWidth
                layoutParam.leftMargin = layoutParam.leftMargin + (oldWidth - newWidth) / 2


                mTextureView.layoutParams = layoutParam
            }
        })

        (context.window.decorView as ViewGroup).addView(mTextureView)


    }
}