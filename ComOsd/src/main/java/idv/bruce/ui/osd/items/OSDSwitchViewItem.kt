package idv.bruce.ui.osd.items

import android.graphics.*
import android.util.Size
import android.util.SizeF
import android.view.View
import idv.bruce.ui.osd.OSDItem

class OSDSwitchViewItem(
    private var firstView: View,
    private var firstLocation: PointF,
    private var firstSize: SizeF,
    private val firstDuration: Long,
    private var secondView: View,
    private val secondLocation: PointF,
    private val secondSize: SizeF,
    private val secondDuration: Long
) : OSDItem(firstLocation, firstSize) {

    private var firstBitmap: Bitmap? = null

    private var firstMatrix: Matrix? = null

    private var secondBitmap: Bitmap? = null

    private var secondMatrix: Matrix? = null

    private var mPaint: Paint = Paint()

    private var toggle: Boolean = false

    private var mStartTimeNanos: Long = -1L

    override fun onAttachToContainer(containerSize: Size) {
        generateBitmap(containerSize, firstView, firstLocation, firstSize).also {
            firstBitmap = it.first
            firstMatrix = it.second
        }

        generateBitmap(containerSize, secondView, secondLocation, secondSize).also {
            secondBitmap = it.first
            secondMatrix = it.second
        }
    }

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        if (firstBitmap == null || secondBitmap == null) return false

        if (mStartTimeNanos == -1L) mStartTimeNanos = frameTimeNanos

        if (toggle) {
            if (secondDuration != -1L && (frameTimeNanos - mStartTimeNanos) / 1000000L >= secondDuration)
                return true

            canvas.drawBitmap(secondBitmap!!, secondMatrix!!, mPaint)
        } else {
            if (firstDuration != -1L && (frameTimeNanos - mStartTimeNanos) / 1000000L >= firstDuration) {
                toggle = true
                mStartTimeNanos = frameTimeNanos
            }

            canvas.drawBitmap(firstBitmap!!, firstMatrix!!, mPaint)
        }

        return false
    }

    override fun release() {
        firstBitmap?.recycle()
        secondBitmap?.recycle()
    }

    private fun generateBitmap(
        containerSize: Size,
        view: View,
        location: PointF,
        size: SizeF
    ): Pair<Bitmap, Matrix> {
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
            if (location.x != -1f)
                (containerSize.width * location.x).toInt()
            else
                (containerSize.width - drawSize.width) / 2,
            if (location.y != -1f)
                (containerSize.height * location.y).toInt()
            else
                (containerSize.height - drawSize.height) / 2
        )

        val mDrawRect = Rect(
            point.x,
            point.y,
            point.x + drawSize.width,
            point.y + drawSize.height
        )

        val withSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect.width() /* any */,
                View.MeasureSpec.EXACTLY
            )

        val hightSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect.height() /* any */,
                View.MeasureSpec.EXACTLY
            )

        view.measure(withSpec, hightSpec)

        val w: Int = view.measuredWidth

        val h: Int = view.measuredHeight

        view.layout(0, 0, w, h)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val mCanvas = Canvas(bitmap)

        view.draw(mCanvas)

        val matrix = Matrix().apply {
            postTranslate(mDrawRect.left.toFloat(), mDrawRect.top.toFloat())
        }

        return Pair(bitmap, matrix)
    }
}