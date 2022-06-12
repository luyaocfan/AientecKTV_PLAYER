package com.aientec.ktv_vod.fragment

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aientec.ktv_vod.activity.MainActivity
import com.aientec.ktv_vod.databinding.FragmentAuthorizationBinding

class AuthorizationFragment : Fragment() {
    private lateinit var binding: FragmentAuthorizationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.movementMethod = ScrollingMovementMethod.getInstance()
        binding.accept.setOnClickListener {
            activity?.startActivity(Intent(requireActivity(), MainActivity::class.java))
            activity?.finish()
        }
        binding.cancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.accept.performClick()
    }
}