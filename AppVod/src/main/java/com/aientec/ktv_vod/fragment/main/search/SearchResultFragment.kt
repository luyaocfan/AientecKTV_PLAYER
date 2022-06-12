package com.aientec.ktv_vod.fragment.main.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentSearchResultBinding
import com.aientec.ktv_vod.view.KeyboardView
import com.aientec.ktv_vod.viewmodel.SearchViewModel

class SearchResultFragment : Fragment() {
      private lateinit var binding: FragmentSearchResultBinding

      private val searchViewModel: SearchViewModel by activityViewModels()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentSearchResultBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


            binding.searchType.setOnCheckedChangeListener { group, checkedId ->

                  when (checkedId) {
                        R.id.search_type_male -> searchViewModel.onSelectTypeChanged(1)
                        R.id.search_type_female -> searchViewModel.onSelectTypeChanged(2)
                        R.id.search_type_group -> searchViewModel.onSelectTypeChanged(3)
                        R.id.search_type_track -> searchViewModel.onSelectTypeChanged(4)
                  }
            }

            binding.keyType.setOnCheckedChangeListener { group, checkedId ->
                  val keyType = when (checkedId) {
                        R.id.key_type_1 -> 1
                        R.id.key_type_2 -> 2
                        R.id.key_type_3 -> 3
                        else -> 3
                  }
                  binding.keyboard.setKeyType(keyType)
                  searchViewModel.onKeyTypeChanged(keyType)
            }

            binding.keyboard.onKeyInputListener = object : KeyboardView.OnKeyInputListener {
                  override fun onEvent(text: String, code: Int) {
                        searchViewModel.onKeyInput(text, code)
                  }
            }

            searchViewModel.searchKeys.observe(viewLifecycleOwner) {
                  binding.input.text = it
            }

            searchViewModel.selectType.observe(viewLifecycleOwner) {
                  when (it) {
                        1 -> binding.searchType.check(R.id.search_type_male)
                        2 -> binding.searchType.check(R.id.search_type_female)
                        3 -> binding.searchType.check(R.id.search_type_group)
                        4 -> binding.searchType.check(R.id.search_type_track)
                  }
                  if (it < 4) {
                        Navigation.findNavController(binding.root.findViewById(R.id.nav_fragment_search))
                              .navigate(R.id.resultSingersFragment)
                  } else {
                        Navigation.findNavController(binding.root.findViewById(R.id.nav_fragment_search))
                              .navigate(R.id.resultTracksFragment)
                  }
            }

            searchViewModel.keyType.observe(viewLifecycleOwner) {
                  when (it) {
                        1 -> binding.keyType.check(R.id.key_type_1)
                        2 -> binding.keyType.check(R.id.key_type_2)
                  }
            }

            searchViewModel.filterKeys.observe(viewLifecycleOwner) {
                  binding.keyboard.filterKeys(it)
            }

      }
}