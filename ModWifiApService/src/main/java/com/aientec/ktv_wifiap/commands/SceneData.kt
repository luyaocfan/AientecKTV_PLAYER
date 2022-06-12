package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class SceneData(var id: SceneId = SceneId.SWITCH) : DSData() {
    override var length: UShort = 9u

    override val command: CommandList
        get() = CommandList.SCENE

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.put(id.code)
    }

    override fun decodeBody(body: ByteBuffer) {
        val code: Byte = body.get()

        id = SceneId.values().find { it.code == code } ?: SceneId.SWITCH
    }
}