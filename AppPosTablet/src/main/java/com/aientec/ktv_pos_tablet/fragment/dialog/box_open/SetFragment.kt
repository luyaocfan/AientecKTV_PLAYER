package com.aientec.ktv_pos_tablet.fragment.dialog.box_open

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxOrderSetBinding
import com.aientec.ktv_pos_tablet.viewmodel.BoxViewModel

class SetFragment() : Fragment() {
    private lateinit var binding: FragmentBoxOrderSetBinding

    private val viewModel: BoxViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxOrderSetBinding.inflate(inflater, container, false)
        return binding.root
    }
}