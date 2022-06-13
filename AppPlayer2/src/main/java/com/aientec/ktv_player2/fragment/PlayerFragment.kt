package com.aientec.ktv_player2.fragment

import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_player2.databinding.FragmentPlayerBinding
import com.aientec.ktv_player2.viewmodel.PlayerViewModel
import com.aientec.structure.Track
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoSize
import idv.bruce.ktv.audio.KtvVocalProcessor
import java.util.*

class PlayerFragment : Fragment() {

      private lateinit var binding: FragmentPlayerBinding

      private lateinit var idlePlayer: ExoPlayer

      private lateinit var player: ExoPlayer

      private var currantPlayer: ExoPlayer? = null

      private val playerViewModel: PlayerViewModel by activityViewModels()

      private val prefix: String = com.aientec.ktv_player2.BuildConfig.MTV_URL

      private val mediaUriList: LinkedList<Uri> = LinkedList<Uri>()

      private val audioProcessor: KtvVocalProcessor = KtvVocalProcessor()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentPlayerBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            initPlayer()

            binding.log.movementMethod = ScrollingMovementMethod()

            binding.test.setOnClickListener {
                  testFn()
//            if (player.hasNext())
//                player.next()
//                  audioProcessor.type = when (audioProcessor.type) {
//                        KtvVocalProcessor.VocalType.ORIGIN -> KtvVocalProcessor.VocalType.BACKING
//                        KtvVocalProcessor.VocalType.BACKING -> KtvVocalProcessor.VocalType.GUIDE
//                        KtvVocalProcessor.VocalType.GUIDE -> KtvVocalProcessor.VocalType.ORIGIN
//                  }
            }

            playerViewModel.adsTrackList.observe(viewLifecycleOwner) {
                  if (it != null) {
                        startAds(it)
                  }
            }
      }

      override fun onResume() {
            super.onResume()
            playerViewModel.updateAdsList()
      }

      private fun initPlayer() {
            idlePlayer = ExoPlayer.Builder(requireContext())
                  .setLoadControl(
                        DefaultLoadControl.Builder()
                              .setBackBuffer(0, false)
                              .setBufferDurationsMs(3000, 6000, 3000, 3000)
                              .build()
                  )
                  .build()

            idlePlayer.playWhenReady = true

            idlePlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL

            idlePlayer.addAnalyticsListener(EventLogger(null, "KTV_PLAYER_IDLE"))

            player = ExoPlayer.Builder(requireContext())
                  .setLoadControl(
                        DefaultLoadControl.Builder()
                              .setBackBuffer(0, false)
                              .setBufferDurationsMs(3000, 6000, 3000, 3000)
                              .build()
                  )
                  .build()

            player.pauseAtEndOfMediaItems = true

            player.playWhenReady = true

            player.addListener(object : Player.Listener {
                  override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                        super.onTimelineChanged(timeline, reason)
                  }

                  override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                  }

                  override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
                        super.onTracksInfoChanged(tracksInfo)
                  }

                  override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        super.onMediaMetadataChanged(mediaMetadata)
                  }

                  override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
                        super.onPlaylistMetadataChanged(mediaMetadata)
                  }

                  override fun onIsLoadingChanged(isLoading: Boolean) {
                        super.onIsLoadingChanged(isLoading)
                  }

                  override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                        super.onAvailableCommandsChanged(availableCommands)
                  }

                  override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                  }

                  override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)
                  }

                  override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                        super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
                  }

                  override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        switchSurface()
                  }

                  override fun onRepeatModeChanged(repeatMode: Int) {
                        super.onRepeatModeChanged(repeatMode)
                  }

                  override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                  }

                  override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                  }

                  override fun onPlayerErrorChanged(error: PlaybackException?) {
                        super.onPlayerErrorChanged(error)
                  }

                  override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                  ) {
                        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                  }

                  override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        super.onPlaybackParametersChanged(playbackParameters)
                  }

                  override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
                        super.onSeekBackIncrementChanged(seekBackIncrementMs)
                  }

                  override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
                        super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
                  }

                  override fun onEvents(player: Player, events: Player.Events) {
                        super.onEvents(player, events)
                  }

                  override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        super.onAudioSessionIdChanged(audioSessionId)
                  }

                  override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                        super.onAudioAttributesChanged(audioAttributes)
                  }

                  override fun onVolumeChanged(volume: Float) {
                        super.onVolumeChanged(volume)
                  }

                  override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
                        super.onSkipSilenceEnabledChanged(skipSilenceEnabled)
                  }

                  override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
                        super.onDeviceInfoChanged(deviceInfo)
                  }

                  override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                        super.onDeviceVolumeChanged(volume, muted)
                  }

                  override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                  }

                  override fun onSurfaceSizeChanged(width: Int, height: Int) {
                        super.onSurfaceSizeChanged(width, height)
                  }

                  override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()
                  }

                  override fun onCues(cues: MutableList<Cue>) {
                        super.onCues(cues)
                  }

                  override fun onMetadata(metadata: Metadata) {
                        super.onMetadata(metadata)
                  }
            })

            player.addAnalyticsListener(EventLogger(null, "KTV_PLAYER"))

            switchSurface()
      }

      private fun startAds(list: List<Track>) {
            mediaUriList.clear()

            idlePlayer.stop()

            idlePlayer.clearMediaItems()

            val concatenatingMediaSource = ConcatenatingMediaSource().apply {
                  for (track in list) {
                        val uri: Uri =
                              Uri.parse(String.format(Locale.TAIWAN, prefix, track.fileName))

                        addMediaSource(
                              ProgressiveMediaSource.Factory(
                                    DefaultDataSourceFactory(
                                          requireContext()
                                    )
                              ).createMediaSource(MediaItem.fromUri(uri))
                        )
                  }
            }

            idlePlayer.addMediaSource(concatenatingMediaSource)

            idlePlayer.prepare()

      }

      private fun testFn() {
            val uri: Uri =
                  Uri.parse(String.format(Locale.TAIWAN, prefix, "52898YH1.mp4"))

            val mediaSource = ProgressiveMediaSource.Factory(
                  DefaultDataSourceFactory(
                        requireContext()
                  )
            ).createMediaSource(MediaItem.fromUri(uri))

            player.addMediaSource(mediaSource)

            player.prepare()
      }

      private fun switchSurface() {
            currantPlayer = if (currantPlayer == null || currantPlayer == player) {
                  player.stop()
                  player.clearMediaItems()
                  player.setVideoSurfaceView(null)
                  idlePlayer.setVideoTextureView(binding.display)
                  idlePlayer.play()
                  idlePlayer
            } else {
                  idlePlayer.pause()
                  idlePlayer.setVideoSurfaceView(null)
                  player.setVideoTextureView(binding.display)
                  player
            }
      }
}