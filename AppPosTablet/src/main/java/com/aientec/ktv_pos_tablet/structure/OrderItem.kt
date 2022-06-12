package com.aientec.ktv_pos_tablet.structure

class OrderItem(var id: Int = -1, var name: String = "", var count: Int = 0){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}