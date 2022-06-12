package idv.bruce.ktv.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class TimerLocker(private var duration: Long) {
    var mLocker: Boolean = false

    val isLocked: Boolean
        get() = mLocker

    private val timer: ExecutorService = Executors.newSingleThreadExecutor()

    private var timerFuture: Future<*>? = null

    fun lock() {
        if (timerFuture != null) {
            timerFuture!!.cancel(true)
        }

        timerFuture = timer.submit {
            try {
                mLocker = true
                Thread.sleep(duration)
            }finally {
                mLocker = false
                timerFuture = null
            }
        }

    }
}