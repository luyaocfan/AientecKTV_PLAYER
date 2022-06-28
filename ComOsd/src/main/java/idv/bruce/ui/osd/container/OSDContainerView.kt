package idv.bruce.ui.osd.container

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Choreographer
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

class OSDContainerView(context: Context, attr: AttributeSet) : View(context, attr),
    Choreographer.FrameCallback, OsdContainer {
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

    private var debugThread: ExecutorService = Executors.newSingleThreadExecutor()

    private var mLastTimeNanos: Long = -1L

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

    override fun onDraw(canvas: Canvas?) {
        if (isAnime && isViewPortReady) {
            val mCanvas = canvas ?: return

            val frameTimeNanos: Long = System.nanoTime()

            if (mLastTimeNanos == -1L)
                mLastTimeNanos = frameTimeNanos

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            queue.onDraw(mCanvas, frameTimeNanos, frameTimeNanos - mLastTimeNanos)

            mLastTimeNanos = frameTimeNanos


        } else {
            super.onDraw(canvas)
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        //postInvalidate()
        choreographer.postFrameCallback(this)
    }

    override fun addOsdItem(item: OSDItem) {
        queue.add(item)
        onStart()
    }

    override fun removeOsdItem(item: OSDItem) {
        queue.remove(item)
    }

    override fun onStart() {
        debugThread.submit {
            Log.d("OSD", "Queue : ${queue.size}")
        }
        if (!isAnime) {
            isAnime = true
            choreographer.postFrameCallback(this)
        }
    }

    override fun onStop() {
        if (isAnime) {
            isAnime = false
            choreographer.removeFrameCallback(this)
        }
    }

}