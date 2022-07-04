package idv.bruce.ui.osd.container

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.linecorp.apng.ApngDrawable
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ApngView(
    context: Context,
    private val repeatCount: Int,
    private val onDone: () -> Unit
) :
    GLSurfaceView(context) {

    private val textures: IntArray = IntArray(1)

    private var isTextureAvailable: Boolean = false

    private val VERTEX_COORDINATES = floatArrayOf(
        -1.0f, +1.0f, 0.0f,
        +1.0f, +1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        +1.0f, -1.0f, 0.0f
    )

    private val TEXTURE_COORDINATES = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    private val TEXCOORD_BUFFER: Buffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind()
    private val VERTEX_BUFFER: Buffer = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()

    private var currantIndex: Int = -1

    private var startTime: Long = 0

    private var currentTime: Long = -1

    private var bitmap: Bitmap? = null

    private var canvas: Canvas? = null

    private var mRepeatCount: Int = 0

    private var isAvailable: Boolean = false

    private var currantDuration: Long = 0

    private var lastFrameTime: Long = 0

    private val updateThread: ExecutorService = Executors.newSingleThreadExecutor()

    private var updateFuture: Future<*>? = null

    var apngDrawable: ApngDrawable? = null
        set(value) {
            field = value

            if (field == null) {
                isAvailable = false
                updateFuture?.cancel(true)
            } else {
                bitmap?.recycle()

                bitmap = Bitmap.createBitmap(
                    field!!.minimumWidth,
                    field!!.minimumHeight,
                    Bitmap.Config.ARGB_8888
                )

                canvas = Canvas(bitmap!!)

                startTime = System.currentTimeMillis()

                mRepeatCount = 0

                isAvailable = true

//                updateFuture = updateThread.submit {
//                    while (true) {
//                        requestRender()
//                        Thread.sleep(33)
//                    }
//                }
            }
        }


    private val maxDuration: Long
        @SuppressLint("Range")
        get() = apngDrawable?.durationMillis?.toLong() ?: 0L

    init {

        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.RGBA_8888)
        setZOrderOnTop(true)
        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10, p1: EGLConfig?) {
                gl.glEnable(GL10.GL_TEXTURE_2D)
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
                gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)



                gl.glGenTextures(1, textures, 0)

                renderMode = RENDERMODE_CONTINUOUSLY
            }

            override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
                gl.glViewport(0, 0, w, h)
            }

            override fun onDrawFrame(gl: GL10) {
                if (!isAvailable) return

                if (mRepeatCount >= repeatCount) {
                    apngDrawable?.recycle()
                    apngDrawable = null
                    onDone()
                    return
                }

                currentTime = System.currentTimeMillis()

                currantDuration = currentTime - startTime

                if (currantDuration > maxDuration) {
                    startTime = currentTime
                    currantDuration = 0
                    mRepeatCount++
                }

                apngDrawable?.seekTo(currantDuration)

                if (apngDrawable?.currentFrameIndex != currantIndex) {
                    updateTexture(gl)
                }


                gl.glClearColor(0f, 0f, 0f, 0f)
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

                gl.glActiveTexture(GL10.GL_TEXTURE0)
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
            }
        })
    }

    private fun updateTexture(gl: GL10) {

        if (bitmap == null) return

        val drawable: ApngDrawable = apngDrawable ?: return

        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawable.draw(canvas!!)


        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])

        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap!!, 0)

        currantIndex = drawable.currentFrameIndex

        isTextureAvailable = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmap?.recycle()
    }
}