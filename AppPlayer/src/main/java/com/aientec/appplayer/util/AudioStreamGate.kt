package com.aientec.appplayer.util

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
class AudioStreamGate {
      companion object {
            private const val SAMPLE_RATE: Int = 48000

            private var BUFFER_SIZE: Int = AudioRecord.getMinBufferSize(
                  SAMPLE_RATE,
                  AudioFormat.CHANNEL_IN_MONO,
                  AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)
      }

      var threshold: Float = -30.0f

      var travelTime: Long = -1L

      private val thread: ExecutorService = Executors.newSingleThreadExecutor()

      private var future: Future<*>? = null

      private var mPipeline: AudioStreamPipeline? = null

      private var mAudioRecorder: AudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
      )

      fun enable() {
            if (future != null) return

            mPipeline?.onEnable()

            future = thread.submit(recorderRunnable)
      }

      fun disable() {
            if (future == null) return

            mPipeline?.onDisable()

            future!!.cancel(true)
      }

      fun addStreamPipeline(pipeline: AudioStreamPipeline) {
            if (mPipeline == null)
                  mPipeline = pipeline
            else
                  mPipeline!!.addSink(pipeline)
      }

      protected fun finalize() {
            if (future != null)
                  future!!.cancel(true)
      }

      private val recorderRunnable: Runnable = Runnable {
            val buffer: ByteArray = ByteArray(BUFFER_SIZE)

            var startTime: Long = 0L

            var currantTime: Long

            var dataLength: Int

            var value: Long

            var rms: Double

            var volume: Double = 0.0

            var smoothRms: Double = 0.0

            mAudioRecorder.startRecording()

            var toggle: Boolean = false

            try {

                  while (true) {
                        dataLength = mAudioRecorder.read(buffer, 0, BUFFER_SIZE / 2)

                        if (toggle) {
                              currantTime = System.currentTimeMillis()

                              if (startTime == -1L) {
                                    startTime = System.currentTimeMillis()
                                    mPipeline?.onTriggerChanged(true, volume)
                              }

                              if (currantTime - startTime > travelTime && travelTime != -1L) {
                                    toggle = false

                                    smoothRms = 0.0

                                    mPipeline?.onTriggerChanged(false)

                                    continue
                              }

                              val data: ByteArray = ByteArray(dataLength)

                              buffer.copyInto(data, 0, 0, dataLength)

                              mPipeline?.onProcessingData(data)

                        } else {
                              value = 0

                              for (i in 0 until dataLength step 2)
                                    value += (buffer[i].toInt() + (buffer[i + 1].toInt() shl 8)) * (buffer[i].toInt() + (buffer[i + 1].toInt() shl 8))


                              rms = sqrt((value / (32768.0 * 32768.0)) / dataLength * 2.0)


                              volume = 20.0 * log10(abs(rms))

//                              Log.d("Volume", "Volume : $volume")

                              if (volume > threshold) {
                                    toggle = true

                                    startTime = -1L
                              }
                        }
                  }
            } catch (e: InterruptedException) {
                  e.printStackTrace()
            } finally {
                  mAudioRecorder.stop()

                  future = null
            }
      }

      abstract class AudioStreamPipeline {
            private var mSink: AudioStreamPipeline? = null

            abstract fun onStartTrigger(volume: Double)
            abstract fun onAudioDataFrame(data: ByteArray)
            abstract fun onStopTrigger()

            internal fun onProcessingData(data: ByteArray) {
                  onAudioDataFrame(data)
                  mSink?.onProcessingData(data)
            }

            internal fun onTriggerChanged(enable: Boolean, volume: Double = 0.0) {
                  if (enable)
                        onStartTrigger(volume)
                  else
                        onStopTrigger()
                  mSink?.onTriggerChanged(enable)
            }

            internal fun addSink(sink: AudioStreamPipeline) {
                  if (mSink == null)
                        mSink = sink
                  else
                        mSink!!.addSink(sink)
            }

            open fun onEnable() {
                  mSink?.onEnable()
            }

            open fun onDisable() {
                  mSink?.onDisable()
            }
      }
}