package com.aientec.player2.data

sealed class PlayerControl {
    object RESUME : PlayerControl()
    object PAUSE : PlayerControl()
    object CUT : PlayerControl()
    object REPLAY : PlayerControl()
    class MUTE(val mute: Boolean) : PlayerControl()
    class RATING(val enable: Boolean) : PlayerControl()
    class VOCAL(val type: Int) : PlayerControl()
}