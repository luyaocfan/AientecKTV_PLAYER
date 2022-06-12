package com.aientec.ktv_vod.common.impl

import android.content.Context
import java.lang.ref.WeakReference

abstract class ModuleImpl(context: Context) {
    protected val contextRef:WeakReference<Context> = WeakReference(context)

    abstract fun init()

    abstract fun release()
}