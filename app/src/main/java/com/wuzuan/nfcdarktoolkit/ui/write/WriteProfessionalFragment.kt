package com.wuzuan.nfcdarktoolkit.ui.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
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
    private lateinit var tvHint: TextView
    private lateinit var tvPreview: TextView
    private lateinit var btnWrite: Button
    private lateinit var progressBar: ProgressBar
    
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
        tvHint = view.findViewById(R.id.tv_hint)
        tvPreview = view.findViewById(R.id.tv_preview)
        btnWrite = view.findViewById(R.id.btn_write_pro)
        progressBar = view.findViewById(R.id.progress_write_pro)
        
        etInput.addTextChangedListener {
            updatePreview()
        }
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
        val typeNames = types.map { it.displayName }
        
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeNames
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = types[position]
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
        updatePreview()
    }
    
    private fun updatePreview() {
        val input = etInput.text.toString()
        if (input.isBlank()) {
            tvPreview.text = "預覽：\n（輸入內容後顯示）"
            return
        }
        
        val type = viewModel.selectedType.value
        val preview = when (type) {
            WriteType.TEXT -> "將寫入文字：\n$input"
            WriteType.URL -> "將寫入網址：\n$input"
            WriteType.SEARCH -> "將寫入搜尋：\nhttps://www.google.com/search?q=$input"
            
            WriteType.SOCIAL_DISCORD,
            WriteType.SOCIAL_INSTAGRAM,
            WriteType.SOCIAL_FACEBOOK,
            WriteType.SOCIAL_LINE,
            WriteType.SOCIAL_TELEGRAM,
            WriteType.SOCIAL_TWITTER,
            WriteType.SOCIAL_YOUTUBE,
            WriteType.SOCIAL_TIKTOK -> {
                val url = SocialUrlBuilder.buildUrl(type, input)
                "將寫入 ${type.displayName}：\n$url"
            }
            
            WriteType.VIDEO -> "將寫入影片連結：\n$input"
            WriteType.FILE -> "將寫入檔案連結：\n$input"
            WriteType.APPLICATION -> "將啟動應用程式：\nmarket://details?id=$input"
            WriteType.MAIL -> "將寫入 Email：\nmailto:$input"
            WriteType.CONTACT -> "將寫入聯絡人：\n$input"
            WriteType.PHONE -> "將撥打電話：\ntel:$input"
            WriteType.SMS -> "將發送簡訊：\nsms:?body=$input"
            WriteType.LOCATION -> "將開啟地圖：\ngeo:$input"
            WriteType.ADDRESS -> "將搜尋地址：\ngeo:0,0?q=$input"
            WriteType.BITCOIN -> "將開啟錢包：\nbitcoin:$input"
            WriteType.BLUETOOTH -> "藍牙裝置：\n$input"
            WriteType.WIFI -> "WiFi 網路：\n$input"
        }
        
        tvPreview.text = "預覽：\n$preview"
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.writeState.collect { state ->
                    when (state) {
                        is WriteProfessionalState.Idle -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "靠近標籤以寫入"
                        }
                        is WriteProfessionalState.Writing -> {
                            progressBar.visibility = View.VISIBLE
                            btnWrite.isEnabled = false
                            btnWrite.text = "寫入中..."
                        }
                        is WriteProfessionalState.Success -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "靠近標籤以寫入"
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                            etInput.text?.clear()
                        }
                        is WriteProfessionalState.Error -> {
                            progressBar.visibility = View.GONE
                            btnWrite.isEnabled = true
                            btnWrite.text = "靠近標籤以寫入"
                            Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(requireContext().getColor(R.color.color_error))
                                .show()
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
                    handleWrite(tag)
                }
            }
        }
        
        btnWrite.setOnClickListener {
            Snackbar.make(requireView(), "請靠近 NFC 標籤", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun handleWrite(tag: android.nfc.Tag) {
        val input = etInput.text.toString()
        if (input.isBlank()) {
            Snackbar.make(requireView(), "請輸入內容", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        viewModel.writeToTag(tag, input)
    }
}
