package com.google.android.exoplayer2.video;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.nio.ByteBuffer;

public class IneMediaCodecVideoRenderer extends MediaCodecVideoRenderer{
    private static final String TAG = "IneMediaCodecVideoRenderer";
    private long outputBufferBeforeDownsamplingDropCount = 0;

    /**
     * @param context A context.
     * @param mediaCodecSelector A decoder selector.
     */
    public IneMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
        this(context, mediaCodecSelector, 0);
    }

    /**
     * @param context A context.
     * @param mediaCodecSelector A decoder selector.
     * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
     *     can attempt to seamlessly join an ongoing playback.
     */
    public IneMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector,
                                   long allowedJoiningTimeMs) {
        this(
                context,
                mediaCodecSelector,
                allowedJoiningTimeMs,
                /* eventHandler= */ null,
                /* eventListener= */ null,
                /* maxDroppedFramesToNotify= */ 0);
    }

    /**
     * @param context A context.
     * @param mediaCodecSelector A decoder selector.
     * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
     *     can attempt to seamlessly join an ongoing playback.
     * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
     *     null if delivery of events is not required.
     * @param eventListener A listener of events. May be null if delivery of events is not required.
     * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
     *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
     */
    public IneMediaCodecVideoRenderer(
            Context context,
            MediaCodecSelector mediaCodecSelector,
            long allowedJoiningTimeMs,
            @Nullable Handler eventHandler,
            @Nullable VideoRendererEventListener eventListener,
            int maxDroppedFramesToNotify) {
        this(
                context,
                MediaCodecAdapter.Factory.DEFAULT,
                mediaCodecSelector,
                allowedJoiningTimeMs,
                /* enableDecoderFallback= */ false,
                eventHandler,
                eventListener,
                maxDroppedFramesToNotify);
    }

    /**
     * @param context A context.
     * @param mediaCodecSelector A decoder selector.
     * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
     *     can attempt to seamlessly join an ongoing playback.
     * @param enableDecoderFallback Whether to enable fallback to lower-priority decoders if decoder
     *     initialization fails. This may result in using a decoder that is slower/less efficient than
     *     the primary decoder.
     * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
     *     null if delivery of events is not required.
     * @param eventListener A listener of events. May be null if delivery of events is not required.
     * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
     *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
     */
    public IneMediaCodecVideoRenderer(
            Context context,
            MediaCodecSelector mediaCodecSelector,
            long allowedJoiningTimeMs,
            boolean enableDecoderFallback,
            @Nullable Handler eventHandler,
            @Nullable VideoRendererEventListener eventListener,
            int maxDroppedFramesToNotify) {
        this(
                context,
                MediaCodecAdapter.Factory.DEFAULT,
                mediaCodecSelector,
                allowedJoiningTimeMs,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                maxDroppedFramesToNotify);
    }
    public IneMediaCodecVideoRenderer(Context context, MediaCodecAdapter.Factory codecAdapterFactory, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, boolean enableDecoderFallback, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, codecAdapterFactory, mediaCodecSelector, allowedJoiningTimeMs, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify);
        outputBufferBeforeDownsamplingDropCount = 0;
    }
    @Override
    public String getName() {
        return TAG;
    }

    @Override
    protected boolean processOutputBuffer(
            long positionUs,
            long elapsedRealtimeUs,
            @Nullable MediaCodecAdapter codec,
            @Nullable ByteBuffer buffer,
            int bufferIndex,
            int bufferFlags,
            int sampleCount,
            long bufferPresentationTimeUs,
            boolean isDecodeOnlyBuffer,
            boolean isLastBuffer,
            Format format)
            throws ExoPlaybackException {
//        if(format.frameRate>30.0) {
//            elapsedRealtimeUs /= 2;
//            bufferPresentationTimeUs /= 2;
//        }
        boolean skip = isDecodeOnlyBuffer;
        if (!skip) {
            this.outputBufferBeforeDownsamplingDropCount++;
            if(format.frameRate>30.0) {
                //int skipr =  (int)(format.frameRate/30.0+0.999999);
                if(this.outputBufferBeforeDownsamplingDropCount%2!=0) {
                    skip = true;
                }
            }
        }

        return super.processOutputBuffer(
                positionUs,
                elapsedRealtimeUs,
                codec,
                buffer,
                bufferIndex,
                bufferFlags,
                sampleCount,
                bufferPresentationTimeUs,
                skip,
                isLastBuffer,
                format);
    }
//    @Override
//    fun processOutputBuffer(..., decodeOnlyBuffer, ...)
//   if (decodeOnlyBuffer) return super.processOutputBuffer(decodeOnlyBuffer)
//
//    bool shouldDrop = this.outputBufferBeforeDownsamplingDropCount++ % 6 != 0; // drop 5/6 of frames
//   return super.processOutputBuffer(shouldDrop);
}
