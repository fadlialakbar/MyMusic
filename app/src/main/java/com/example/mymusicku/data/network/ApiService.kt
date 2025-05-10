package com.example.mymusicku.data.network

import com.example.mymusicku.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface  ApiService {
    @GET("search")
    suspend fun searchMusic (
        @Query("term") term: String,
        @Query ("media") media: String = "music",
        @Query ("limit") limit : Int = 50
    ) : SearchResponse
}

