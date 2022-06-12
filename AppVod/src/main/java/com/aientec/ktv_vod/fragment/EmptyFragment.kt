package com.aientec.ktv_vod.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aientec.ktv_vod.databinding.FragmentEmptyBinding

class EmptyFragment : Fragment() {
    private lateinit var binding: FragmentEmptyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmptyBinding.inflate(inflater, container, false)
        return binding.root
    }
}