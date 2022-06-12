package com.aientec.ktv_wifiap.commands

import java.nio.ByteBuffer

class ServiceData(
    var ice: Byte = 0x00,
    var water: Byte = 0x00,
    var tea: Byte = 0x00,
    var paper: Byte = 0x00,
    var clean: Byte = 0x00,
    var pay: Byte = 0x00,
    var other: Byte = 0x00,
) : DSData() {
    override var length: UShort = 15u

    override val command: CommandList
        get() = CommandList.SERVICE_CTRL

    override fun encodeBody(buffer: ByteBuffer) {
        buffer.apply {
            put(ice)
            put(water)
            put(tea)
            put(paper)
            put(clean)
            put(pay)
            put(other)
        }
    }

    override fun decodeBody(body: ByteBuffer) {
        body.apply {
            ice = get()
            water = get()
            tea = get()
            paper = get()
            clean = get()
            pay = get()
            other = get()
        }
    }
}