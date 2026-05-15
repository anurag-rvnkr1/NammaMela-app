package com.example.nammamela.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammamela.R
import com.example.nammamela.data.SeatEntity
import com.example.nammamela.data.SeatLockEntity
import com.example.nammamela.databinding.ItemSeatBinding

class SeatAdapter(
    private val userId: Int,
    private val onSeatClick: (SeatEntity, Boolean) -> Unit // Boolean: isSelecting
) : ListAdapter<SeatEntity, SeatAdapter.SeatViewHolder>(SeatDiffCallback()) {

    private val selectedSeats = mutableSetOf<Int>()
    private var locks = listOf<SeatLockEntity>()

    fun updateLocks(newLocks: List<SeatLockEntity>) {
        locks = newLocks
        notifyDataSetChanged()
    }

    fun isSelected(seatId: Int) = selectedSeats.contains(seatId)

    fun toggleSelection(seatId: Int) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId)
        } else {
            selectedSeats.add(seatId)
        }
        notifyDataSetChanged()
    }

    class SeatViewHolder(val binding: ItemSeatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val binding = ItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = getItem(position)
        val rowChar = ('A'.code + seat.row).toChar()
        holder.binding.tvSeatNumber.text = "$rowChar${seat.column + 1}"
        
        val lock = locks.find { it.seatId == seat.id }
        val isLockedByOthers = lock != null && lock.userId != userId && lock.expiryTimestamp > System.currentTimeMillis()
        val isSelected = selectedSeats.contains(seat.id)

        val drawableRes = when {
            seat.isBooked -> R.drawable.seat_booked_bg
            isLockedByOthers -> R.drawable.seat_locked_bg
            isSelected -> R.drawable.seat_selected_bg
            else -> R.drawable.seat_available_bg
        }
        holder.binding.viewSeatBg.background = ContextCompat.getDrawable(holder.itemView.context, drawableRes)
        
        holder.binding.tvSeatNumber.setTextColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.black)
            else ContextCompat.getColor(holder.itemView.context, R.color.white)
        )

        holder.itemView.isEnabled = !seat.isBooked && !isLockedByOthers
        holder.itemView.setOnClickListener {
            if (!seat.isBooked && !isLockedByOthers) {
                it.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()

                onSeatClick(seat, !isSelected)
            }
        }
    }

    fun getSelectedSeats(): List<SeatEntity> {
        return currentList.filter { selectedSeats.contains(it.id) }
    }

    class SeatDiffCallback : DiffUtil.ItemCallback<SeatEntity>() {
        override fun areItemsTheSame(oldItem: SeatEntity, newItem: SeatEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SeatEntity, newItem: SeatEntity) = oldItem == newItem
    }
}
