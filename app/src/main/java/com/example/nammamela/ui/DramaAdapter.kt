package com.example.nammamela.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammamela.data.DramaEntity
import com.example.nammamela.databinding.ItemDramaBinding

class DramaAdapter(
    private val onBookClick: (DramaEntity) -> Unit,
    private val onViewDetailsClick: (DramaEntity) -> Unit
) :
    ListAdapter<DramaEntity, DramaAdapter.DramaViewHolder>(DramaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DramaViewHolder {
        val binding = ItemDramaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DramaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DramaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DramaViewHolder(private val binding: ItemDramaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(drama: DramaEntity) {
            binding.tvTitle.text = drama.title
            binding.tvDuration.text = "${drama.duration} • ${drama.genre}"
            binding.tvRating.text = String.format("%.1f/5", drama.rating)
            
            Glide.with(binding.ivPoster.context)
                .load(drama.posterUrl)
                .centerCrop()
                .into(binding.ivPoster)

            binding.btnBook.setOnClickListener { 
                it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    onBookClick(drama)
                }.start()
            }
            binding.root.setOnClickListener { 
                it.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    onViewDetailsClick(drama)
                }.start()
            }
        }
    }

    class DramaDiffCallback : DiffUtil.ItemCallback<DramaEntity>() {
        override fun areItemsTheSame(oldItem: DramaEntity, newItem: DramaEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DramaEntity, newItem: DramaEntity): Boolean {
            return oldItem == newItem
        }
    }
}
