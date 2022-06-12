package com.aientec.ktv_diningout.common

import com.aientec.structure.Meals

class MealsGroup(val id: String, val boxName: String) {
    val mealsList: ArrayList<Meals> = ArrayList()
}