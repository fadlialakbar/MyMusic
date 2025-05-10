package com.example.mymusicku.data.model
import com.google.gson.annotations.SerializedName

data class Track (
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val title: String?,
    @SerializedName("artistName") val artist: String?,
    @SerializedName("collectionName") val album: String?,
    @SerializedName("artworkUrl100") val artworkUrl: String?,
    @SerializedName("previewUrl") val previewUrl: String?,
    @SerializedName("trackTimeMillis") val duration: String?,
    @SerializedName("primaryGenreName") val genre: String?,

){
    fun isValid(): Boolean {
        return !previewUrl.isNullOrEmpty() && !title.isNullOrEmpty()
    }
}