package com.example.nammamela.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammamela.data.BookingEntity
import com.example.nammamela.databinding.ItemBookingHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class BookingHistoryAdapter(private val onItemClick: (BookingEntity) -> Unit) : ListAdapter<BookingEntity, BookingHistoryAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemBookingHistoryBinding,
        private val onItemClick: (BookingEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booking: BookingEntity) {
            binding.tvDramaName.text = booking.dramaName
            binding.tvVenue.text = booking.venueName
            binding.tvShowSlot.text = "${booking.showDate} | ${booking.showTime}"
            binding.tvSeats.text = "Seats: ${booking.seatNumbers}"
            
            val sdf = SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault())
            binding.tvBookingTime.text = "Booked on: ${sdf.format(Date(booking.dateTime))}"

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<BookingEntity>() {
        override fun areItemsTheSame(oldItem: BookingEntity, newItem: BookingEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BookingEntity, newItem: BookingEntity): Boolean = oldItem == newItem
    }
}
