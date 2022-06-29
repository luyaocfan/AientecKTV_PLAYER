package com.aientec.player2.data

import com.aientec.structure.Track

sealed class MTVEvent {
    class ADD_TRACK(val track: Track?) : MTVEvent()
    class PLAY() : MTVEvent()
    class PAUSE() : MTVEvent()
    class CUT() : MTVEvent()
    class REPLAY() : MTVEvent()
    class MUTE_TOGGLE(val mute: Boolean) : MTVEvent()
    class VOCAL_CHANGE(val type: Int) : MTVEvent()
    class SCORE_TOGGLE(val enable: Boolean) : MTVEvent()
}
