package com.example.nammamela.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nammamela.databinding.FragmentFanWallBinding
import com.example.nammamela.viewmodel.DramaViewModel

class FanWallFragment : Fragment() {

    private var _binding: FragmentFanWallBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DramaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFanWallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val adapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter

        viewModel.allComments.observe(viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
            if (comments.isNotEmpty()) {
                binding.rvComments.scrollToPosition(0)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString()
            if (text.isNotBlank()) {
                viewModel.addComment(text)
                binding.etComment.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
