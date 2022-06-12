package com.aientec.ktv_pos_tablet.model

import android.content.Context
import java.lang.ref.WeakReference

abstract class Model(context: Context) {
    protected val contextRef: WeakReference<Context> = WeakReference(context)

    abstract fun init()

    abstract fun release()
}