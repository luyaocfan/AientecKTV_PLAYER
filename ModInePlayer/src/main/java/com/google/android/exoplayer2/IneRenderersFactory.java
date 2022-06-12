package com.google.android.exoplayer2;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.util.ArrayList;

public class IneRenderersFactory extends DefaultRenderersFactory {
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
}
