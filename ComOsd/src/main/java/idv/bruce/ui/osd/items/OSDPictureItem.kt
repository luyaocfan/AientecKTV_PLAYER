package idv.bruce.ui.osd.items

import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import idv.bruce.ui.osd.OSDItem
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OSDPictureItem(
    private var path: String,
    private var duration: Long,
    locationPercent: PointF,
    areaPercent: SizeF
) : OSDItem(locationPercent, areaPercent) {

    private var mPaint: Paint = Paint()

    private var mBitmap: Bitmap? = null

    private var isLoaded: Boolean = false

    private var isError: Boolean = false

    private var mCanvas: Canvas? = null

    private var mMatrix: Matrix? = null

    private var mDrawSize: Size? = null

    private var mDrawPoint: Point? = null

    private var mStartTimeNanos: Long = -1L

    private val downloadThread: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onUpdate(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        if (isError) return true

        if (!isLoaded) return false

        if (mBitmap == null) return false

        if (mStartTimeNanos == -1L)
            mStartTimeNanos = frameTimeNanos

        if ((frameTimeNanos - mStartTimeNanos) / 1000000L >= duration)
            return true

        canvas.drawBitmap(mBitmap!!, mMatrix!!, mPaint)

        return false
    }

    override fun release() {
        if (!isLoaded)
            Picasso.get().cancelRequest(target)
        mBitmap?.recycle()
        downloadThread.shutdown()


        val file: File = File(path)
        if (file.exists())
            file.delete()
    }

    private val target: Target = object : Target {


        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap == null) {
                isError = true
                return
            }

            val w: Int =
                (bitmap.width.toFloat() * mDrawSize!!.height.toFloat() / bitmap.height.toFloat()).toInt()

            mCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            mCanvas!!.drawBitmap(
                bitmap,
                null,
                Rect(0, 0, w, mCanvas!!.height),
                mPaint
            )

            mMatrix!!.postTranslate((mDrawSize!!.width - w) / 2f, 0f)

            isLoaded = true
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            isError = true
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            isLoaded = false
        }
    }

    override fun onAttachToContainer(containerSize: Size) {

        mDrawSize = Size(
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
                (containerSize.width - mDrawSize!!.width) / 2,
            if (locationPercent.y != -1f)
                (containerSize.height * locationPercent.y).toInt()
            else
                (containerSize.height - mDrawSize!!.height) / 2
        )

        val file: File = File(path)

        val rotate: Int = getRotate(file)


        mBitmap =
            Bitmap.createBitmap(mDrawSize!!.width, mDrawSize!!.height, Bitmap.Config.ARGB_8888)

        mCanvas = Canvas(mBitmap!!)

        mMatrix = Matrix().apply {
            postTranslate(point.x.toFloat(), point.y.toFloat())
        }

        Picasso.get().load(file)
            .resize(mDrawSize!!.width, mDrawSize!!.height)
            .centerInside()
            .rotate(rotate.toFloat())
            .into(target)
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
            0
        }
    }
}