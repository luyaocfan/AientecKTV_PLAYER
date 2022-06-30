package idv.bruce.ui.osd.container

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OSDQueue
import idv.bruce.ui.osd.OsdContainer
import idv.bruce.ui.osd.OsdEventListener
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class OSDContainerView(context: Context, attr: AttributeSet? = null) : SurfaceView(context, attr),
    OsdContainer {
    companion object {
        const val TAG: String = "OSD_Container"
    }

    private val queue: OSDQueue = OSDQueue()

    private val service: ExecutorService = Executors.newSingleThreadExecutor()

    private var future: Future<*>? = null

    private val choreographer: Choreographer = Choreographer.getInstance()

    var eventListener: OsdEventListener?
        get() {
            return queue.eventListener
        }
        set(value) {
            queue.eventListener = value
        }

    private var isAnime: Boolean = false

    private var isViewPortReady: Boolean = false

    private var mWidth: Int = -1

    private var mHeight: Int = -1

    private var drawerThread: ExecutorService = Executors.newSingleThreadExecutor()

    private var drawerFuture: Future<*>? = null

    init {
        (context as LifecycleOwner).apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    onStart()
                }
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    onStop()
                }
            }
        }
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceCreated(p0: SurfaceHolder) {

            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                if (drawerFuture == null)
                    drawerFuture = drawerThread.submit(drawWorker)
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                if (drawerFuture != null)
                    drawerFuture!!.cancel(true)
                drawerFuture = null
            }

            override fun surfaceRedrawNeeded(p0: SurfaceHolder) {

            }

        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        Log.d("Trace", "OnMeasure : $measuredWidth, $measuredHeight")

        if (mWidth != measuredWidth || mHeight != measuredHeight)
            isViewPortReady = false

        if (!isViewPortReady) {
            mWidth = measuredWidth
            mHeight = measuredHeight
            queue.viewSize = Size(mWidth, mHeight)
            isViewPortReady = true
        }
    }

    private var count: Int = 0


    override fun addOsdItem(item: OSDItem) {
        queue.add(item)
    }

    override fun removeOsdItem(item: OSDItem) {
        queue.remove(item)
    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    private val drawWorker: Runnable = Runnable {
        var frameTimeNanos: Long

        var lastTimeNanos: Long = -1L

        val tpf: Long = (1000000000 / 20)

        while (true) {
            frameTimeNanos = System.nanoTime()

            if (frameTimeNanos - lastTimeNanos < tpf) {
                Thread.yield()
            } else {
                lastTimeNanos = frameTimeNanos
                val mCanvas = holder.lockCanvas() ?: continue
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                queue.onDraw(mCanvas, frameTimeNanos, frameTimeNanos - lastTimeNanos)
                holder.unlockCanvasAndPost(mCanvas)
            }
        }
//

//
//        if (mLastTimeNanos == -1L)
//            mLastTimeNanos = frameTimeNanos
//
//        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//
////        Log.d(TAG,"Draw")
//        queue.onDraw(mCanvas, frameTimeNanos, frameTimeNanos - mLastTimeNanos)
//
//        mLastTimeNanos = frameTimeNanos
//
    }
}