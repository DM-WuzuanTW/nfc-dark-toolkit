package com.wuzuan.nfcdarktoolkit.ui.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.MainActivity
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.domain.model.WiFiSecurityType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WriteFragmentSimple : Fragment() {
    
    private val viewModel: WriteViewModel by viewModels()
    
    private lateinit var etText: EditText
    private lateinit var etUrl: EditText
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_write_simple, container, false)
        
        etText = view.findViewById(R.id.et_text_simple)
        etUrl = view.findViewById(R.id.et_url_simple)
        tvStatus = view.findViewById(R.id.tv_status_simple)
        progressBar = view.findViewById(R.id.progress_simple)
        
        val btnWriteText: Button = view.findViewById(R.id.btn_write_text)
        val btnWriteUrl: Button = view.findViewById(R.id.btn_write_url)
        
        setupButtons(btnWriteText, btnWriteUrl)
        setupObservers()
        setupNfcListener()
        
        return view
    }
    
    private fun setupButtons(btnText: Button, btnUrl: Button) {
        btnText.setOnClickListener {
            viewModel.setWriteType(WriteType.TEXT)
            Snackbar.make(requireView(), "請靠近標籤寫入文字", Snackbar.LENGTH_SHORT).show()
        }
        
        btnUrl.setOnClickListener {
            viewModel.setWriteType(WriteType.URL)
            Snackbar.make(requireView(), "請靠近標籤寫入網址", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is WriteUiState.Idle -> {
                            progressBar.visibility = View.GONE
                            tvStatus.text = "準備寫入"
                        }
                        is WriteUiState.Writing -> {
                            progressBar.visibility = View.VISIBLE
                            tvStatus.text = "寫入中..."
                        }
                        is WriteUiState.Success -> {
                            progressBar.visibility = View.GONE
                            tvStatus.text = state.message
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                        }
                        is WriteUiState.Error -> {
                            progressBar.visibility = View.GONE
                            tvStatus.text = "錯誤: ${state.message}"
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun setupNfcListener() {
        val mainActivity = activity as? MainActivity ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivity.nfcTagFlow.collect { tag ->
                    when (viewModel.writeType.value) {
                        WriteType.TEXT -> {
                            val text = etText.text.toString()
                            if (text.isNotBlank()) {
                                viewModel.writeText(tag, text)
                            }
                        }
                        WriteType.URL -> {
                            val url = etUrl.text.toString()
                            if (url.isNotBlank()) {
                                viewModel.writeUri(tag, url)
                            }
                        }
                        else -> {
                            Snackbar.make(requireView(), "此功能開發中", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

