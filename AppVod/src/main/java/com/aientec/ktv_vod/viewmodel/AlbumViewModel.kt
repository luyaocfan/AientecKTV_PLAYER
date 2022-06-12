package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.structure.Album
import com.aientec.structure.Track
import kotlinx.coroutines.launch

class AlbumViewModel : ViewModelImpl() {
    val lists: MutableLiveData<List<Album>?> = MutableLiveData()

    val selectedList: MutableLiveData<Album?> = MutableLiveData()

    fun onAlbumSelected(album: Album?) {
        viewModelScope.launch {
            val tracks: List<Track>? = repository.getTracksFromAlbum(album?.id ?: return@launch)

            album.tracks = tracks

            selectedList.postValue(album)
        }
    }

    fun updateAlbums(type: Int) {
        viewModelScope.launch {
            lists.postValue(repository.getAlbums(type))
        }
    }
}