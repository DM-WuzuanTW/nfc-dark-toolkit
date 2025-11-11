package com.wuzuan.nfcdarktoolkit.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.databinding.ItemHistoryBinding
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import java.text.SimpleDateFormat
import java.util.*

/**
 * 歷史記錄 Adapter
 */
class HistoryAdapter(
    private val onItemClick: (HistoryRecord) -> Unit,
    private val onItemLongClick: (HistoryRecord) -> Boolean
) : ListAdapter<HistoryRecord, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                } else {
                    false
                }
            }
        }
        
        fun bind(record: HistoryRecord) {
            binding.apply {
                tvTitle.text = record.title
                tvDescription.text = record.description
                tvTime.text = formatTimestamp(record.timestamp)
                tvTagId.text = record.tagId ?: "未知"
                
                // 設定操作類型圖示和顏色
                when (record.actionType) {
                    ActionType.READ -> {
                        ivIcon.setImageResource(R.drawable.ic_scan)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_info))
                    }
                    ActionType.WRITE -> {
                        ivIcon.setImageResource(R.drawable.ic_write)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_success))
                    }
                    ActionType.FORMAT -> {
                        ivIcon.setImageResource(R.drawable.ic_settings)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_warning))
                    }
                    ActionType.LOCK -> {
                        ivIcon.setImageResource(R.drawable.ic_settings)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_error))
                    }
                    ActionType.CLONE -> {
                        ivIcon.setImageResource(R.drawable.ic_scan)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_secondary))
                    }
                    ActionType.EMULATE -> {
                        ivIcon.setImageResource(R.drawable.ic_emulate)
                        ivIcon.setColorFilter(itemView.context.getColor(R.color.color_primary))
                    }
                }
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryRecord>() {
        override fun areItemsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem == newItem
        }
    }
}

