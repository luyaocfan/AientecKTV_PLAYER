package com.aientec.ktv_staff.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.aientec.ktv_staff.R
import com.aientec.ktv_staff.databinding.FragmentLoginBinding
import com.aientec.ktv_staff.viewmodel.UserViewModel

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
                findNavController().navigate(R.id.action_loginFragment_to_simpleRoomOCFragment)
            }
        })
    }
}