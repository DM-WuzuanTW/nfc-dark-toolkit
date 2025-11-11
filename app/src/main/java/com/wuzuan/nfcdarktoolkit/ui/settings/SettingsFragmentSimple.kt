package com.wuzuan.nfcdarktoolkit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragmentSimple : Fragment() {
    
    private val viewModel: SettingsViewModel by viewModels()
    
    private lateinit var rgTheme: RadioGroup
    private lateinit var rbThemeSystem: RadioButton
    private lateinit var rbThemeDark: RadioButton
    private lateinit var rbThemeLight: RadioButton
    private lateinit var switchAutoSave: SwitchCompat
    private lateinit var switchSafeMode: SwitchCompat
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_simple, container, false)
        
        try {
            rgTheme = view.findViewById(R.id.rg_theme_simple)
            rbThemeSystem = view.findViewById(R.id.rb_theme_system_simple)
            rbThemeDark = view.findViewById(R.id.rb_theme_dark_simple)
            rbThemeLight = view.findViewById(R.id.rb_theme_light_simple)
            switchAutoSave = view.findViewById(R.id.switch_auto_save_simple)
            switchSafeMode = view.findViewById(R.id.switch_safe_mode_simple)
            
            setupListeners()
            observeSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun setupListeners() {
        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_theme_system_simple -> {
                    viewModel.setThemeMode("system")
                    showMessage("已切換至跟隨系統主題")
                }
                R.id.rb_theme_dark_simple -> {
                    viewModel.setThemeMode("dark")
                    showMessage("已切換至深色主題")
                }
                R.id.rb_theme_light_simple -> {
                    viewModel.setThemeMode("light")
                    showMessage("已切換至淺色主題")
                }
            }
        }
        
        switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoSaveHistory(isChecked)
            showMessage(if (isChecked) "已開啟自動儲存歷史" else "已關閉自動儲存歷史")
        }
        
        switchSafeMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSafeModeEnabled(isChecked)
            showMessage(if (isChecked) "已開啟安全模式" else "已關閉安全模式")
        }
    }
    
    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.themeMode.collect { mode ->
                        when (mode) {
                            "system" -> rbThemeSystem.isChecked = true
                            "dark" -> rbThemeDark.isChecked = true
                            "light" -> rbThemeLight.isChecked = true
                        }
                    }
                }
                
                launch {
                    viewModel.autoSaveHistory.collect { enabled ->
                        switchAutoSave.isChecked = enabled
                    }
                }
                
                launch {
                    viewModel.safeModeEnabled.collect { enabled ->
                        switchSafeMode.isChecked = enabled
                    }
                }
            }
        }
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}

