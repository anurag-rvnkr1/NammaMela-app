package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nammamela.R
import com.example.nammamela.databinding.FragmentProfileBinding
import com.example.nammamela.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val adapter = BookingHistoryAdapter { booking ->
            val bundle = Bundle().apply {
                putInt("bookingId", booking.id)
            }
            findNavController().navigate(R.id.action_profileFragment_to_bookingDetailsFragment, bundle)
        }
        binding.rvBookingHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookingHistory.adapter = adapter

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileName.text = user.name
                binding.tvProfileEmail.text = user.email
                binding.tvLoginType.text = "Login Type: ${user.loginType}"
            }
        }

        viewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            adapter.submitList(bookings.sortedByDescending { it.dateTime })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
