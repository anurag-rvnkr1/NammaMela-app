package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.nammamela.R
import com.example.nammamela.data.ShowTimingEntity
import com.example.nammamela.databinding.FragmentDramaDetailsBinding
import com.example.nammamela.viewmodel.DramaDetailsViewModel
import com.google.android.material.chip.Chip

class DramaDetailsFragment : Fragment() {

    private var _binding: FragmentDramaDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DramaDetailsViewModel by viewModels()
    private var isButtonVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDramaDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dramaId = arguments?.getInt("dramaId") ?: return
        viewModel.setDramaId(dramaId)

        binding.btnBookNow.alpha = 0f
        binding.btnBookNow.visibility = View.GONE

        setupAdapters()
        setupObservers()
        setupListeners()
        setupScrollListener()
    }

    private fun setupAdapters() {
        binding.rvCastPreview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.drama.observe(viewLifecycleOwner) { drama ->
            drama?.let {
                binding.tvDramaTitle.text = it.title
                binding.tvDramaDuration.text = it.duration
                binding.tvDramaTiming.text = it.timing
                binding.tvDramaDescription.text = it.description
                binding.tvVenueName.text = it.venueName
                binding.tvVenueAddress.text = "${it.venueAddress}, ${it.venueCity}"
                
                Glide.with(this)
                    .load(it.posterUrl)
                    .into(binding.ivDramaPoster)
            }
        }

        viewModel.cast.observe(viewLifecycleOwner) { cast ->
            binding.rvCastPreview.adapter = CastAdapter(cast)
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            val adapter = CommentAdapter()
            binding.rvComments.adapter = adapter
            adapter.submitList(comments)
        }

        viewModel.showTimings.observe(viewLifecycleOwner) { timings ->
            setupDateChips(timings)
            updateBookButtonState()
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            date?.let { 
                setupTimingChips(it)
                updateBookButtonState()
            }
        }
        
        viewModel.selectedTiming.observe(viewLifecycleOwner) {
            updateBookButtonState()
        }
        
        // Check login status for comments
        if (!viewModel.isLoggedIn()) {
            binding.layoutCommentInput.visibility = View.GONE
        }
    }

    private fun setupDateChips(timings: List<ShowTimingEntity>) {
        binding.cgDates.removeAllViews()
        val uniqueDates = timings.map { it.date }.distinct()
        uniqueDates.forEach { date ->
            val chip = Chip(requireContext()).apply {
                text = date
                isCheckable = true
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
                setChipStrokeWidth(0f)
                setRippleColorResource(R.color.glass_white)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.selectDate(date)
                    updateBookButtonState()
                }
            }
            binding.cgDates.addView(chip)
        }
    }

    private fun setupTimingChips(date: String) {
        binding.cgTimings.removeAllViews()
        val timingsForDate = viewModel.showTimings.value?.filter { it.date == date } ?: emptyList()
        timingsForDate.forEach { timing ->
            val chip = Chip(requireContext()).apply {
                text = timing.time
                isCheckable = true
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
                setChipStrokeWidth(0f)
                setRippleColorResource(R.color.glass_white)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.selectTiming(timing)
                    updateBookButtonState()
                }
            }
            binding.cgTimings.addView(chip)
        }
    }

    private fun updateBookButtonState() {
        val isValid = viewModel.selectedDate.value != null && viewModel.selectedTiming.value != null
        binding.btnBookNow.isEnabled = isValid
        binding.btnBookNow.alpha = if (isValid) 1.0f else 0.6f
    }

    private fun setupScrollListener() {
        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            // Show button when user scrolls past description
            if (binding.tvDramaDescription.height > 0) {
                val descriptionBottom = binding.tvDramaDescription.bottom
                if (scrollY > descriptionBottom && !isButtonVisible) {
                    isButtonVisible = true
                    binding.btnBookNow.visibility = View.VISIBLE
                    binding.btnBookNow.animate().alpha(1f).setDuration(500).start()
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnPostComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.postComment(text)
                binding.etComment.text?.clear()
                Toast.makeText(requireContext(), "Comment posted", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBookNow.setOnClickListener {
            val selectedTiming = viewModel.selectedTiming.value
            if (selectedTiming != null) {
                val bundle = Bundle().apply {
                    putInt("dramaId", selectedTiming.dramaId)
                    putString("dramaTitle", binding.tvDramaTitle.text.toString())
                    putInt("showTimingId", selectedTiming.id)
                }
                findNavController().navigate(R.id.action_dramaDetailsFragment_to_seatsFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Please select date and time", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
