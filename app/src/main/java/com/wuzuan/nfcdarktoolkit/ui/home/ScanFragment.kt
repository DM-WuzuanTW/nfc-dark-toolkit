package com.wuzuan.nfcdarktoolkit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wuzuan.nfcdarktoolkit.MainActivity
import com.wuzuan.nfcdarktoolkit.databinding.FragmentScanBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 掃描 Fragment
 */
@AndroidEntryPoint
class ScanFragment : Fragment() {
    
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ScanViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupNfcListener()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun setupNfcListener() {
        val mainActivity = activity as? MainActivity ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivity.nfcTagFlow.collect { tag ->
                    viewModel.onTagDetected(tag)
                }
            }
        }
    }
    
    private fun updateUI(state: ScanUiState) {
        when (state) {
            is ScanUiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = "準備掃描 NFC 標籤..."
                binding.cardTagInfo.visibility = View.GONE
            }
            is ScanUiState.Reading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.text = "讀取中..."
                binding.cardTagInfo.visibility = View.GONE
            }
            is ScanUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = "讀取成功"
                binding.cardTagInfo.visibility = View.VISIBLE
                
                // 顯示標籤資訊
                binding.tvTagId.text = state.tagInfo.id
                binding.tvTagType.text = state.tagInfo.type.name
                binding.tvTechList.text = state.tagInfo.techList.joinToString(", ")
                binding.tvWritable.text = if (state.tagInfo.isWritable) "可寫入" else "唯讀"
                binding.tvSize.text = if (state.tagInfo.maxSize != null) 
                    "${state.tagInfo.currentSize ?: 0} / ${state.tagInfo.maxSize} bytes" 
                else "N/A"
                
                // 顯示 NDEF 內容
                if (state.tagInfo.ndefRecords.isNotEmpty()) {
                    binding.tvNdefContent.visibility = View.VISIBLE
                    binding.tvNdefContent.text = state.tagInfo.ndefRecords.joinToString("\n\n") { record ->
                        "類型: ${record.recordType}\n內容: ${record.payload}"
                    }
                } else {
                    binding.tvNdefContent.visibility = View.GONE
                }
            }
            is ScanUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = "錯誤: ${state.message}"
                binding.cardTagInfo.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

