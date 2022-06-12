package com.aientec.appplayer.data

class MessageBundle {
    enum class Type {
        TXT, IMAGE, VIDEO, EMOJI, VOD, NONE, TEST
    }

    var senderIcon: String? = null

    var sender: String? = null

    var type: Type = Type.NONE

    var data: Any? = null
}