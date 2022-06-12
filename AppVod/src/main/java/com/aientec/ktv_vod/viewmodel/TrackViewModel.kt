package com.aientec.ktv_vod.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aientec.ktv_vod.common.impl.ViewModelImpl
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.service.VodService
import com.aientec.ktv_vod.structure.Singer
import com.aientec.structure.Album
import com.aientec.structure.Track
import kotlinx.coroutines.launch

class TrackViewModel : ViewModelImpl() {
      var idleTracks: MutableLiveData<List<Track>>? = null

      lateinit var playingTracks: MutableLiveData<ArrayList<Track>>

      lateinit var currantPlayTrack: LiveData<Track?>

      lateinit var prepareTrack: LiveData<Track?>

      val tracks: MutableLiveData<List<Track>?> = MutableLiveData()

      private var mSelectedTrack: Track? = null
            set(value) {
                  field = value
                  selectedTrack.postValue(field)
            }

      val selectedTrack: MutableLiveData<Track?> = MutableLiveData()

      var albums: MutableLiveData<List<Album>>? = null

      val lists: MutableLiveData<List<Album>?> = MutableLiveData()

      val inputType: MutableLiveData<Repository.InputType> =
            MutableLiveData(Repository.InputType.PHONETIC)

      var searchingText: MutableLiveData<String> = MutableLiveData("")

      var searchingKeys: MutableLiveData<List<String>> = MutableLiveData()

      override fun onServiceConnected(service: VodService) {
            super.onServiceConnected(service)
            albums = repository.albums
            idleTracks = repository.idleTracks
            playingTracks = repository.playingTracks
            currantPlayTrack = repository.currantPlayingTrack
            prepareTrack = repository.prepareTrack
      }

      fun onKeyInput(value: String) {
            viewModelScope.launch {
                  var key: String = searchingText.value ?: ""
                  key += value
                  searchingText.postValue(key)
                  tracks.postValue(repository.quickFilterTracks(inputType.value!!, key))
                  updateSearchKeys(key)
            }
      }

      fun onKeyBack() {
            viewModelScope.launch {
                  var key: String = searchingText.value ?: return@launch
                  if (key.isNotEmpty()) {
                        key = key.substring(0, key.length - 1)
                        searchingText.postValue(key)
                        if (key.isNotEmpty())
                              tracks.postValue(repository.quickFilterTracks(inputType.value!!, key))
                        else
                              tracks.postValue(null)
                  }
                  updateSearchKeys(key)
            }
      }

      fun onKeyClean() {
            searchingText.postValue("")
            tracks.postValue(null)
            updateSearchKeys("")
      }

      fun onInputTypeChange(type: Repository.InputType) {
            inputType.postValue(type)
            searchingText.postValue("")
            tracks.postValue(null)
      }

      fun updateSearchKeys(key: String) {
            viewModelScope.launch {
//                  searchingKeys.postValue(
////                        repository.quickFilterKeys(inputType.value!!, key)
//                  )
            }
      }

      fun onAlbumSelected(album: Album) {
            viewModelScope.launch {
                  tracks.postValue(repository.getTracksFromAlbum(album.id))
            }
      }

      fun onSingerSelected(singer: Singer) {
            viewModelScope.launch {
                  tracks.postValue(repository.getTracksFromSinger(singer))
            }
      }

      fun onTrackSelected(track: Track?) {
            mSelectedTrack = track
      }

      fun onOrderTrack() {
            val track: Track = mSelectedTrack ?: return
            viewModelScope.launch {
                  val result: Boolean = repository.trackCommand(track, 1)
//                  toast.postValue(if (result) "點播成功" else "點播失敗")
            }
      }

      fun onInsertTrack() {
            val track: Track = mSelectedTrack ?: return
            viewModelScope.launch {
                  val result: Boolean = repository.trackCommand(track, 2)
//                  toast.postValue(if (result) "插播成功" else "插播失敗")
            }
      }

      fun onPlaylistInsertTrack(track: Track) {
            viewModelScope.launch {
                  viewModelScope.launch {
                        repository.playlistInsert(track)
                  }
            }
      }

      fun onPlaylistDeleteTrack(track: Track? = null) {
            viewModelScope.launch {
                  val mTrack: Track = track ?: (selectedTrack.value ?: return@launch)
                  repository.playlistDel(mTrack)
            }
      }


      fun onPlayListOrderTrack(track: Track? = null) {
            viewModelScope.launch {
                  val mTrack: Track = track ?: (mSelectedTrack ?: return@launch)
                  val result: Boolean = repository.trackCommand(mTrack, 1)
                  toast.postValue(if (result) "點播成功" else "點播失敗")
            }
      }

      fun onPlayListMoveTrackToTop(track: Track? = null) {
            viewModelScope.launch {
                  val mTrack: Track = track ?: (mSelectedTrack ?: return@launch)
                  repository.playlistInsert(mTrack)
            }
      }
}