package com.aientec.ktv_player2.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_player2.databinding.FragmentPlayerBinding
import com.aientec.ktv_player2.viewmodel.PlayerViewModel
import com.aientec.structure.Track
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.EventLogger
import idv.bruce.ktv.audio.KtvVocalProcessor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlayerFragment : Fragment() {

      private lateinit var binding: FragmentPlayerBinding

      private lateinit var player: ExoPlayer

      private val playerViewModel: PlayerViewModel by activityViewModels()

      private lateinit var simpleCache: SimpleCache

      private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

      private lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor

      private lateinit var databaseProvider: StandaloneDatabaseProvider

      private val cacheSize: Long = 1024 * 1024 * 4

      private val prefix: String = "http://106.104.151.145:10003/mtv/%s"

      private val mediaUriList: LinkedList<Uri> = LinkedList<Uri>()

      private val cacheThread: ExecutorService = Executors.newSingleThreadExecutor()

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
//            if (player.hasNext())
//                player.next()
                  audioProcessor.type = when (audioProcessor.type) {
                        KtvVocalProcessor.VocalType.ORIGIN -> KtvVocalProcessor.VocalType.BACKING
                        KtvVocalProcessor.VocalType.BACKING -> KtvVocalProcessor.VocalType.GUIDE
                        KtvVocalProcessor.VocalType.GUIDE -> KtvVocalProcessor.VocalType.ORIGIN
                  }
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


            leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)

            databaseProvider = StandaloneDatabaseProvider(requireContext())

            val path: File = File(requireContext().cacheDir, "video_cache")

            if (!path.exists())
                  path.mkdirs()

            simpleCache =
                  SimpleCache(
                        path,
                        leastRecentlyUsedCacheEvictor,
                        databaseProvider,
                        null,
                        false,
                        false
                  )

            for (key in simpleCache.keys) {
                  Log.d("Trace", "Key : $key")
                  simpleCache.removeResource(key)
            }

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                  .setAllowCrossProtocolRedirects(true)

            cacheDataSourceFactory = CacheDataSource.Factory()
                  .setCache(simpleCache)
                  .setCacheWriteDataSinkFactory(
                        CacheDataSink.Factory().setCache(simpleCache)
                              .setBufferSize(cacheSize.toInt())
                  )
                  .setUpstreamDataSourceFactory(httpDataSourceFactory)
                  .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                  .setEventListener(cacheEventListener)

            val renderersFactory: DefaultRenderersFactory =
                  object : DefaultRenderersFactory(requireContext()) {
                        override fun buildAudioSink(
                              context: Context,
                              enableFloatOutput: Boolean,
                              enableAudioTrackPlaybackParams: Boolean,
                              enableOffload: Boolean
                        ): AudioSink? {
                              return DefaultAudioSink(
                                    AudioCapabilities.getCapabilities(context),
                                    DefaultAudioSink.DefaultAudioProcessorChain(audioProcessor),
                                    enableFloatOutput,
                                    enableAudioTrackPlaybackParams,
                                    if (enableOffload) DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED else DefaultAudioSink.OFFLOAD_MODE_DISABLED
                              )
                        }
                  }

            player = ExoPlayer.Builder(requireContext())
                  .setRenderersFactory(renderersFactory)
                  .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                  .build()

            player.playWhenReady = true

            player.repeatMode = ExoPlayer.REPEAT_MODE_ALL

            player.setVideoSurfaceView(binding.display)

            player.addAnalyticsListener(EventLogger(null, "KTV_PLAYER"))

            player.addListener(object : Player.Listener {
                  override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Log.e("Trace", "onPlayerError : ${error.message}, ${player.mediaItemCount}")

                        logger("onPlayerError : ${error.message}, ${player.mediaItemCount}")
                  }

                  override fun onPlayerErrorChanged(error: PlaybackException?) {
                        super.onPlayerErrorChanged(error)
                        Log.d(
                              "Trace",
                              "onPlayerErrorChanged : ${error?.message}, ${player.mediaItemCount}"
                        )
                        logger("onPlayerErrorChanged : ${error?.message}, ${player.mediaItemCount}")
                  }

                  override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        val state: String = when (playbackState) {
                              ExoPlayer.STATE_BUFFERING -> "Buffering"
                              ExoPlayer.STATE_IDLE -> "Idle"
                              ExoPlayer.STATE_ENDED -> "Ended"
                              ExoPlayer.STATE_READY -> "Ready"
                              else -> "Unknown"
                        }
                        Log.d("Trace", "onPlaybackStateChanged : $state, ${player.mediaItemCount}")

                        logger("onPlaybackStateChanged : $state, ${player.mediaItemCount}")
                  }

                  override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        Log.d("Trace", "onIsPlayingChanged : $isPlaying")

                        logger("onIsPlayingChanged : $isPlaying")
                  }

                  override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                  ) {
                        super.onPositionDiscontinuity(oldPosition, newPosition, reason)

                        if (reason == ExoPlayer.DISCONTINUITY_REASON_REMOVE) return

                        if (player.mediaItemCount > 1) {
                              val mediaItem: MediaItem = player.getMediaItemAt(0)

                              simpleCache.removeResource(
                                    cacheDataSourceFactory.cacheKeyFactory.buildCacheKey(
                                          DataSpec(mediaItem.localConfiguration!!.uri)
                                    )
                              )

                              player.removeMediaItem(0)
                        }
                        play()

                        Log.d(
                              "Trace",
                              "onPositionDiscontinuity : ${oldPosition.mediaItemIndex}, ${newPosition.mediaItemIndex}, $reason"
                        )

                        logger("onPositionDiscontinuity : ${oldPosition.mediaItemIndex}, ${newPosition.mediaItemIndex}, $reason")
                  }
            })
      }

      private fun startAds(list: List<Track>) {
            mediaUriList.clear()

            player.stop()

            player.clearMediaItems()

            for (track in list) {
                  val uri: String = String.format(Locale.TAIWAN, prefix, track.fileName)

                  mediaUriList.add(Uri.parse(uri))
            }

            Log.d("Trace", "Media item : ${mediaUriList.size}")

            play()
      }

      private fun play() {

            val mediaUri: Uri = mediaUriList.poll() ?: return

            val mediaItem: MediaItem = MediaItem.fromUri(mediaUri)


            player.addMediaSource(
                  ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(
                              mediaItem
                        )
            )

            if (player.playbackState == ExoPlayer.STATE_IDLE)
                  player.prepare()
            else
                  cacheThread.submit(PrecacheRunnable(mediaUri))


            if (player.mediaItemCount < 2)
                  play()
      }

      @SuppressLint("SetTextI18n")
      private fun logger(msg: String) {
            MainScope().launch {
                  binding.log.text = "$msg\n${binding.log.text}"
            }
      }

      private val cacheEventListener: CacheDataSource.EventListener =
            object : CacheDataSource.EventListener {
                  override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
                        Log.d(
                              "Trace",
                              "Cache size : $cacheSizeBytes, Cache read : $cachedBytesRead"
                        )

                  }

                  override fun onCacheIgnored(reason: Int) {
                        Log.d("Trace", "Cache ignored : $reason")
                  }
            }

      private inner class PrecacheRunnable(val uri: Uri) : Runnable {
            private val buffer: ByteArray = ByteArray(4096)

            override fun run() {

                  val dataSpec: DataSpec = DataSpec(uri)

                  runCatching {
                        Log.d("Trace", "Precache : $uri")
                        logger("Precache : $uri")
                        val cacheWriter: CacheWriter = CacheWriter(
                              cacheDataSourceFactory.createDataSource(),
                              dataSpec,
                              buffer
                        ) { requestLength, bytesCached, _ ->
                              if (bytesCached >= requestLength) {
                                    Log.d("Trace", "Cache complete")
                                    logger("Cache complete")
                              }
                        }
                        cacheWriter.cache()
                  }.onFailure {
                        it.printStackTrace()
                  }
            }
      }


}