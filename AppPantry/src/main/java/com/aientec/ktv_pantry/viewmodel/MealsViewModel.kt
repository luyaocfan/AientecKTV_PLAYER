package com.aientec.ktv_pantry.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_pantry.model.Hardware
import com.aientec.ktv_pantry.model.Repository
import com.aientec.structure.Meals
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class MealsViewModel : ViewModelImpl() {
    lateinit var mealsList: MutableLiveData<List<Meals>>

    private lateinit var repository: Repository

    private lateinit var hardware: Hardware

    fun startUpdate() {
        viewModelScope.launch {
            repository.startUpdate()
        }
    }

    fun stopUpdate() {
        viewModelScope.launch {
            repository.stopUpdate()
        }
    }

    fun printItem(list: List<Meals>) {
        viewModelScope.launch {
            if (list.isEmpty()) return@launch

            for (item in list)
                hardware.printMealItem(item)
        }
    }

    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)
        repository = binder.models!!.find { it.tag == "Repo" } as Repository

        hardware = binder.getModel("HW") as Hardware

        mealsList = repository.mealsList
    }
}