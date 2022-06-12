package com.aientec.ktv_vod.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentSearchMenuBinding
import com.aientec.ktv_vod.viewmodel.SearchViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel

class SearchMenuFragment : Fragment() {
      private lateinit var binding: FragmentSearchMenuBinding

      private val searchViewModel: SearchViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentSearchMenuBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            binding.type1.setOnClickListener(onClickListener)

            binding.type2.setOnClickListener(onClickListener)

            binding.type3.setOnClickListener(onClickListener)

            binding.type4.setOnClickListener(onClickListener)

            binding.ads.testCount(5)
      }

      private val onClickListener: View.OnClickListener = View.OnClickListener {
            when (it) {
                  binding.type1 -> {
                        searchViewModel.onSelectTypeChanged(1)
                  }
                  binding.type2 -> {
                        searchViewModel.onSelectTypeChanged(2)
                  }
                  binding.type3 -> {
                        searchViewModel.onSelectTypeChanged(3)
                  }
                  binding.type4 -> {
                        searchViewModel.onSelectTypeChanged(4)
                  }
            }

            uiViewModel.onControlActionEvent("SONG_SEARCHING_RESULT")
      }
}