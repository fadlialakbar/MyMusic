package com.example.mymusicku.data.model

import com.google.gson.annotations.SerializedName

data class  SearchResponse (
    @SerializedName("resultCount") val resultCount : Int,
    @SerializedName("results") val result: List<Track>
)

