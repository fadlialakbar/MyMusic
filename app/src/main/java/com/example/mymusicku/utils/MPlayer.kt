package com.example.mymusicku.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper



class MPlayer {
    private  var mediaPlayer: MediaPlayer? = null
    private  var progressHandler: Handler? = null
    private  var progressRunnable: Runnable? = null

    private var onProgressUpdateListener : ((Int,Int) ->Unit)? = null
    private var onCompleteListener: (()-> Unit)? = null

    fun play (urlMusic : String ){

        dispose()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(urlMusic)
                prepareAsync()

                setOnPreparedListener{
                    start()
                    startProgressTracking()
                }

                setCompleted {
                    onCompleteListener?.invoke()
                }


            }

        }catch (e:Exception){
            e.printStackTrace()
            dispose()
        }
    }

    fun pause (){
        mediaPlayer?.let {
            if(it.isPlaying){
                it.pause()
                stopProgressTracking()
            }
        }
    }


    fun resume (){
        mediaPlayer?.let {
            if(!it.isPlaying){
                it.start()
                startProgressTracking()
            }
        }
    }


    fun seekTo(p: Int){
        mediaPlayer?.seekTo(p)
    }

    fun dispose (){
        stopProgressTracking()
        mediaPlayer?.apply {
            if(isPlaying){
                stop()
            }
            dispose()
        }
        mediaPlayer = null
    }

    fun setOnProgressUpdateListener (listener: (progress: Int, duration: Int) -> Unit){
        onProgressUpdateListener = listener
    }

    fun setCompleted (listener: ()-> Unit){
        onCompleteListener = listener
    }

    private fun startProgressTracking(){
        progressHandler = Handler(Looper.getMainLooper())
        progressRunnable = Runnable {
            mediaPlayer?.let {
                 val cp = it.currentPosition
                val duration = it.duration
                onProgressUpdateListener?.invoke(cp, duration)
                progressHandler?.postDelayed(progressRunnable!!, 1000)
            }
        }
        progressHandler?.post(progressRunnable!!)
    }

    private  fun stopProgressTracking (){
        progressRunnable?.let{
            progressHandler?.removeCallbacks(it)
        }
        progressHandler = null
        progressRunnable = null
    }
}