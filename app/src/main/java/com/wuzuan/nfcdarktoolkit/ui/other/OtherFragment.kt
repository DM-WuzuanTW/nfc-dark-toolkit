package com.wuzuan.nfcdarktoolkit.ui.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.MainActivity
import com.wuzuan.nfcdarktoolkit.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 其他操作頁面
 * 功能：copy tag, copy to infinity, erase tag, lock tag, 
 *      read memory, format memory, set password, remove password, advanced commands
 */
@AndroidEntryPoint
class OtherFragment : Fragment() {
    
    private val viewModel: OtherViewModel by viewModels()
    
    private lateinit var btnCopyTag: Button
    private lateinit var btnCopyInfinity: Button
    private lateinit var btnEraseTag: Button
    private lateinit var btnLockTag: Button
    private lateinit var btnReadMemory: Button
    private lateinit var btnFormatMemory: Button
    private lateinit var btnSetPassword: Button
    private lateinit var btnRemovePassword: Button
    private lateinit var btnAdvancedCommands: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_other, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupButtons()
        setupNfcListener()
        observeState()
    }
    
    private fun initViews(view: View) {
        btnCopyTag = view.findViewById(R.id.btn_copy_tag)
        btnCopyInfinity = view.findViewById(R.id.btn_copy_infinity)
        btnEraseTag = view.findViewById(R.id.btn_erase_tag)
        btnLockTag = view.findViewById(R.id.btn_lock_tag)
        btnReadMemory = view.findViewById(R.id.btn_read_memory)
        btnFormatMemory = view.findViewById(R.id.btn_format_memory)
        btnSetPassword = view.findViewById(R.id.btn_set_password)
        btnRemovePassword = view.findViewById(R.id.btn_remove_password)
        btnAdvancedCommands = view.findViewById(R.id.btn_advanced_commands)
    }
    
    private fun setupButtons() {
        btnCopyTag.setOnClickListener {
            viewModel.startCopyTag()
            showMessage("步驟 1：請靠近來源標籤")
        }
        
        btnCopyInfinity.setOnClickListener {
            viewModel.startCopyInfinity()
            showMessage("步驟 1：請靠近來源標籤（無限複製模式）")
        }
        
        btnEraseTag.setOnClickListener {
            showConfirmDialog(
                "擦除標籤",
                "確定要擦除標籤上的所有資料嗎？此操作無法復原。",
                onConfirm = { viewModel.setOperation(OtherOperation.ERASE) }
            )
        }
        
        btnLockTag.setOnClickListener {
            showConfirmDialog(
                "鎖定標籤",
                "鎖定後標籤將永久變為唯讀，此操作不可逆！確定繼續？",
                onConfirm = { viewModel.setOperation(OtherOperation.LOCK) }
            )
        }
        
        btnReadMemory.setOnClickListener {
            viewModel.setOperation(OtherOperation.READ_MEMORY)
            showMessage("請靠近標籤讀取完整記憶體")
        }
        
        btnFormatMemory.setOnClickListener {
            showConfirmDialog(
                "格式化記憶體",
                "格式化將清除標籤上的所有資料，此操作無法復原。",
                onConfirm = { viewModel.setOperation(OtherOperation.FORMAT) }
            )
        }
        
        btnSetPassword.setOnClickListener {
            showPasswordInputDialog(isSet = true)
        }
        
        btnRemovePassword.setOnClickListener {
            showPasswordInputDialog(isSet = false)
        }
        
        btnAdvancedCommands.setOnClickListener {
            // TODO: 開啟 APDU 指令頁面
            Toast.makeText(requireContext(), "進階指令功能開發中", Toast.LENGTH_SHORT).show()
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
                viewModel.operationState.collect { state ->
                    when (state) {
                        is OtherOperationState.Idle -> {
                            // 空閒狀態
                        }
                        is OtherOperationState.WaitingForTag -> {
                            showMessage(state.message)
                        }
                        is OtherOperationState.Processing -> {
                            showMessage("處理中...")
                        }
                        is OtherOperationState.Success -> {
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                        }
                        is OtherOperationState.Error -> {
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(requireContext().getColor(R.color.color_error))
                                .show()
                        }
                    }
                }
            }
        }
    }
    
    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("確定") { _, _ ->
                onConfirm()
                showMessage("請靠近標籤執行操作")
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showPasswordInputDialog(isSet: Boolean) {
        val editText = android.widget.EditText(requireContext())
        editText.hint = "輸入 4 字節密碼（十六進位）"
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isSet) "設定密碼" else "移除密碼")
            .setView(editText)
            .setPositiveButton("確定") { _, _ ->
                val password = editText.text.toString()
                if (isSet) {
                    viewModel.setPasswordOperation(password)
                } else {
                    viewModel.removePasswordOperation(password)
                }
                showMessage("請靠近標籤執行操作")
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}

