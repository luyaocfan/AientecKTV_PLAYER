package idv.bruce.common.impl

import android.content.Context
import java.lang.ref.WeakReference

abstract class ModelImpl(context: Context) {
    abstract val tag: String

    protected val contextRef: WeakReference<Context> = WeakReference(context)

    abstract fun init()

    abstract fun release()
}