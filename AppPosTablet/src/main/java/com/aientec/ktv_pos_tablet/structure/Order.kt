package com.aientec.ktv_pos_tablet.structure

import com.aientec.structure.Room

class Order {
    var boxId: Int = -1

    var duration: Long = 0

    var owner: String = ""

    var count: Int = 0

    var startTime: Long = 0



    var boxType: Room.Type? = null

    var itemList: ArrayList<OrderItem> = ArrayList()

    fun addItems(vararg items: OrderItem) {
        itemList.addAll(items)
    }

    fun removeItems(vararg items: OrderItem) {
        for (item in items)
            itemList.remove(item)
    }

    fun updateItems(vararg items: OrderItem) {
        for (item in items)
            itemList[itemList.indexOf(item)] = item
    }
}