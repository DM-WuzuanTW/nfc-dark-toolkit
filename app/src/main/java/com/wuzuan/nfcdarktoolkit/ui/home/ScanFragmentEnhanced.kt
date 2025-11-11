package com.wuzuan.nfcdarktoolkit.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.MainActivity
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.nfc.TagOperations
import com.wuzuan.nfcdarktoolkit.utils.TagInfoHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 增強版掃描 Fragment（參考 NFC Tools）
 */
@AndroidEntryPoint
class ScanFragmentEnhanced : Fragment() {
    
    @Inject
    lateinit var tagOperations: TagOperations
    
    @Inject
    lateinit var cloneHelper: CloneHelper
    
    private val viewModel: ScanViewModel by viewModels()
    
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardTagInfo: LinearLayout
    private lateinit var cardNdefContent: LinearLayout
    private lateinit var layoutActions: LinearLayout
    private lateinit var tvActionsLabel: TextView
    
    private lateinit var tvManufacturer: TextView
    private lateinit var tvTagId: TextView
    private lateinit var tvTagType: TextView
    private lateinit var tvTechList: TextView
    private lateinit var tvWritable: TextView
    private lateinit var tvSize: TextView
    private lateinit var tvNdefContent: TextView
    
    private lateinit var btnCopyContent: Button
    private lateinit var btnCloneTag: Button
    private lateinit var btnFormatTag: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_enhanced, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupObservers()
        setupNfcListener()
        setupActionButtons()
    }
    
    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tv_status_scan)
        progressBar = view.findViewById(R.id.progress_scan)
        cardTagInfo = view.findViewById(R.id.card_tag_info_scan)
        cardNdefContent = view.findViewById(R.id.card_ndef_content)
        layoutActions = view.findViewById(R.id.layout_actions_scan)
        tvActionsLabel = view.findViewById(R.id.tv_actions_label)
        
        tvManufacturer = view.findViewById(R.id.tv_manufacturer)
        tvTagId = view.findViewById(R.id.tv_tag_id_scan)
        tvTagType = view.findViewById(R.id.tv_tag_type_scan)
        tvTechList = view.findViewById(R.id.tv_tech_list_scan)
        tvWritable = view.findViewById(R.id.tv_writable_scan)
        tvSize = view.findViewById(R.id.tv_size_scan)
        tvNdefContent = view.findViewById(R.id.tv_ndef_content_scan)
        
        btnCopyContent = view.findViewById(R.id.btn_copy_content)
        btnCloneTag = view.findViewById(R.id.btn_clone_tag)
        btnFormatTag = view.findViewById(R.id.btn_format_tag)
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
                    // 如果正在等待複製目標標籤
                    if (cloneHelper.isWaitingForTarget()) {
                        handleCloneTarget(tag)
                    } else {
                        viewModel.onTagDetected(tag)
                    }
                }
            }
        }
    }
    
    private fun setupActionButtons() {
        btnCopyContent.setOnClickListener {
            copyContentToClipboard()
        }
        
        btnCloneTag.setOnClickListener {
            startCloning()
        }
        
        btnFormatTag.setOnClickListener {
            showFormatConfirmDialog()
        }
    }
    
    private fun updateUI(state: ScanUiState) {
        when (state) {
            is ScanUiState.Idle -> {
                progressBar.visibility = View.GONE
                tvStatus.text = getString(R.string.nfc_ready)
                cardTagInfo.visibility = View.GONE
                cardNdefContent.visibility = View.GONE
                layoutActions.visibility = View.GONE
                tvActionsLabel.visibility = View.GONE
            }
            is ScanUiState.Reading -> {
                progressBar.visibility = View.VISIBLE
                tvStatus.text = getString(R.string.nfc_reading)
                cardTagInfo.visibility = View.GONE
                cardNdefContent.visibility = View.GONE
                layoutActions.visibility = View.GONE
            }
            is ScanUiState.Success -> {
                progressBar.visibility = View.GONE
                tvStatus.text = getString(R.string.scan_tag_detected)
                cardTagInfo.visibility = View.VISIBLE
                layoutActions.visibility = View.VISIBLE
                tvActionsLabel.visibility = View.VISIBLE
                
                val tagInfo = state.tagInfo
                
                // 顯示標籤資訊（參考 NFC Tools）
                tvManufacturer.text = state.rawTag?.let { TagInfoHelper.getManufacturer(it) } ?: "未知"
                tvTagId.text = tagInfo.id
                tvTagType.text = TagInfoHelper.getDetailedTagModel(state.rawTag!!, tagInfo.type)
                tvTechList.text = tagInfo.techList.joinToString(", ")
                tvWritable.text = if (tagInfo.isWritable) 
                    "✓ ${getString(R.string.scan_tag_writable)}" 
                else 
                    "✗ ${getString(R.string.scan_tag_readonly)}"
                tvSize.text = TagInfoHelper.getCapacityDescription(tagInfo.maxSize)
                
                // 顯示 NDEF 內容
                if (tagInfo.ndefRecords.isNotEmpty()) {
                    cardNdefContent.visibility = View.VISIBLE
                    tvNdefContent.text = tagInfo.ndefRecords.joinToString("\n\n") { record ->
                        "類型: ${record.recordType}\n內容: ${record.payload}"
                    }
                } else {
                    cardNdefContent.visibility = View.GONE
                }
            }
            is ScanUiState.Error -> {
                progressBar.visibility = View.GONE
                tvStatus.text = "錯誤: ${state.message}"
                cardTagInfo.visibility = View.GONE
                cardNdefContent.visibility = View.GONE
                layoutActions.visibility = View.GONE
                
                Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun copyContentToClipboard() {
        val state = viewModel.uiState.value
        if (state !is ScanUiState.Success) return
        
        val content = buildString {
            appendLine("Tag ID: ${state.tagInfo.id}")
            appendLine("類型: ${state.tagInfo.type.name}")
            appendLine("技術: ${state.tagInfo.techList.joinToString(", ")}")
            appendLine()
            state.tagInfo.ndefRecords.forEach { record ->
                appendLine("${record.recordType}: ${record.payload}")
            }
        }
        
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NFC Tag Info", content))
        
        Snackbar.make(requireView(), "已複製到剪貼簿", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun startCloning() {
        val state = viewModel.uiState.value
        if (state !is ScanUiState.Success || state.rawTag == null) return
        
        val result = cloneHelper.startCloning(state.rawTag)
        
        if (result.isSuccess) {
            Snackbar.make(
                requireView(),
                result.getOrNull() ?: "請靠近目標標籤",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("取消") {
                cloneHelper.cancelCloning()
            }.show()
        } else {
            Snackbar.make(
                requireView(),
                result.exceptionOrNull()?.message ?: "讀取失敗",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun handleCloneTarget(tag: android.nfc.Tag) {
        val result = cloneHelper.completeCloning(tag)
        
        if (result.isSuccess) {
            Snackbar.make(
                requireView(),
                "標籤複製成功！",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            Snackbar.make(
                requireView(),
                result.exceptionOrNull()?.message ?: "複製失敗",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showFormatConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.tag_op_format))
            .setMessage(getString(R.string.tag_op_format_warning))
            .setPositiveButton("格式化") { _, _ ->
                formatTag()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun formatTag() {
        val state = viewModel.uiState.value
        if (state !is ScanUiState.Success || state.rawTag == null) return
        
        lifecycleScope.launch {
            val result = tagOperations.formatTag(state.rawTag)
            
            if (result.isSuccess) {
                Snackbar.make(requireView(), "格式化成功", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(
                    requireView(),
                    result.exceptionOrNull()?.message ?: "格式化失敗",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}

