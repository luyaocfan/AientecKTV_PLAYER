package com.aientec.ktv_diningout.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_diningout.BuildConfig
import com.aientec.ktv_diningout.activity.MainActivity
import com.aientec.ktv_diningout.databinding.FragmentLoginBinding
import com.aientec.ktv_diningout.viewmodel.UserViewModel

class LoginFragment : Fragment() {


    private lateinit var binding: FragmentLoginBinding

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.login.setOnClickListener {
            val checking: Boolean =
                binding.account.text.isNotEmpty() && binding.account.text.isNotEmpty()

            if (checking)
                userViewModel.onLogin(
                    binding.account.text.toString().trim(),
                    binding.password.text.toString().trim()
                )
            else
                Toast.makeText(requireContext(), "請輸入帳號密碼", Toast.LENGTH_LONG).show()
        }

        userViewModel.user.observe(viewLifecycleOwner, {
            if (it == null)
                Toast.makeText(requireContext(), "登入失敗", Toast.LENGTH_LONG).show()
            else {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (BuildConfig.DEBUG) {
            binding.account.setText("emp03")
            binding.password.setText("123123")
            binding.login.performClick()
        }
    }
}