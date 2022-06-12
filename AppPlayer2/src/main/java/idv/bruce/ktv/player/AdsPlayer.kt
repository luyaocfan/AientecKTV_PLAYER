package idv.bruce.ktv.player

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import idv.bruce.ktv.KtvPlayerHost

internal class AdsPlayer(context: Context) : PlayerImpl(context) {
    override val player: ExoPlayer
        get() = ExoPlayer.Builder(context).build()

    init {
        player.playWhenReady = true
    }

    override fun addMedia(vararg items: KtvPlayerHost.KtvItem) {
        if (player.playbackState == ExoPlayer.STATE_READY)
            player.stop()

        if (player.mediaItemCount > 0)
            player.removeMediaItems(0, player.mediaItemCount)

        val list: ArrayList<MediaItem> = ArrayList()

        for (item in items)
            list.add(MediaItem.fromUri(item.uri ?: continue))

        player.prepare()
    }

    override fun pause(): Boolean = false

    override fun resume(): Boolean = false

    override fun next(): Boolean = false
}