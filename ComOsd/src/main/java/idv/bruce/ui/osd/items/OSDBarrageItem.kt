package idv.bruce.ui.osd.items

import android.graphics.*
import android.text.TextPaint
import android.util.Size
import android.util.SizeF
import androidx.annotation.ColorInt
import idv.bruce.ui.osd.OSDItem

class OSDBarrageItem(
    var text: String,
    var direction: Direction = Direction.RIGHT_TO_LEFT,
    var scale: Float = 1f,
    @ColorInt var color: Int = Color.WHITE,
    var marqueMode: Boolean = false,
    var duration: Long = 20000L,
    var headerIcon: Bitmap? = null,
    private var displayRange: Pair<Float, Float> = Pair(0f, 1f)
) : OSDItem(PointF(0f, 0f), SizeF(0f, 0f)) {
    companion object {
        private const val DEFAULT_SIZE: Int = 48
    }

    enum class Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    private lateinit var textPaint: TextPaint

    private lateinit var outlinePaint: TextPaint

    private var dx: Float = 0f

    private var point: PointF = PointF()

    private var mLimit: Pair<Float, Float>? = null

    private var mHeader: Bitmap? = null

    private lateinit var headerSize: Size

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {


        point.offset(dx * (timeIntervalNanos / 1000000000f), 0f)

        canvas.save()

        canvas.translate(point.x, point.y)

        if (mHeader != null) {
            canvas.drawBitmap(mHeader!!, 0f, -mHeader!!.height.toFloat() , textPaint)
            canvas.translate(mHeader!!.width.toFloat() + 10f, 0f)
        }

        canvas.drawText(text, 0f, 0f, textPaint)

        canvas.restore()

        if (point.x < mLimit!!.first || point.x > mLimit!!.second) {
            if (!marqueMode)
                return true
            else
                point.x = when (direction) {
                    Direction.LEFT_TO_RIGHT -> mLimit!!.first
                    Direction.RIGHT_TO_LEFT -> mLimit!!.second
                }
        }

        return false
    }

    override fun release() {

    }

    override fun onAttachToContainer(containerSize: Size) {
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

        textPaint.textSize = DEFAULT_SIZE * scale
        textPaint.color = color

        outlinePaint = TextPaint(textPaint)


        val width: Int = textPaint.measureText(text).toInt()

        val fontMatrix: Paint.FontMetrics = textPaint.fontMetrics

        val mRect = Rect(
            0, 0, width, (fontMatrix.descent - fontMatrix.ascent).toInt()
        )

        mHeader = if (headerIcon == null) null else Bitmap.createScaledBitmap(
            headerIcon!!,
            mRect.height(),
            mRect.height(),
            true
        )

        val startHighRange: Pair<Int, Int> = Pair(
            first = (containerSize.height * displayRange.first).toInt(),
            second = (containerSize.height * displayRange.second - mRect.height()).toInt()
        )


        mLimit = Pair(
            (0 - mRect.width() - if (headerIcon == null) 0 else mRect.height()).toFloat(),
            containerSize.width.toFloat()
        )

        val h: Int =
            (Math.random() * ((startHighRange.second - startHighRange.first).toFloat()) + startHighRange.first.toFloat()).toInt()

        dx = when (direction) {
            Direction.LEFT_TO_RIGHT -> {
                point.offset(0f, h.toFloat())
                ((containerSize.width + mRect.width()).toFloat() / duration.toFloat() * 1000f)
            }
            Direction.RIGHT_TO_LEFT -> {
                point.offset(containerSize.width.toFloat(), h.toFloat())
                -((containerSize.width + mRect.width()).toFloat() / duration.toFloat() * 1000f)
            }
        }
    }
}