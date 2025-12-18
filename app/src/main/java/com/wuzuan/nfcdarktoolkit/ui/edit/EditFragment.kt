package com.wuzuan.nfcdarktoolkit.ui.edit

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 讀取編輯頁面
 * 讀取標籤內容後可直接編輯並寫回
 */
@AndroidEntryPoint
class EditFragment : Fragment() {
    
    private val viewModel: EditViewModel by viewModels()
    
    private lateinit var tvStatus: TextView
    private lateinit var layoutContent: LinearLayout
    private lateinit var etContent: EditText
    private lateinit var tvOriginal: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupButtons()
        setupNfcListener()
        observeState()
    }
    
    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tv_status_edit)
        layoutContent = view.findViewById(R.id.layout_edit_content)
        etContent = view.findViewById(R.id.et_edit_content)
        tvOriginal = view.findViewById(R.id.tv_original_content)
        btnSave = view.findViewById(R.id.btn_save_edit)
        btnCancel = view.findViewById(R.id.btn_cancel_edit)
        progressBar = view.findViewById(R.id.progress_edit)
    }
    
    private fun setupButtons() {
        btnSave.setOnClickListener {
            val newContent = etContent.text.toString()
            if (newContent.isNotBlank()) {
                viewModel.prepareToWrite(newContent)
                Snackbar.make(requireView(), "請靠近標籤寫入修改後的內容", Snackbar.LENGTH_LONG).show()
            }
        }
        
        btnCancel.setOnClickListener {
            viewModel.reset()
            etContent.text?.clear()
        }
    }
    
    private fun setupNfcListener() {
        val mainActivity = activity as? MainActivity ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivity.nfcTagFlow.collect { tag ->
                    viewModel.handleTag(tag)
                }
            }
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editState.collect { state ->
                    when (state) {
                        is EditState.Idle -> {
                            tvStatus.text = "請靠近標籤讀取內容"
                            layoutContent.visibility = View.GONE
                            progressBar.visibility = View.GONE
                        }
                        is EditState.Reading -> {
                            tvStatus.text = "讀取中..."
                            progressBar.visibility = View.VISIBLE
                            layoutContent.visibility = View.GONE
                        }
                        is EditState.Loaded -> {
                            tvStatus.text = "已讀取標籤內容，可以編輯"
                            progressBar.visibility = View.GONE
                            layoutContent.visibility = View.VISIBLE
                            
                            tvOriginal.text = "原始內容：\n${state.originalContent}"
                            etContent.setText(state.originalContent)
                        }
                        is EditState.Writing -> {
                            tvStatus.text = "寫入中..."
                            progressBar.visibility = View.VISIBLE
                        }
                        is EditState.Success -> {
                            tvStatus.text = state.message
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.reset()
                        }
                        is EditState.Error -> {
                            tvStatus.text = "錯誤：${state.message}"
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(requireContext().getColor(R.color.color_error))
                                .show()
                        }
                    }
                }
            }
        }
    }
}

