package com.wuzuan.nfcdarktoolkit.ui.emulate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmulateFragmentSimple : Fragment() {
    
    private val viewModel: EmulateViewModel by viewModels()
    
    private lateinit var tvStatus: TextView
    private lateinit var etAid: EditText
    private lateinit var etResponse: EditText
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emulate_simple, container, false)
        
        try {
            tvStatus = view.findViewById(R.id.tv_status_emulate)
            etAid = view.findViewById(R.id.et_aid_simple)
            etResponse = view.findViewById(R.id.et_response_simple)
            btnStart = view.findViewById(R.id.btn_start_simple)
            btnStop = view.findViewById(R.id.btn_stop_simple)
            
            setupButtons()
            observeState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun setupButtons() {
        btnStart.setOnClickListener {
            val aid = etAid.text.toString()
            val response = etResponse.text.toString()
            
            viewModel.setAid(aid)
            viewModel.setResponseData(response)
            viewModel.startEmulation()
        }
        
        btnStop.setOnClickListener {
            viewModel.stopEmulation()
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EmulateUiState.Inactive -> {
                            tvStatus.text = getString(R.string.emulate_inactive)
                            tvStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
                            btnStart.isEnabled = true
                            btnStop.isEnabled = false
                        }
                        is EmulateUiState.Active -> {
                            tvStatus.text = getString(R.string.emulate_active)
                            tvStatus.setTextColor(requireContext().getColor(R.color.color_success))
                            btnStart.isEnabled = false
                            btnStop.isEnabled = true
                            
                            Snackbar.make(
                                requireView(),
                                "HCE 模擬已啟動",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        is EmulateUiState.Error -> {
                            tvStatus.text = getString(R.string.emulate_inactive)
                            tvStatus.setTextColor(requireContext().getColor(R.color.color_error))
                            btnStart.isEnabled = true
                            btnStop.isEnabled = false
                            
                            Snackbar.make(
                                requireView(),
                                state.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}

