package com.wuzuan.nfcdarktoolkit.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.R
import com.wuzuan.nfcdarktoolkit.ui.home.CloneHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ToolsFragment : Fragment() {

    @Inject
    lateinit var cloneHelper: CloneHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        val btnClone: Button = view.findViewById(R.id.btn_clone_tool)
        btnClone.setOnClickListener {
            startCloning()
        }

        return view
    }

    private fun startCloning() {
        // For now, we'll just show a Snackbar. The actual cloning logic will be handled in a later step.
        Snackbar.make(requireView(), "請靠近來源數位名片", Snackbar.LENGTH_INDEFINITE)
            .setAction("取消") {
                // Logic to cancel cloning
            }.show()
    }
}
