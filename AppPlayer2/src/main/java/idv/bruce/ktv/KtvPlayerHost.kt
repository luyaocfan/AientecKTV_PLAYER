package idv.bruce.ktv

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import idv.bruce.ktv.player.AdsPlayer
import idv.bruce.ktv.player.PlayerImpl
import java.util.*

class KtvPlayerHost(context: Context) {

    enum class PlayerType {
        ADS_PLAYER, MTV_PLAYER
    }

    class KtvItem(val tag: Any, val fileName: String) {
        internal val uri: Uri?
            get() {
                if (hosts == null) return null

                return Uri.parse(hosts!![index] + fileName)
            }

        internal var hosts: Array<out String>? = null

        internal fun nextHost(): Boolean {
            if (++index >= maxCount) return false
            return true
        }

        private var index = 0

        private val maxCount: Int
            get() {
                return hosts?.size ?: -1
            }
    }

    private var mHosts: Array<out String>? = null

    private val mPlayerMap: EnumMap<PlayerType, PlayerImpl> = EnumMap(PlayerType::class.java)

    private var currantPlayer: PlayerImpl? = null

    private var mSurfaceView: SurfaceView? = null

    init {
        mPlayerMap[PlayerType.ADS_PLAYER] = AdsPlayer(context)

        currantPlayer = mPlayerMap[PlayerType.ADS_PLAYER]
    }

    fun setMediaHosts(vararg urls: String) {
        mHosts = urls
    }

    fun attachSurfaceView(surfaceView: SurfaceView) {
        mSurfaceView = surfaceView
    }

    fun addMediaItems(type: PlayerType, vararg items: KtvItem) {
        for (item in items)
            item.hosts = mHosts

        mPlayerMap[type]?.addMedia(*items)
    }

    fun next(): Boolean = currantPlayer?.next() ?: false

    fun resume(): Boolean = currantPlayer?.resume() ?: false

    fun pause(): Boolean = currantPlayer?.pause() ?: false

    fun releasePlayer(type: PlayerType) {
        val player: PlayerImpl = mPlayerMap.remove(type) ?: return
        player.release()
    }

    fun releaseAllPlayer() {
        for (tag in mPlayerMap.keys)
            releasePlayer(tag)
    }
}