package com.wuzuan.nfcdarktoolkit.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 歷史記錄 ViewModel
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filterType = MutableStateFlow<ActionType?>(null)
    val filterType: StateFlow<ActionType?> = _filterType.asStateFlow()
    
    val historyList: StateFlow<List<HistoryRecord>> = combine(
        historyRepository.getAllHistory(),
        _searchQuery,
        _filterType
    ) { allHistory, query, filter ->
        var filtered = allHistory
        
        // 按類型篩選
        if (filter != null) {
            filtered = filtered.filter { it.actionType == filter }
        }
        
        // 按搜尋關鍵字篩選
        if (query.isNotBlank()) {
            filtered = filtered.filter { record ->
                record.tagId?.contains(query, ignoreCase = true) == true ||
                record.title.contains(query, ignoreCase = true) ||
                record.description.contains(query, ignoreCase = true)
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setFilterType(type: ActionType?) {
        _filterType.value = type
    }
    
    fun deleteHistory(record: HistoryRecord) {
        viewModelScope.launch {
            historyRepository.deleteHistory(record)
        }
    }
    
    fun deleteAllHistory() {
        viewModelScope.launch {
            historyRepository.deleteAllHistory()
        }
    }
    
    fun exportHistoryAsJson(): String {
        val history = historyList.value
        return buildString {
            append("[\n")
            history.forEachIndexed { index, record ->
                append("  {\n")
                append("    \"id\": ${record.id},\n")
                append("    \"tagId\": \"${record.tagId}\",\n")
                append("    \"tagType\": \"${record.tagType}\",\n")
                append("    \"actionType\": \"${record.actionType}\",\n")
                append("    \"title\": \"${record.title}\",\n")
                append("    \"description\": \"${record.description.replace("\"", "\\\"")}\",\n")
                append("    \"timestamp\": ${record.timestamp}\n")
                append("  }")
                if (index < history.size - 1) append(",")
                append("\n")
            }
            append("]\n")
        }
    }
    
    fun exportHistoryAsCsv(): String {
        val history = historyList.value
        return buildString {
            // 標題行
            appendLine("ID,Tag ID,Tag Type,Action,Title,Description,Timestamp")
            
            // 資料行
            history.forEach { record ->
                append("${record.id},")
                append("\"${record.tagId ?: ""}\",")
                append("\"${record.tagType ?: ""}\",")
                append("\"${record.actionType}\",")
                append("\"${record.title}\",")
                append("\"${record.description.replace("\"", "\"\"")}\",")
                appendLine(record.timestamp)
            }
        }
    }
}

