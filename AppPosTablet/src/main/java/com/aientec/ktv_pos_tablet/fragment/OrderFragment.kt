package com.aientec.ktv_pos_tablet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aientec.ktv_pos_tablet.databinding.FragmentOrderBinding

class OrderFragment : Fragment() {
    private lateinit var binding: FragmentOrderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }
}