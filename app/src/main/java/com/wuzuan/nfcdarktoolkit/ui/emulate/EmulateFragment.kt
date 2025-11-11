package com.wuzuan.nfcdarktoolkit.ui.emulate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.databinding.FragmentEmulateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmulateFragment : Fragment() {
    
    private var _binding: FragmentEmulateBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EmulateViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmulateBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
        observeState()
    }
    
    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            val aid = binding.etAid.text.toString()
            val response = binding.etResponse.text.toString()
            
            viewModel.setAid(aid)
            viewModel.setResponseData(response)
            viewModel.startEmulation()
        }
        
        binding.btnStop.setOnClickListener {
            viewModel.stopEmulation()
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: EmulateUiState) {
        when (state) {
            is EmulateUiState.Inactive -> {
                binding.tvStatus.text = getString(R.string.emulate_inactive)
                binding.tvStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
                binding.btnStart.isEnabled = true
                binding.btnStop.isEnabled = false
            }
            is EmulateUiState.Active -> {
                binding.tvStatus.text = getString(R.string.emulate_active)
                binding.tvStatus.setTextColor(requireContext().getColor(R.color.color_success))
                binding.btnStart.isEnabled = false
                binding.btnStop.isEnabled = true
                
                Snackbar.make(
                    binding.root,
                    "HCE 模擬已啟動\nAID: ${state.aid}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            is EmulateUiState.Error -> {
                binding.tvStatus.text = getString(R.string.emulate_inactive)
                binding.tvStatus.setTextColor(requireContext().getColor(R.color.color_error))
                binding.btnStart.isEnabled = true
                binding.btnStop.isEnabled = false
                
                Snackbar.make(
                    binding.root,
                    state.message,
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.color_error))
                    .show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

