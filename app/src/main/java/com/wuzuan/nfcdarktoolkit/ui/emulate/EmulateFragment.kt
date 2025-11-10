package com.wuzuan.nfcdarktoolkit.ui.emulate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wuzuan.nfcdarktoolkit.databinding.FragmentEmulateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmulateFragment : Fragment() {
    
    private var _binding: FragmentEmulateBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmulateBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

