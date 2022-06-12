package idv.bruce.ktv.player

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.ExoPlayer
import idv.bruce.ktv.KtvPlayerHost
import java.lang.ref.WeakReference


internal abstract class PlayerImpl(context: Context) {

    interface Callback {
        fun onEvent(event: Event)
    }

    sealed class Event() {

    }

    protected val debugTag: String
        get() = "Player_${this.javaClass.simpleName}"

    private val contextRef: WeakReference<Context> = WeakReference(context)

    protected val context: Context
        get() = contextRef.get()!!

    protected abstract val player: ExoPlayer

    fun attachSurfaceView(surfaceView: SurfaceView) {
        player.setVideoSurfaceView(surfaceView)
    }

    fun detachSurfaceView() {
        player.setVideoSurfaceView(null)
    }

    fun release() {
        player.setVideoSurface(null)
        player.release()
    }

    abstract fun addMedia(vararg items: KtvPlayerHost.KtvItem)

    abstract fun pause(): Boolean

    abstract fun resume(): Boolean

    abstract fun next(): Boolean
}