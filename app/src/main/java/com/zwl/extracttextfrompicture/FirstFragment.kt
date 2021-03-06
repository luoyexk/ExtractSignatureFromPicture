package com.zwl.extracttextfrompicture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.zwl.extracttextfrompicture.databinding.FragmentFirstBinding
import kotlinx.coroutines.flow.collect

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val mViewModel by activityViewModels<HandleViewModel>()
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.srcUrl.collect { uri ->
                _binding?.ivSrc?.let { view ->
                    Glide.with(view).load(uri).into(view)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.dstUrl.collect { uri ->
                _binding?.ivResult?.let { view ->
                    Glide.with(view).load(uri).into(view)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.onProcessing.collect {
                _binding?.progressBar?.isVisible = it
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.error.collect {
                if (it.isNotEmpty()) {
                    DialogRepo().showErrorMessage(requireContext(), it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}