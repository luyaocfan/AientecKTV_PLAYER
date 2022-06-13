package idv.bruce.ktv.audio

import androidx.annotation.CallSuper
import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.audio.AudioProcessor.EMPTY_BUFFER
import com.google.android.exoplayer2.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder

class KtvVocalProcessor : AudioProcessor {
      enum class VocalType {
            ORIGIN, BACKING, GUIDE
      }

      /** The current input audio format.  */
      protected var inputAudioFormat: AudioProcessor.AudioFormat =
            AudioProcessor.AudioFormat.NOT_SET

      /** The current output audio format.  */
      protected var outputAudioFormat: AudioProcessor.AudioFormat =
            AudioProcessor.AudioFormat.NOT_SET

      private var pendingInputAudioFormat: AudioProcessor.AudioFormat =
            AudioProcessor.AudioFormat.NOT_SET
      private var pendingOutputAudioFormat: AudioProcessor.AudioFormat =
            AudioProcessor.AudioFormat.NOT_SET
      private var buffer: ByteBuffer = EMPTY_BUFFER
      private var outputBuffer: ByteBuffer? = null
      private var inputEnded = false

      var type: VocalType = VocalType.ORIGIN

      override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
            pendingInputAudioFormat = inputAudioFormat
            pendingOutputAudioFormat = pendingInputAudioFormat
            return if (isActive) pendingOutputAudioFormat else AudioProcessor.AudioFormat.NOT_SET
      }

      override fun isActive(): Boolean {
            return pendingOutputAudioFormat != AudioProcessor.AudioFormat.NOT_SET
      }

      override fun queueInput(inputBuffer: ByteBuffer) {
            var position = inputBuffer.position()
            val limit = inputBuffer.limit()
            val size = limit - position

            if (buffer.capacity() < size) {
                  buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
            } else {
                  buffer.clear();
            }

            if (isActive) {
                  try {
                        var sample: Short
                        when (type) {
                              VocalType.ORIGIN -> {
                                    buffer.put(inputBuffer)
                              }
                              VocalType.BACKING -> {
                                    while (position < limit) {
                                          sample = inputBuffer.getShort(position)
                                          buffer.putShort(sample)
                                          position += 2
                                    }
                              }
                              VocalType.GUIDE -> {
                                    while (position < limit) {
                                          sample = inputBuffer.getShort(position + 2)
                                          buffer.putShort(sample)
                                          buffer.putShort(sample)
                                          position += 4
                                    }
                              }
                        }
                  } catch (e: Exception) {

                  }
                  inputBuffer.position(limit)
                  buffer.flip()
                  outputBuffer = buffer
            }
      }

      override fun queueEndOfStream() {
            inputEnded = true
      }

      override fun getOutput(): ByteBuffer {
            val outputBuffer = outputBuffer
            this.outputBuffer = EMPTY_BUFFER
            return outputBuffer!!
      }

      override fun isEnded(): Boolean {
            return inputEnded && outputBuffer === EMPTY_BUFFER
      }

      override fun flush() {
            outputBuffer = EMPTY_BUFFER
            inputEnded = false
            inputAudioFormat = pendingInputAudioFormat
            outputAudioFormat = pendingOutputAudioFormat!!
      }

      override fun reset() {
            flush()
            buffer = EMPTY_BUFFER
            pendingInputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
            pendingOutputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
            inputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
            outputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
      }

}