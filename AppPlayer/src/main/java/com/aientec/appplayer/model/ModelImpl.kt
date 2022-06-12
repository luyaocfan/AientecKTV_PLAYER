package com.aientec.appplayer.model

import android.content.Context
import java.lang.ref.WeakReference

abstract class ModelImpl(context: Context) {
    protected val contextRef:WeakReference<Context> = WeakReference(context)

    abstract fun init()

    abstract fun release()
}