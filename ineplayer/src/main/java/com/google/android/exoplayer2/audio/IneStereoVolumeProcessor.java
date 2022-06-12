package com.google.android.exoplayer2.audio;

import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IneStereoVolumeProcessor implements AudioProcessor {

    public interface Listener {
        default void onFormatError(IneStereoVolumeProcessor process, AudioFormat audioformat, String message) {}
    }
    AudioFormat audioFormat;
    private static final int[] pendingOutputChannels = new int[]{0, 1};

    private boolean active;
    private ByteBuffer buffer;
    private ByteBuffer outputBuffer;
    private boolean inputEnded;
    public static final int AudioControlOutput_LeftMono = 0;
    public static final int AudioControlOutput_RightMono = 1;
    public static final int AudioControlOutput_Stereo = 2;
    private int AudioControlOutput = AudioControlOutput_Stereo;

    private static final int LEFT_SPEAKER = 0;
    private static final int RIGHT_SPEAKER = 1;
    Listener listener = null;
    public IneStereoVolumeProcessor() {
        buffer = EMPTY_BUFFER;
        outputBuffer = EMPTY_BUFFER;
        audioFormat = new AudioFormat(Format.NO_VALUE, Format.NO_VALUE, Format.NO_VALUE);
    }
    public IneStereoVolumeProcessor(Listener listener) {
        this();
        SetOnEventListener(listener);
    }
    public void SetOnEventListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public AudioFormat configure(AudioFormat inputAudioFormat) throws AudioProcessor.UnhandledAudioFormatException {
        //int sampleRateHz, int channelCount, @C.Encoding int encoding)


        if (inputAudioFormat.encoding == C.ENCODING_PCM_16BIT && inputAudioFormat.channelCount == 2) {
            audioFormat = new AudioFormat(inputAudioFormat.sampleRate, inputAudioFormat.channelCount, inputAudioFormat.encoding);
            active = true;
        }
        else {
            String message = "音軌錯誤 編碼:" + inputAudioFormat.encoding + " 聲道:"+ inputAudioFormat.channelCount;
            if(listener != null)
                listener.onFormatError(this, inputAudioFormat, message);
            Log.d("VolumeProcessor", message);

        }


        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return active;
    }


    /**
     * Returns the sample rate of audio output by the processor, in hertz. The value may change as a
     * result of calling {@link #configure(AudioFormat)} and is undefined if the instance is not
     * active.
     */

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int position = inputBuffer.position();
        int limit = inputBuffer.limit();
        int size = limit - position;
        int i;
        short sample1, sample2;
        if (buffer.capacity() < size) {
            buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            buffer.clear();
        }

        if (active) {
            switch (AudioControlOutput) {
                case AudioControlOutput_LeftMono:
                    for (i = position; i < limit; i += 4) {
                        sample1 = inputBuffer.getShort(i);
                        buffer.putShort(sample1);
                        buffer.putShort(sample1);
                        }
                    break;
                case AudioControlOutput_RightMono:
                    for (i = position; i < limit; i += 4) {
                        sample2 = inputBuffer.getShort(i + 2);
                        buffer.putShort(sample2);
                        buffer.putShort(sample2);
                        }
                    break;
                default:
                    for (i = position; i < limit; i += 2) {
                        sample1 = inputBuffer.getShort(i);
                        buffer.putShort(sample1);
                    }
                    break;
            }
        } else {
            throw new IllegalStateException();
        }

        inputBuffer.position(limit);
        buffer.flip();
        outputBuffer = buffer;
    }

    @Override
    public void queueEndOfStream() {
        inputEnded = true;
    }

    public void setMode(int audioControlOutput) {
        AudioControlOutput = audioControlOutput;
    }

    @Override
    public ByteBuffer getOutput() {
        ByteBuffer outputBuffer = this.outputBuffer;
        this.outputBuffer = EMPTY_BUFFER;
        return outputBuffer;
    }

    @SuppressWarnings("ReferenceEquality")
    @Override
    public boolean isEnded() {
        return inputEnded && outputBuffer == EMPTY_BUFFER;
    }

    @Override
    public void flush() {
        outputBuffer = EMPTY_BUFFER;
        inputEnded = false;
    }

    @Override
    public void reset() {
        flush();
        buffer = EMPTY_BUFFER;
        audioFormat = new AudioFormat(Format.NO_VALUE, Format.NO_VALUE, Format.NO_VALUE);
        active = false;
    }
}