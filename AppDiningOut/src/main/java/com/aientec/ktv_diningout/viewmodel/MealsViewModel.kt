package com.aientec.ktv_diningout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_diningout.common.MealsGroup
import com.aientec.ktv_diningout.model.Repository
import com.aientec.structure.Meals
import idv.bruce.common.impl.ServiceImpl
import idv.bruce.common.impl.ViewModelImpl
import kotlinx.coroutines.launch
import java.io.File

class MealsViewModel : ViewModelImpl() {
    lateinit var mealsGroupList: MutableLiveData<List<MealsGroup>>

    val selectedMealsGroup: MutableLiveData<MealsGroup?> = MutableLiveData()

    val imageFile: MutableLiveData<File?> = MutableLiveData()

    val isUpload: MutableLiveData<Boolean?> = MutableLiveData()

    private lateinit var repository: Repository

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

    fun onGroupSelected(group: MealsGroup, selectedMealsList: List<Meals>) {
        viewModelScope.launch {
            val mGroup: MealsGroup = MealsGroup(group.id, group.boxName)
            mGroup.mealsList.addAll(selectedMealsList)
            selectedMealsGroup.postValue(mGroup)
            imageFile.postValue(null)
            isUpload.postValue(null)
        }
    }

    fun onCaptured(file: File) {
        viewModelScope.launch {
            imageFile.postValue(file)
        }
    }

    fun onReCapture() {
        viewModelScope.launch {
            val file: File = imageFile.value ?: return@launch

            if (file.exists())
                file.delete()

            imageFile.postValue(null)
        }
    }

    fun onMealsConfirm() {
        viewModelScope.launch {
            val group: MealsGroup = selectedMealsGroup.value ?: return@launch

            val imageFile: File = imageFile.value ?: return@launch

            var res: Boolean = repository.mealsDone(group.mealsList)

            if (res)
                res = repository.mealsPicUpload(group.mealsList, imageFile.absolutePath)
            else
                isUpload.postValue(false)

            selectedMealsGroup.postValue(null)
            isUpload.postValue(res)
        }
    }


    override fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        super.onServiceConnected(binder)

        repository = binder.models!!.find { it.tag == "Repo" } as Repository

        mealsGroupList = repository.mealsGroupList
    }
}