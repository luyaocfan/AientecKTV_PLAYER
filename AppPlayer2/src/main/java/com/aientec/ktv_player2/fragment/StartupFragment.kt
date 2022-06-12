package com.aientec.ktv_player2.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_player2.activity.MainActivity
import com.aientec.ktv_player2.databinding.FragmentStartupBinding
import com.aientec.ktv_player2.viewmodel.SystemViewModel

class StartupFragment : Fragment() {
    private lateinit var binding: FragmentStartupBinding

    private val systemViewModel: SystemViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        systemViewModel.systemReady.observe(viewLifecycleOwner) {
            if (it) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else
                binding.msg.text = "連線失敗"
        }
    }

    override fun onResume() {
        super.onResume()
        systemViewModel.initialize()
    }
}