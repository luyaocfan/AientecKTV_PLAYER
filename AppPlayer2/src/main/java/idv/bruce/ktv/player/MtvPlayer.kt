package idv.bruce.ktv.player

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import idv.bruce.ktv.KtvPlayerHost
import idv.bruce.ktv.audio.KtvVocalProcessor
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class MtvPlayer(context: Context) : PlayerImpl(context) {
    override val player: ExoPlayer

    private val cacheThread: ExecutorService = Executors.newSingleThreadExecutor()

    private val audioProcessor: KtvVocalProcessor = KtvVocalProcessor()

    private lateinit var simpleCache: SimpleCache

    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    private lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor

    private lateinit var databaseProvider: StandaloneDatabaseProvider

    private val cacheSize: Long = 1024 * 1024 * 4

    init {
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)

        databaseProvider = StandaloneDatabaseProvider(context)

        val path: File = File(context.cacheDir, "video_cache")

        if (!path.exists())
            path.mkdirs()

        simpleCache =
            SimpleCache(path, leastRecentlyUsedCacheEvictor, databaseProvider, null, false, false)

        for (key in simpleCache.keys) {
            Log.d(debugTag, "Key : $key")
            simpleCache.removeResource(key)
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory().setCache(simpleCache).setBufferSize(cacheSize.toInt())
            )
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val renderersFactory: DefaultRenderersFactory =
            object : DefaultRenderersFactory(context) {
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

        player = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()

        player.playWhenReady = true

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                val state: String = when (playbackState) {
                    ExoPlayer.STATE_BUFFERING -> "Buffering"
                    ExoPlayer.STATE_IDLE -> "Idle"
                    ExoPlayer.STATE_ENDED -> "Ended"
                    ExoPlayer.STATE_READY -> "Ready"
                    else -> "Unknown"
                }
                Log.d(debugTag, "onPlaybackStateChanged : $state, ${player.mediaItemCount}")

            }


            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.d("Trace", "onIsPlayingChanged : $isPlaying")
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

                requestNextItem()

                Log.d(
                    "Trace",
                    "onPositionDiscontinuity : ${oldPosition.mediaItemIndex}, ${newPosition.mediaItemIndex}, $reason"
                )
            }
        })
    }

    override fun addMedia(vararg items: KtvPlayerHost.KtvItem) {
        TODO("Not yet implemented")
    }

    override fun pause(): Boolean {
        TODO("Not yet implemented")
    }

    override fun resume(): Boolean {
        TODO("Not yet implemented")
    }

    override fun next(): Boolean {
        if (player.mediaItemCount > 2)
            player.next()
        else
            player.stop()
        return true
    }

    private fun requestNextItem(){

    }
}