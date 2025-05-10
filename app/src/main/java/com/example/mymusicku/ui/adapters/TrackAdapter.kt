package com.example.mymusicku.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusicku.R
import com.example.mymusicku.data.model.Track

class TrackAdapter(private val onTrackClick: (Track) -> Unit) : 
    ListAdapter<Track, TrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivArtwork: ImageView = itemView.findViewById(R.id.ivArtwork)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)
        private val tvAlbum: TextView = itemView.findViewById(R.id.tvAlbum)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTrackClick(getItem(position))
                }
            }
        }

        fun bind(track: Track) {
            tvTitle.text = track.title ?: "Unknown Title"
            tvArtist.text = track.artist ?: "Unknown Artist"
            tvAlbum.text = track.album ?: "Unknown Album"

            track.artworkUrl?.let { url ->
                Glide.with(ivArtwork.context)
                    .load(url.replace("100x100", "300x300"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivArtwork)
            } ?: run {
                ivArtwork.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.trackId == newItem.trackId
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
} 