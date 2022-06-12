package com.ine.ktv.playerengine;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.util.EventLogger;

public class MyEventLogger extends EventLogger {
    public MyEventLogger(@Nullable MappingTrackSelector trackSelector) {
        super(trackSelector);
    }

    public MyEventLogger(@Nullable MappingTrackSelector trackSelector, String tag) {
        super(trackSelector, tag);
    }

    @Override
    protected void logd(String msg) {
        super.logd(msg);
    }

    @Override
    protected void loge(String msg) {
        super.loge(msg);
    }
}
