package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nammamela.R
import com.example.nammamela.data.AppDatabase
import com.example.nammamela.data.AppRepository
import com.example.nammamela.databinding.FragmentBookingBinding
import com.example.nammamela.viewmodel.BookingViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.nammamela.data.SessionManager
import com.example.nammamela.viewmodel.AuthViewModel

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()
        
        authViewModel.checkSession()
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.etName.setText(user.name)
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

        val dramaId = arguments?.getInt("dramaId") ?: -1
        val selectedSeatIds = arguments?.getIntArray("selectedSeatIds") ?: intArrayOf()
        val showTimingId = arguments?.getInt("showTimingId") ?: -1

        lifecycleScope.launch {
            val timing = viewModel.getShowTimingById(showTimingId)
            val drama = viewModel.getDramaById(dramaId)
            timing?.let { t ->
                drama?.let { d ->
                    binding.tvShowInfo.text = "${d.title}\n${t.date} | ${t.time}\n${d.venueName}"
                    binding.tvShowInfo.visibility = View.VISIBLE
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Name is required"
                return@setOnClickListener
            }
            binding.tilName.error = null

            if (phone.isEmpty() || phone.length < 10) {
                binding.tilPhone.error = "Valid phone number is required"
                return@setOnClickListener
            }
            binding.tilPhone.error = null

            lifecycleScope.launch {
                val repository = AppRepository(AppDatabase.getDatabase(requireContext()).appDao())
                val allSeatsForShow = repository.getSeatsByShowTiming(showTimingId).first()
                val selectedSeats = allSeatsForShow.filter { selectedSeatIds.contains(it.id) }

                val drama = viewModel.getDramaById(dramaId)
                val dramaName = drama?.title ?: "Drama"

                viewModel.confirmBooking(userId, dramaId, dramaName, showTimingId, name, phone, selectedSeats) {
                    showConfirmationDialog()
                }
            }
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Booking Confirmed")
            .setMessage("Your seats have been successfully reserved!")
            .setPositiveButton("OK") { _, _ ->
                findNavController().navigate(R.id.homeFragment)
            }
            .setCancelable(false)
            .show()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnConfirm.isEnabled = enabled
        binding.btnConfirm.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        if (isError) {
            snackbar.setBackgroundTint(requireContext().getColor(R.color.theatre_red))
        } else {
            snackbar.setBackgroundTint(requireContext().getColor(R.color.accent_gold))
            snackbar.setTextColor(requireContext().getColor(R.color.background_dark))
        }
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
