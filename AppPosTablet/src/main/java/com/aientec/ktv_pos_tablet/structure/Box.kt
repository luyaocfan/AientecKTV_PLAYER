package com.aientec.ktv_pos_tablet.structure

import com.aientec.structure.Room

class Box : Room {
    constructor() : super()

    constructor(room: Room) : super(
        room.id,
        room.name,
        room.type,
        room.floor,
        room.state,
        room.rect
    )

    var order: Order? = null
}