package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nammamela.data.AppDatabase
import com.example.nammamela.data.AppRepository
import com.example.nammamela.databinding.FragmentBookingDetailsBinding
import kotlinx.coroutines.launch

class BookingDetailsFragment : Fragment() {

    private var _binding: FragmentBookingDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookingId = arguments?.getInt("bookingId") ?: return
        
        lifecycleScope.launch {
            val repository = AppRepository(AppDatabase.getDatabase(requireContext()).appDao())
            val booking = repository.getBookingById(bookingId)
            val drama = booking?.let { repository.getDramaById(it.dramaId) }

            booking?.let { b ->
                binding.tvDramaName.text = b.dramaName
                binding.tvVenue.text = b.venueName
                binding.tvSlot.text = "${b.showDate} | ${b.showTime}"
                binding.tvSeats.text = "Seats: ${b.seatNumbers}"
                binding.tvBookingId.text = "Booking ID: #NM${b.id}"

                drama?.let { d ->
                    Glide.with(this@BookingDetailsFragment)
                        .load(d.posterUrl)
                        .centerCrop()
                        .into(binding.ivDramaBanner)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
