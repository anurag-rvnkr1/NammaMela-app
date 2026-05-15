package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nammamela.R
import com.example.nammamela.databinding.FragmentSeatsBinding
import com.example.nammamela.viewmodel.SeatViewModel
import com.google.android.material.snackbar.Snackbar

class SeatsFragment : Fragment() {

    private var _binding: FragmentSeatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SeatViewModel by viewModels()
    private lateinit var adapter: SeatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.releaseUserLocks()
                isEnabled = false
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dramaId = arguments?.getInt("dramaId") ?: -1
        val dramaTitle = arguments?.getString("dramaTitle") ?: "Drama"
        val showTimingId = arguments?.getInt("showTimingId") ?: -1
        
        if (showTimingId != -1) {
            viewModel.setShowTimingId(showTimingId)
        }

        viewModel.showTiming.observe(viewLifecycleOwner) { timing ->
            timing?.let {
                binding.tvDramaNameHeader.text = "$dramaTitle | ${it.date} | ${it.time}"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingLayout.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            setButtonsEnabled(!isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showSnackbar(error, isError = true)
                viewModel.clearError()
            }
        }

        adapter = SeatAdapter(viewModel.getUserId()) { seat, isSelecting -> 
            if (isSelecting) {
                viewModel.tryLockSeat(seat.id) { success, message ->
                    if (success) {
                        adapter.toggleSelection(seat.id)
                        updateBookingInfo()
                    }
                    // Error is already handled by viewModel observer
                }
            } else {
                viewModel.unlockSeat(seat.id)
                adapter.toggleSelection(seat.id)
                updateBookingInfo()
            }
        }

        binding.rvSeats.layoutManager = GridLayoutManager(requireContext(), 8)
        binding.rvSeats.adapter = adapter

        viewModel.seats.observe(viewLifecycleOwner) { seats ->
            adapter.submitList(seats)
        }

        viewModel.locks.observe(viewLifecycleOwner) { locks ->
            adapter.updateLocks(locks)
        }

        binding.btnProceed.setOnClickListener {
            val selectedSeats = adapter.getSelectedSeats()
            if (selectedSeats.isEmpty()) {
                showSnackbar("Please select at least one seat", isWarning = true)
            } else {
                val selectedIds = selectedSeats.map { it.id }.toIntArray()
                val bundle = Bundle().apply {
                    putInt("dramaId", dramaId)
                    putIntArray("selectedSeatIds", selectedIds)
                    putInt("showTimingId", showTimingId)
                }
                findNavController().navigate(R.id.action_seatsFragment_to_bookingFragment, bundle)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnProceed.isEnabled = enabled
        binding.btnProceed.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun showSnackbar(message: String, isError: Boolean = false, isWarning: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        when {
            isError -> snackbar.setBackgroundTint(requireContext().getColor(R.color.theatre_red))
            isWarning -> {
                snackbar.setBackgroundTint(requireContext().getColor(R.color.accent_gold))
                snackbar.setTextColor(requireContext().getColor(R.color.background_dark))
            }
            else -> snackbar.setBackgroundTint(requireContext().getColor(R.color.primary))
        }
        snackbar.show()
    }

    private fun updateBookingInfo() {
        val selected = adapter.getSelectedSeats()
        binding.tvSelectedSeatsCount.text = "${selected.size} Seats Selected"
        binding.tvTotalPrice.text = "₹ ${selected.size * 250}.00"
    }

    override fun onDestroyView() {
        // Only release if we're not moving forward to booking
        // Actually, if we're moving to booking, we want to KEEP the locks.
        // But if we're going BACK, we should release them.
        // Simplified: The requirement says "On back/cancel: Release all locks immediately".
        // Navigating forward is not "cancel".
        super.onDestroyView()
        _binding = null
    }

    // We can use a custom back press or just rely on expiry if not explicitly handled.
    // For now, let's just make sure we don't release when moving forward.
}
