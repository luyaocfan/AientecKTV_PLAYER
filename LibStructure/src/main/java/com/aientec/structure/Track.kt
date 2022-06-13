package com.aientec.structure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * TODO 確認欄位
 *
 * @property id
 * @property name 歌名
 * @property performer 演出者
 */

@Parcelize
open class Track @JvmOverloads constructor(
    var id: Int = -1,
    var sn: String = "",
    var name: String = "",
    var performer: String = "",
    var lyricist: String = "",
    var composer: String = "",
    var language: String = "",
    var imageUrl: String = "",
    var state: State = State.NONE,
    var stateName: String = "",
    var fileName: String = "",
    var bpm: Int = 0,
    var length:Int = 0
) : Parcelable {
    enum class State(var code: Int) {
        NONE(-1), QUEUE(0), NEXT(1), PLAYING(2), DONE(3)
    }

    override fun toString(): String {
        return "Track(id=$id, sn='$sn', name='$name', performer='$performer', lyricist='$lyricist', composer='$composer', language='$language', imageUrl='$imageUrl', state=$state, stateName='$stateName', fileName='$fileName', bpm=$bpm)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}