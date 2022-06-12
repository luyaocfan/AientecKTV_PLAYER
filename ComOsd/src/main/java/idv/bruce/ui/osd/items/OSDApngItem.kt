package idv.bruce.ui.osd.items

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.util.SizeF
import com.linecorp.apng.ApngDrawable
import idv.bruce.ui.osd.OSDItem

class OSDApngItem(
    private var mApngDrawable: ApngDrawable,
    private var repeatCount: Int,
    locationPercent: PointF,
    areaPercent: SizeF
) : OSDItem(locationPercent, areaPercent) {

    private var mLoopStartTime: Long = -1L

    private var mDuration: Long = -1L

    private var mCount: Int = 0

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        if (mLoopStartTime == -1L) {
            mCount++
            mLoopStartTime = frameTimeNanos
        }

        if ((frameTimeNanos - mLoopStartTime) / 1000000L >= mDuration) {
            if (mCount == repeatCount)
                return true
            else
                mLoopStartTime = -1L
        }

        mApngDrawable.seekTo((frameTimeNanos - mLoopStartTime) / 1000000L)


        mApngDrawable.draw(canvas)


        return false
    }

    override fun release() {
        mApngDrawable.recycle()
    }

    @SuppressLint("Range")
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

        val drawRect = Rect(
            point.x,
            point.y,
            point.x + drawSize.width,
            point.y + drawSize.height
        )

        mApngDrawable.bounds = drawRect

        mDuration = mApngDrawable.durationMillis.toLong()
    }
}