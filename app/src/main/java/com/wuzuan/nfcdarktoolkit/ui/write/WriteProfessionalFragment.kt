package com.wuzuan.nfcdarktoolkit.ui.write

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
 * 專業級寫入 Fragment（無 emoji，專業UI）
 */
@AndroidEntryPoint
class WriteProfessionalFragment : Fragment() {
    
    private val viewModel: WriteProfessionalViewModel by viewModels()
    
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var etInput: EditText
    private lateinit var etWifiPassword: EditText
    private lateinit var etSmsPhone: EditText
    private lateinit var btnPickContact: Button
    private lateinit var btnPickLocation: Button
    private lateinit var tvHint: TextView
    private lateinit var btnWrite: Button
    
    // 等待寫入的狀態
    private var isWaitingForTag = false
    private var waitingDialog: androidx.appcompat.app.AlertDialog? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private lateinit var progressBar: ProgressBar
    
    private lateinit var layoutContactInput: LinearLayout
    private lateinit var etContactName: EditText
    private lateinit var etContactPhone: EditText
    private lateinit var etContactEmail: EditText
    
    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                val cursor = requireActivity().contentResolver.query(contactUri, null, null, null, null)
                cursor?.use { 
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val emailCursor = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            arrayOf(it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))),
                            null
                        )
                        emailCursor?.use {
                            if (it.moveToFirst()) {
                                val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                                val email = it.getString(emailIndex)
                                etContactEmail.setText(email)
                            }
                        }
                        val name = it.getString(nameIndex)
                        val phone = it.getString(phoneIndex)
                        etContactName.setText(name)
                        etContactPhone.setText(phone)
                    }
                }
            }
        }
    }
    
    private val pickLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { locationUri ->
                etInput.setText(locationUri.toString())
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchContactPicker()
        } else {
            Snackbar.make(requireView(), "需要聯絡人權限才能選擇聯絡人", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_write_professional, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupCategorySpinner()
        setupObservers()
        setupNfcListener()
    }
    
    private fun initViews(view: View) {
        spinnerCategory = view.findViewById(R.id.spinner_category)
        spinnerType = view.findViewById(R.id.spinner_type)
        etInput = view.findViewById(R.id.et_input)
        etWifiPassword = view.findViewById(R.id.et_wifi_password)
        etSmsPhone = view.findViewById(R.id.et_sms_phone)
        btnPickContact = view.findViewById(R.id.btn_pick_contact)
        btnPickLocation = view.findViewById(R.id.btn_pick_location)
        tvHint = view.findViewById(R.id.tv_hint)
        btnWrite = view.findViewById(R.id.btn_write_pro)
        progressBar = view.findViewById(R.id.progress_write_pro)
        
        layoutContactInput = view.findViewById(R.id.layout_contact_input)
        etContactName = view.findViewById(R.id.et_contact_name)
        etContactPhone = view.findViewById(R.id.et_contact_phone)
        etContactEmail = view.findViewById(R.id.et_contact_email)
        
        btnPickContact.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchContactPicker()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        }
        
        btnPickLocation.setOnClickListener {
            launchLocationPicker()
        }
    }
    
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }
    
    private fun launchLocationPicker() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=0,0(地點)"))
        pickLocationLauncher.launch(intent)
    }
    
    private fun setupCategorySpinner() {
        val categories = WriteType.getCategoryList()
        
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = categories[position]
                updateTypeSpinner(category)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun updateTypeSpinner(category: String) {
        val types = WriteType.getCategories()[category] ?: emptyList()
        
        val items = types.map { type ->
            IconSpinnerItem(
                text = type.displayName,
                iconRes = type.getIconResource(),
                value = type
            )
        }
        
        val typeAdapter = IconSpinnerAdapter(requireContext(), items)
        spinnerType.adapter = typeAdapter
        
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = items[position].value ?: return
                viewModel.setWriteType(selectedType)
                updateInputHint(selectedType)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun updateInputHint(type: WriteType) {
        val hint = SocialUrlBuilder.getPlaceholder(type)
        tvHint.text = hint
        etInput.hint = hint
        
        layoutContactInput.visibility = if (type == WriteType.CONTACT) View.VISIBLE else View.GONE
        etInput.visibility = if (type == WriteType.CONTACT) View.GONE else View.VISIBLE
        
        btnPickLocation.visibility = if (type == WriteType.LOCATION) View.VISIBLE else View.GONE
        btnPickContact.visibility = if (type == WriteType.CONTACT) View.VISIBLE else View.GONE
        etSmsPhone.visibility = if (type == WriteType.SMS) View.VISIBLE else View.GONE
        etWifiPassword.visibility = if (type == WriteType.WIFI) View.VISIBLE else View.GONE
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.writeState.collect { state ->
                    when (state) {
                        is WriteProfessionalState.Idle -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "寫入"
                        }
                        is WriteProfessionalState.Writing -> {
                            progressBar.visibility = View.VISIBLE
                            btnWrite.isEnabled = false
                            btnWrite.text = "寫入中..."
                        }
                        is WriteProfessionalState.Success -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "寫入"
                            // 不再清空輸入框，讓用戶可以連續寫入多張相同內容的卡片
                            // 更新對話框為成功狀態，停留一下再關閉
                            showSuccessDialog()
                        }
                        is WriteProfessionalState.Error -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "寫入"
                            // 更新對話框為失敗狀態，停留一下再關閉
                            showErrorDialog(state.message)
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
                    // 只有在等待寫入狀態時才處理 NFC 標籤
                    if (isWaitingForTag) {
                        handleWrite(tag)
                    }
                }
            }
        }
        
        // 修改按鈕點擊邏輯：顯示對話框並進入等待狀態
        btnWrite.setOnClickListener {
            showWaitingDialog()
        }
    }
    
    /**
     * 顯示等待靠卡的對話框
     */
    private fun showWaitingDialog() {
        // 先清理舊的對話框（如果存在）
        dismissWaitingDialogImmediately()
        
        // 驗證輸入
        val currentType = viewModel.selectedType.value
        val input = etInput.text.toString()
        val contactName = etContactName.text.toString()
        val contactPhone = etContactPhone.text.toString()
        val contactEmail = etContactEmail.text.toString()
        
        // 簡單驗證
        if (currentType != WriteType.CONTACT && input.isBlank()) {
            Snackbar.make(requireView(), "請先輸入內容", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (currentType == WriteType.CONTACT && contactName.isBlank() && contactPhone.isBlank() && contactEmail.isBlank()) {
            Snackbar.make(requireView(), "請先輸入聯絡人資訊", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        isWaitingForTag = true
        
        waitingDialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("準備寫入")
            .setMessage("請將 NFC 卡靠近手機背面")
            .setIcon(R.drawable.ic_scan)
            .setCancelable(true)
            .setNegativeButton("取消") { dialog, _: Int ->
                dismissWaitingDialog()
                dialog.dismiss()
            }
            .setOnDismissListener {
                // 對話框被關閉時，停止動畫並重置等待狀態
                stopAnimation()
                if (isWaitingForTag) {
                    isWaitingForTag = false
                }
                waitingDialog = null
            }
            .show()
            
        // 啟動動態文字效果
        startWaitingAnimation()
    }
    
    /**
     * 啟動等待動畫（動態點點點效果）
     */
    private fun startWaitingAnimation() {
        var dotCount = 0
        animationRunnable = object : Runnable {
            override fun run() {
                waitingDialog?.let { dialog ->
                    val dots = ".".repeat((dotCount % 4))
                    dialog.setMessage("請將 NFC 卡靠近手機背面$dots")
                    dotCount++
                    handler.postDelayed(this, 500) // 每 500ms 更新一次
                }
            }
        }
        handler.post(animationRunnable!!)
    }
    
    /**
     * 停止動畫
     */
    private fun stopAnimation() {
        animationRunnable?.let {
            handler.removeCallbacks(it)
            animationRunnable = null
        }
    }
    
    /**
     * 顯示成功對話框
     */
    private fun showSuccessDialog() {
        stopAnimation()
        // 先移除所有舊的延遲任務
        handler.removeCallbacksAndMessages(null)
        
        waitingDialog?.let { dialog ->
            dialog.setTitle("✓ 寫入成功")
            dialog.setMessage("NFC 卡已成功寫入資料！")
            dialog.findViewById<android.widget.Button>(android.R.id.button2)?.visibility = View.GONE
            
            // 1.5 秒後自動關閉
            handler.postDelayed({
                dismissWaitingDialog()
            }, 1500)
        }
    }
    
    /**
     * 顯示失敗對話框
     */
    private fun showErrorDialog(message: String) {
        stopAnimation()
        // 先移除所有舊的延遲任務
        handler.removeCallbacksAndMessages(null)
        
        waitingDialog?.let { dialog ->
            dialog.setTitle("✗ 寫入失敗")
            dialog.setMessage(message)
            dialog.findViewById<android.widget.Button>(android.R.id.button2)?.visibility = View.GONE
            
            // 2 秒後自動關閉
            handler.postDelayed({
                dismissWaitingDialog()
            }, 2000)
        }
    }
    
    /**
     * 關閉等待對話框（用於取消或手動關閉，會重置狀態）
     */
    private fun dismissWaitingDialog() {
        stopAnimation()
        isWaitingForTag = false
        waitingDialog?.dismiss()
        // 重置 ViewModel 狀態，允許下一次寫入
        viewModel.resetState()
        // waitingDialog 會在 onDismissListener 中被設為 null
    }
    
    /**
     * 立即清理對話框（用於在創建新對話框前清理舊的）
     */
    private fun dismissWaitingDialogImmediately() {
        stopAnimation()
        isWaitingForTag = false
        waitingDialog?.dismiss()
        waitingDialog = null
        // 移除所有延遲任務
        handler.removeCallbacksAndMessages(null)
        // 重置 ViewModel 狀態，允許下一次寫入
        viewModel.resetState()
    }
    
    private fun handleWrite(tag: android.nfc.Tag) {
        val input = etInput.text.toString()
        val wifiPassword = etWifiPassword.text.toString()
        val smsPhone = etSmsPhone.text.toString()
        val contactName = etContactName.text.toString()
        val contactPhone = etContactPhone.text.toString()
        val contactEmail = etContactEmail.text.toString()
        
        viewModel.writeToTag(tag, input, wifiPassword, smsPhone, contactName, contactPhone, contactEmail)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 清理資源
        stopAnimation()
        dismissWaitingDialog()
    }
}
