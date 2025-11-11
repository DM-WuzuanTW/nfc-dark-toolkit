package com.wuzuan.nfcdarktoolkit.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.databinding.FragmentHistoryBinding
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupButtons()
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onItemClick = { record ->
                showRecordDetailsDialog(record)
            },
            onItemLongClick = { record ->
                showDeleteConfirmDialog(record)
                true
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }
    
    private fun setupFilters() {
        binding.chipAll.setOnClickListener {
            viewModel.setFilterType(null)
            updateFilterChips(null)
        }
        
        binding.chipRead.setOnClickListener {
            viewModel.setFilterType(ActionType.READ)
            updateFilterChips(ActionType.READ)
        }
        
        binding.chipWrite.setOnClickListener {
            viewModel.setFilterType(ActionType.WRITE)
            updateFilterChips(ActionType.WRITE)
        }
        
        binding.chipFormat.setOnClickListener {
            viewModel.setFilterType(ActionType.FORMAT)
            updateFilterChips(ActionType.FORMAT)
        }
        
        binding.chipEmulate.setOnClickListener {
            viewModel.setFilterType(ActionType.EMULATE)
            updateFilterChips(ActionType.EMULATE)
        }
    }
    
    private fun updateFilterChips(activeType: ActionType?) {
        binding.chipAll.isChecked = activeType == null
        binding.chipRead.isChecked = activeType == ActionType.READ
        binding.chipWrite.isChecked = activeType == ActionType.WRITE
        binding.chipFormat.isChecked = activeType == ActionType.FORMAT
        binding.chipEmulate.isChecked = activeType == ActionType.EMULATE
    }
    
    private fun setupButtons() {
        binding.btnDeleteAll.setOnClickListener {
            showDeleteAllConfirmDialog()
        }
        
        binding.btnExport.setOnClickListener {
            showExportDialog()
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyList.collect { history ->
                    adapter.submitList(history)
                    
                    if (history.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    private fun showRecordDetailsDialog(record: HistoryRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(record.title)
            .setMessage(buildString {
                appendLine("Tag ID: ${record.tagId ?: "未知"}")
                appendLine("類型: ${record.tagType ?: "未知"}")
                appendLine("操作: ${record.actionType}")
                appendLine("時間: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(record.timestamp))}")
                appendLine()
                appendLine("詳細內容:")
                append(record.description)
            })
            .setPositiveButton("確定", null)
            .setNegativeButton("刪除") { _, _ ->
                viewModel.deleteHistory(record)
                Snackbar.make(binding.root, "已刪除記錄", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showDeleteConfirmDialog(record: HistoryRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("刪除記錄")
            .setMessage("確定要刪除這筆記錄嗎？")
            .setPositiveButton("刪除") { _, _ ->
                viewModel.deleteHistory(record)
                Snackbar.make(binding.root, "已刪除記錄", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showDeleteAllConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("清除所有記錄")
            .setMessage("確定要清除所有歷史記錄嗎？此操作無法復原。")
            .setPositiveButton("清除") { _, _ ->
                viewModel.deleteAllHistory()
                Snackbar.make(binding.root, "已清除所有記錄", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showExportDialog() {
        val options = arrayOf("JSON 格式", "CSV 格式")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("選擇匯出格式")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAsJson()
                    1 -> exportAsCsv()
                }
            }
            .show()
    }
    
    private fun exportAsJson() {
        try {
            val json = viewModel.exportHistoryAsJson()
            saveAndShareFile(json, "nfc_history.json", "application/json")
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "匯出失敗: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }
    
    private fun exportAsCsv() {
        try {
            val csv = viewModel.exportHistoryAsCsv()
            saveAndShareFile(csv, "nfc_history.csv", "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "匯出失敗: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }
    
    private fun saveAndShareFile(content: String, filename: String, mimeType: String) {
        val file = File(requireContext().cacheDir, filename)
        file.writeText(content)
        
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(shareIntent, "匯出歷史記錄"))
        
        Snackbar.make(binding.root, "匯出成功", Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

