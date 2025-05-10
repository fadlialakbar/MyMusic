package com.example.mymusicku.ui.viewmodel

import androidx.lifecycle.*
import com.example.mymusicku.data.model.Track
import com.example.mymusicku.data.repository.MusicRepository
import com.example.mymusicku.utils.MPlayer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class MusicViewModel () : ViewModel (){
    private  val repo = MusicRepository()
    private  val mediaPlayer =MPlayer()

    private  val _resultSearch  = MutableLiveData<List<Track>> ()
    val searchResult: LiveData<List<Track>> = _resultSearch

    private  val _isLoading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean?> = _isLoading

    private  val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    //Music variable
    private val _currentSong = MutableLiveData<Track?>()
    val currentSong: LiveData<Track?> = _currentSong

    private  val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean?> = _isPlaying

    private  val _progress = MutableLiveData<Int>()
    val progress : LiveData<Int> = _progress

    private  val _duration = MutableLiveData<Int>()
    val duration : LiveData<Int> = _duration

    private  var currentIndexPlayList: List<Track> = emptyList()
    var currentIndex : Int = -1

   init {

       _isPlaying.value = false
       _progress.value = 0
       _duration.value = 0

       mediaPlayer.setOnProgressUpdateListener { progress, duration ->
           _progress.postValue(progress)
           _duration.postValue(duration)
       }

       mediaPlayer.setCompleted {

       }

   }

    fun searchSongs (query: String){
        if (query.isEmpty()){
            _resultSearch.value = emptyList()
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            repo.getTrack(query).catch {
                e-> _error.value = e.message
                _isLoading.value = false
            }.collect{
                song -> _resultSearch.value = song
                _isLoading.value = false
                currentIndexPlayList = song
            }
        }
    }

    fun playSong (song: Track){
        val index = currentIndexPlayList.indexOf(song)
        if (index != -1){
            currentIndex = index
            _currentSong.value = song

            song.previewUrl.let{
                url ->
                mediaPlayer.play(url.toString())
                _isPlaying.value = true
            }
        }
    }

    fun PlayOrPause() {
        if  (_isPlaying.value == true){
            mediaPlayer.pause()
            _isPlaying.value = false
        }else{
            mediaPlayer.resume()
            _isPlaying.value = true
        }
    }

    fun PlayNextSong(){
        if (currentIndexPlayList.isEmpty() || currentIndex == -1)  return
       // currentIndex = if (currentIndex > 0) currentIndex - 1 else currentIndexPlayList.size -1
        currentIndex =  (currentIndex + 1) %  currentIndexPlayList.size
        val nextSong = currentIndexPlayList[currentIndex]
        playSong(nextSong)
    }

    fun PlayPreviousSong(){
        if (currentIndexPlayList.isEmpty() || currentIndex == -1)  return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else currentIndexPlayList.size -1

        val prevSong = currentIndexPlayList[currentIndex]
        playSong(prevSong)
    }

    fun seekTo (position: Int){
        mediaPlayer.seekTo(position)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.dispose()
    }


}
