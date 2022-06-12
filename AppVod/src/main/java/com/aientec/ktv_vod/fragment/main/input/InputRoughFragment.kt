package com.aientec.ktv_vod.fragment.main.input

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentInputRouphBinding
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel

class InputRoughFragment : Fragment() {
    private lateinit var binding: FragmentInputRouphBinding

    private val trackViewModel: TrackViewModel by activityViewModels()

    private val uiViewModel: UiViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputRouphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.inputSelector.setOnCheckedChangeListener { _, checkedId ->
            val inputType: Repository.InputType = when (checkedId) {
                R.id.phonetic -> Repository.InputType.PHONETIC
                R.id.spell -> Repository.InputType.SPELLING
                R.id.other -> Repository.InputType.OTHER
                else -> Repository.InputType.OTHER
            }

            trackViewModel.onInputTypeChange(inputType)
        }


        binding.hide.setOnClickListener {
            binding.keyboardLayout.visibility = View.GONE
            binding.show.visibility = View.VISIBLE
        }

        binding.show.setOnClickListener {
            binding.show.visibility = View.GONE
            binding.keyboardLayout.visibility = View.VISIBLE
        }
    }


}