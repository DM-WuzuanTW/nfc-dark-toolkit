package com.wuzuan.nfcdarktoolkit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragmentSimple : Fragment() {
    
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_simple, container, false)
        
        try {
            recyclerView = view.findViewById(R.id.recycler_view_simple)
            tvEmpty = view.findViewById(R.id.tv_empty_simple)
            
            setupRecyclerView()
            observeData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
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
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragmentSimple.adapter
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyList.collect { history ->
                    adapter.submitList(history)
                    
                    if (history.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
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
                appendLine()
                append(record.description)
            })
            .setPositiveButton("確定", null)
            .show()
    }
    
    private fun showDeleteConfirmDialog(record: HistoryRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("刪除記錄")
            .setMessage("確定要刪除這筆記錄嗎？")
            .setPositiveButton("刪除") { _, _ ->
                viewModel.deleteHistory(record)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}

