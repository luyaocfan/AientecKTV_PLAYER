package idv.bruce.ui.osd.container

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Choreographer
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MarqueView(context: Context, attr: AttributeSet, defAttr: Int) :
    AppCompatTextView(context, attr, defAttr),
    Choreographer.FrameCallback {
    companion object {
        const val TAG: String = "Marque"
    }

    fun interface Callback {
        fun onDone()
    }


    private val mChoreographer: Choreographer = Choreographer.getInstance()

    private var isAnimation = false

    private var mWidth: Float = -1f
        set(value) {
            field = value
            mVelocity = (value / (duration / 1000L).toFloat())
            scrollX = (-value).toInt()
        }

    private var mUpdateTime: Long = -1L

    private var mVelocity: Float = 0f

    private var mBound: Float = 0f

    var duration: Long = 20000
        set(value) {
            field = value
            mVelocity = (mWidth / (value / 1000L).toFloat())
        }

    var callback: Callback? = null

    init {
        init()
    }

    constructor(context: Context, attr: AttributeSet) : this(
        context,
        attr,
        android.R.attr.textViewStyle
    )

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        val str: String = (text ?: return).toString()
        val rect = Rect()
        paint.getTextBounds(str, 0, str.length, rect)
        mBound = (rect.width()).toFloat()

    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 8f
        setTextColor(Color.RED)
        super.onDraw(canvas)

        paint.style = Paint.Style.FILL
        setTextColor(Color.WHITE)
        super.onDraw(canvas)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (isAnimation) {

            if (mUpdateTime == -1L)
                mUpdateTime = frameTimeNanos

            if (scrollX > mBound) {
                scrollX = (-width)
                callback?.onDone()
            }

            scrollX += (mVelocity * ((frameTimeNanos - mUpdateTime) / 1000000000f)).toInt()

            mUpdateTime = frameTimeNanos

            invalidate()

            mChoreographer.postFrameCallback(this)
        }
    }

    fun start() {
        if (!isAnimation) {
            isAnimation = true
            mChoreographer.postFrameCallback(this)
        }
    }

    fun stop() {
        if (isAnimation) {
            isAnimation = false
        }
    }

    private fun init() {
        maxLines = 1
        ellipsize = null
        setHorizontallyScrolling(true)

        (context as LifecycleOwner).apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    start()
                }
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    stop()
                }
            }
        }
        this.viewTreeObserver.addOnGlobalLayoutListener {
            mWidth = this.width.toFloat()
        }

    }
}