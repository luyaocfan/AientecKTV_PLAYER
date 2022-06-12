package idv.bruce.ui.osd.items

import android.graphics.*
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.View
import idv.bruce.ui.osd.OSDItem

class OSDViewItem(
    private var view: View,
    private var duration: Long,
    locationPoint: PointF,
    areaPercent: SizeF
) :
    OSDItem(locationPoint, areaPercent) {


    private var mDrawRect: Rect? = null

    private var mCanvas: Canvas? = null

    private var mBitmap: Bitmap? = null

    private var mMatrix: Matrix? = null

    private var mPaint: Paint = Paint()

    private var mStartTimeNanos: Long = -1L

    var hidden: Boolean = false

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        if (mCanvas == null) return true

        if (mStartTimeNanos == -1L) {
            mStartTimeNanos = frameTimeNanos
        }

        if ((frameTimeNanos - mStartTimeNanos) / 1000000L >= duration && duration != -1L) {
            Log.d("OSD", "duration : $duration, ${(frameTimeNanos - mStartTimeNanos) / 1000000L}")
            return true
        }

        if (!hidden)
            canvas.drawBitmap(mBitmap!!, mMatrix!!, mPaint)

        return false
    }

    override fun release() {
        mBitmap?.recycle()
    }

    fun notifyContentChanged() {
        if (mBitmap?.isRecycled == true) {
            view.draw(mCanvas)
        }
    }

    override fun onAttachToContainer(containerSize: Size) {
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

        mDrawRect = Rect(
            point.x,
            point.y,
            point.x + drawSize.width,
            point.y + drawSize.height
        )

        val withSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect!!.width() /* any */,
                View.MeasureSpec.EXACTLY
            )

        val hightSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect!!.height() /* any */,
                View.MeasureSpec.EXACTLY
            )

        view.measure(withSpec, hightSpec)

        val w: Int = view.measuredWidth

        val h: Int = view.measuredHeight

        view.layout(0, 0, w, h)

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        mCanvas = Canvas(mBitmap!!)

        view.draw(mCanvas)

        mMatrix = Matrix().apply {
            postTranslate(mDrawRect!!.left.toFloat(), mDrawRect!!.top.toFloat())
        }

    }
}