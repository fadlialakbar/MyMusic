package com.example.mymusicku.ui.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicku.R
import com.example.mymusicku.data.model.Track
import com.example.mymusicku.data.network.RetrofitClient
import com.example.mymusicku.ui.adapters.TrackAdapter
import com.example.mymusicku.utils.MPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var rvSongs: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: View
    private lateinit var tvCurrentSongTitle: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    
    private lateinit var trackAdapter: TrackAdapter
    private val tracks = mutableListOf<Track>()
    private var currentTrackIndex = -1
    private var searchJob: Job? = null
    private val player = MPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var currentDuration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupRecyclerView()
        setupSearchView()
        setupPlayerControls()
        setupProgressUpdates()
    }
    
    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        rvSongs = findViewById(R.id.rvSongs)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)
        tvCurrentSongTitle = findViewById(R.id.tvCurrentSongTitle)
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
    }
    
    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter { track ->
            playSong(track)
        }
        
        rvSongs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trackAdapter
        }
    }
    
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchArtist(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (newText.isNullOrBlank()) {
                    showEmptyState()
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(500) // Debounce search
                        searchArtist(newText)
                    }
                }
                return true
            }
        })
    }
    
    private fun setupPlayerControls() {
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                resumePlayback()
            }
        }
        
        btnNext.setOnClickListener {
            playNextSong()
        }
        
        btnPrevious.setOnClickListener {
            playPreviousSong()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress)
                    updateCurrentTimeText(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupProgressUpdates() {
        player.setOnProgressUpdateListener { progress, duration ->
            runOnUiThread {
                seekBar.progress = progress
                seekBar.max = duration
                currentDuration = duration
                updateCurrentTimeText(progress)
                updateTotalTimeText(duration)
            }
        }
        
        player.setCompleted {
            runOnUiThread {
                playNextSong()
            }
        }
    }
    
    private fun searchArtist(query: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiS.searchMusic(term = query)
                tracks.clear()
                tracks.addAll(response.result.filter { it.isValid() })
                
                if (tracks.isEmpty()) {
                    showEmptyState("No songs found for \"$query\"")
                } else {
                    showResults()
                    trackAdapter.submitList(tracks)
                }
            } catch (e: Exception) {
                showEmptyState("Error: ${e.message}")
            }
        }
    }
    
    private fun playSong(track: Track) {
        currentTrackIndex = tracks.indexOf(track)
        if (currentTrackIndex != -1) {
            val url = track.previewUrl ?: return
            
            player.play(url)
            isPlaying = true
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            tvCurrentSongTitle.text = track.title
            tvCurrentArtist.text = track.artist
        }
    }
    
    private fun pausePlayback() {
        player.pause()
        isPlaying = false
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }
    
    private fun resumePlayback() {
        player.resume()
        isPlaying = true
        btnPlayPause.setImageResource(R.drawable.ic_pause)
    }
    
    private fun playNextSong() {
        if (tracks.isEmpty()) return
        
        val nextIndex = (currentTrackIndex + 1) % tracks.size
        playSong(tracks[nextIndex])
    }
    
    private fun playPreviousSong() {
        if (tracks.isEmpty()) return
        
        val prevIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else tracks.size - 1
        playSong(tracks[prevIndex])
    }
    
    private fun updateCurrentTimeText(milliseconds: Int) {
        tvCurrentTime.text = formatTime(milliseconds)
    }
    
    private fun updateTotalTimeText(milliseconds: Int) {
        tvTotalTime.text = formatTime(milliseconds)
    }
    
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    private fun showEmptyState(message: String = "Search for artists to find songs") {
        tvEmptyState.text = message
        tvEmptyState.visibility = View.VISIBLE
        rvSongs.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvSongs.visibility = View.GONE
    }
    
    private fun showResults() {
        rvSongs.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player.dispose()
        handler.removeCallbacksAndMessages(null)
    }
}