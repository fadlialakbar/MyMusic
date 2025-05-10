package com.example.mymusicku.data.repository

import com.example.mymusicku.data.model.Track
import com.example.mymusicku.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class MusicRepository {
    private  val apiServiceG = RetrofitClient.apiS

    fun getTrack(query: String) : Flow <List<Track>> = flow {
        try {
            val response = apiServiceG.searchMusic(query)
            val dataResValid = response.result.filter {
                it.isValid()
            }
            emit(dataResValid)
        }
        catch (e: Exception){
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}











