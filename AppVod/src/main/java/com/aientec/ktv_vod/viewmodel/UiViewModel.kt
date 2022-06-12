package com.aientec.ktv_vod.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import kotlinx.coroutines.launch

class UiViewModel : ViewModelImpl() {
      val quickSearch: MutableLiveData<Boolean> = MutableLiveData(false)

      val searchFragmentQueue: MutableLiveData<List<Pair<String, Int>>> = MutableLiveData()

      private val mSearchingQueue: ArrayList<Pair<String, Int>> = ArrayList()

      val backSearchFragment: MutableLiveData<Int?> = MutableLiveData()

      val mainPage: MutableLiveData<Int> = MutableLiveData(R.id.homeFragment)

      private val mControlAction: MutableLiveData<Pair<String, Bundle?>> = MutableLiveData()

      val controlAction: LiveData<Pair<String, Bundle?>>
            get() = mControlAction

      val pageFlag: MutableLiveData<Pair<Boolean, Boolean>> = MutableLiveData(
            Pair(
                  first = false,
                  second = false
            )
      )

      fun onControlActionEvent(action: String, bundle: Bundle? = null) {
            mControlAction.postValue(Pair(action, bundle))
      }

      fun setMainPage(res: Int) {
            if (mainPage.value!! != res)
                  mainPage.postValue(res)
      }

      fun onMainPageUpdate(res: Int) {
            if (mainPage.value!! != res) {
                  mainPage.value = res
            }
      }

      fun toggleTrackPage() {

            val flag: Pair<Boolean, Boolean> = pageFlag.value!!

            pageFlag.postValue(Pair(true, flag.second))
      }


      fun onSearchHome() {
            viewModelScope.launch {
                  mSearchingQueue.clear()
                  searchFragmentQueue.postValue(mSearchingQueue)
            }
      }

      fun onSearchBack() {
            viewModelScope.launch {
                  val res: Int =
                        if (mSearchingQueue.size < 2) -1 else mSearchingQueue[mSearchingQueue.lastIndex - 1].second
                  backSearchFragment.postValue(res)
            }
      }

      fun onSearchFragmentInto(title: String, action: Int) {
            viewModelScope.launch {
                  val flag: Pair<String, Int> = Pair(title, action)
                  if (mSearchingQueue.contains(flag)) {
                        val index = mSearchingQueue.indexOf(flag)
                        if (index != mSearchingQueue.lastIndex) {
                              mSearchingQueue.removeAll(
                                    mSearchingQueue.subList(
                                          index,
                                          mSearchingQueue.lastIndex
                                    ).toSet()
                              )
                              searchFragmentQueue.postValue(mSearchingQueue)
                        }
                  } else {
                        mSearchingQueue.add(flag)
                        searchFragmentQueue.postValue(mSearchingQueue)
                  }
            }
      }

      fun isQuickSearch(flag: Boolean) {
            viewModelScope.launch {
                  backSearchFragment.value = null
                  quickSearch.postValue(flag)
            }

      }
}