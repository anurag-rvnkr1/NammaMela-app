package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nammamela.R
import com.example.nammamela.databinding.FragmentHomeBinding
import com.example.nammamela.viewmodel.DramaViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DramaViewModel by viewModels()
    private lateinit var recommendedAdapter: RecommendedDramaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DramaAdapter(
            onBookClick = { drama ->
                val bundle = Bundle().apply {
                    putInt("dramaId", drama.id)
                    putString("dramaTitle", drama.title)
                }
                findNavController().navigate(R.id.action_homeFragment_to_seatsFragment, bundle)
            },
            onViewDetailsClick = { drama ->
                val bundle = Bundle().apply {
                    putInt("dramaId", drama.id)
                }
                findNavController().navigate(R.id.action_homeFragment_to_dramaDetailsFragment, bundle)
            }
        )

        binding.rvDramas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDramas.adapter = adapter

        recommendedAdapter = RecommendedDramaAdapter(
            onBookClick = { drama ->
                val bundle = Bundle().apply {
                    putInt("dramaId", drama.id)
                    putString("dramaTitle", drama.title)
                }
                findNavController().navigate(R.id.action_homeFragment_to_seatsFragment, bundle)
            },
            onViewDetailsClick = { drama ->
                val bundle = Bundle().apply {
                    putInt("dramaId", drama.id)
                }
                findNavController().navigate(R.id.action_homeFragment_to_dramaDetailsFragment, bundle)
            }
        )
        binding.rvRecommended.adapter = recommendedAdapter

        viewModel.allDramas.observe(viewLifecycleOwner) { dramas ->
            adapter.submitList(dramas)
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            if (recommendations.isNotEmpty()) {
                binding.tvRecommendedTitle.visibility = View.VISIBLE
                binding.rvRecommended.visibility = View.VISIBLE
                recommendedAdapter.submitList(recommendations)
            } else {
                binding.tvRecommendedTitle.visibility = View.GONE
                binding.rvRecommended.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.shimmerContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvDramas.visibility = if (isLoading) View.GONE else View.VISIBLE
            
            binding.shimmerRecommended.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) binding.rvRecommended.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showSnackbar(error, isError = true)
                viewModel.clearError()
            }
        }
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

    override fun onResume() {
        super.onResume()
        viewModel.loadRecommendations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
