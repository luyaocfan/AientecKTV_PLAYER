package com.google.android.exoplayer2;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.video.IneMediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class IneRenderersFactory extends DefaultRenderersFactory {
    private static final String TAG = "IneRenderersFactory";
    AudioProcessor audioProcessor;
    public IneRenderersFactory(Context context, AudioProcessor audioProcessor) {
        super(context);
        this.audioProcessor = audioProcessor;
    }
    @Nullable
    @Override
    protected AudioSink buildAudioSink(
            Context context,
            boolean enableFloatOutput,
            boolean enableAudioTrackPlaybackParams,
            boolean enableOffload) {
        return new DefaultAudioSink(
                AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES,
                new DefaultAudioSink.DefaultAudioProcessorChain(
                        new SilenceSkippingAudioProcessor(),
                        audioProcessor),
                enableFloatOutput,
                enableAudioTrackPlaybackParams,
                enableOffload
                        ? DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED
                        : DefaultAudioSink.OFFLOAD_MODE_DISABLED);
    }

//    IneMediaCodecVideoRenderer videoRenderer= new IneMediaCodecVideoRenderer();

//
//    @Override
//    protected void buildAudioRenderers(
//            Context context,
//            int extensionRendererMode,
//            MediaCodecSelector mediaCodecSelector,
//            boolean enableDecoderFallback,
//            AudioSink audioSink,
//            Handler eventHandler,
//            AudioRendererEventListener eventListener,
//            ArrayList<Renderer> out) {
//        MediaCodecAudioRenderer audioRenderer =
//                new MediaCodecAudioRenderer(
//                        context,
//                        mediaCodecSelector,
//                        enableDecoderFallback,
//                        eventHandler,
//                        eventListener,
//                        audioSink) {
//                    @Override
//                    protected void onEnabled(boolean joining, boolean mayRenderStartOfStream)
//                            throws ExoPlaybackException {
//                        super.onEnabled(joining, mayRenderStartOfStream);
//                        sonicAudioProcessor.setOutputSampleRateHz(22050);
//                    }
//                };
//        out.add(audioRenderer);
//    }

    /**
     * Builds video renderers for use by the player.
     *
     * @param context The {@link Context} associated with the player.
     * @param extensionRendererMode The extension renderer mode.
     * @param mediaCodecSelector A decoder selector.
     * @param enableDecoderFallback Whether to enable fallback to lower-priority decoders if decoder
     *     initialization fails. This may result in using a decoder that is slower/less efficient than
     *     the primary decoder.
     * @param eventHandler A handler associated with the main thread's looper.
     * @param eventListener An event listener.
     * @param allowedVideoJoiningTimeMs The maximum duration for which video renderers can attempt to
     *     seamlessly join an ongoing playback, in milliseconds.
     * @param out An array to which the built renderers should be appended.
     */
//    @Override
//    protected void buildVideoRenderers(
//            Context context,
//            @ExtensionRendererMode int extensionRendererMode,
//            MediaCodecSelector mediaCodecSelector,
//            boolean enableDecoderFallback,
//            Handler eventHandler,
//            VideoRendererEventListener eventListener,
//            long allowedVideoJoiningTimeMs,
//            ArrayList<Renderer> out) {
//        IneMediaCodecVideoRenderer videoRenderer =
//                new IneMediaCodecVideoRenderer(
//                        context,
//                        mediaCodecSelector,
//                        allowedVideoJoiningTimeMs,
//                        enableDecoderFallback,
//                        eventHandler,
//                        eventListener,
//                        MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
//        videoRenderer.experimentalSetAsynchronousBufferQueueingEnabled(false);
//        videoRenderer.experimentalSetForceAsyncQueueingSynchronizationWorkaround(false);
//        videoRenderer.experimentalSetSynchronizeCodecInteractionsWithQueueingEnabled(false);
//        out.add(videoRenderer);
//
//        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
//            return;
//        }
//        int extensionRendererIndex = out.size();
//        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
//            extensionRendererIndex--;
//        }
//
//        try {
//            // Full class names used for constructor args so the LINT rule triggers if any of them move.
//            // LINT.IfChange
//            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
//            Constructor<?> constructor =
//                    clazz.getConstructor(
//                            long.class,
//                            android.os.Handler.class,
//                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
//                            int.class);
//            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
//            Renderer renderer =
//                    (Renderer)
//                            constructor.newInstance(
//                                    allowedVideoJoiningTimeMs,
//                                    eventHandler,
//                                    eventListener,
//                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
//            out.add(extensionRendererIndex++, renderer);
//            Log.i(TAG, "Loaded LibvpxVideoRenderer.");
//        } catch (ClassNotFoundException e) {
//            // Expected if the app was built without the extension.
//        } catch (Exception e) {
//            // The extension is present, but instantiation failed.
//            throw new RuntimeException("Error instantiating VP9 extension", e);
//        }
//
//        try {
//            // Full class names used for constructor args so the LINT rule triggers if any of them move.
//            // LINT.IfChange
//            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.av1.Libgav1VideoRenderer");
//            Constructor<?> constructor =
//                    clazz.getConstructor(
//                            long.class,
//                            android.os.Handler.class,
//                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
//                            int.class);
//            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
//            Renderer renderer =
//                    (Renderer)
//                            constructor.newInstance(
//                                    allowedVideoJoiningTimeMs,
//                                    eventHandler,
//                                    eventListener,
//                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
//            out.add(extensionRendererIndex++, renderer);
//            Log.i(TAG, "Loaded Libgav1VideoRenderer.");
//        } catch (ClassNotFoundException e) {
//            // Expected if the app was built without the extension.
//        } catch (Exception e) {
//            // The extension is present, but instantiation failed.
//            throw new RuntimeException("Error instantiating AV1 extension", e);
//        }
//    }
}
