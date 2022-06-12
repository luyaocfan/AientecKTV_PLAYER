package com.aientec.ktv_vod.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.structure.Singer
import com.aientec.structure.Track
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class SearchViewModel : ViewModelImpl() {

      private var mSelectType: Int = -1

      private var mSearchKeys: StringBuilder = StringBuilder()

      private var mKeyType: Int = 1

      val selectType: MutableLiveData<Int> = MutableLiveData(mSelectType)

      val keyType: MutableLiveData<Int> = MutableLiveData(mKeyType)

      val searchKeys: MutableLiveData<String> = MutableLiveData(mSearchKeys.toString())

      val singerResults: MutableLiveData<List<Singer>> = MutableLiveData()

      val trackResults: MutableLiveData<List<Track>> = MutableLiveData()

      var filterKeys: MutableLiveData<List<String>?> = MutableLiveData()

      fun onSelectTypeChanged(type: Int) {
            viewModelScope.launch {
                  if (mSelectType != type) {
                        Log.d("Trace", "Select type : $type")
                        mSelectType = type
                        selectType.postValue(mSelectType)
                        onKeyInput("", -22)
                  }
            }
      }

      fun onKeyTypeChanged(type: Int) {
            viewModelScope.launch {
                  if (mKeyType != type) {
                        mKeyType = type
                        keyType.postValue(mKeyType)
                        onKeyInput("", -22)
                  }
            }
      }

      fun onKeyInput(key: String, fn: Int) {
            viewModelScope.launch {
                  when (fn) {
                        -21 -> {
                              if (!mSearchKeys.isNullOrEmpty())
                                    mSearchKeys.deleteCharAt(mSearchKeys.lastIndex)
                        }
                        -22 -> mSearchKeys.clear()
                        else -> mSearchKeys.append(key)
                  }

                  searchKeys.postValue(mSearchKeys.toString())

                  if (mSelectType < 4) {
                        singerResults.postValue(
                              repository.searchSingers(
                                    mSelectType,
                                    mSearchKeys.toString(),
                                    mKeyType
                              )
                        )
                  } else {
                        trackResults.postValue(
                              repository.searchTracks(
                                    mSearchKeys.toString(),
                                    mKeyType
                              )
                        )
                  }
                  updateSearchKeys()
            }
      }

      private fun updateSearchKeys() {
            viewModelScope.launch {
                  filterKeys.postValue(
                        repository.quickFilterKeys(
                              mSearchKeys.toString(), mSelectType, mKeyType
                        )
                  )
            }
      }
}