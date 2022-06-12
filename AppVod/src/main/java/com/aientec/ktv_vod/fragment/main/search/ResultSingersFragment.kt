package com.aientec.ktv_vod.fragment.main.search

import android.app.UiAutomation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.databinding.FragmentResultSingersBinding
import com.aientec.ktv_vod.databinding.ItemSingerBinding
import com.aientec.ktv_vod.structure.Singer
import com.aientec.ktv_vod.viewmodel.SearchViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel
import kotlin.math.sin

class ResultSingersFragment : Fragment() {
      private lateinit var binding: FragmentResultSingersBinding

      private val searchViewModel: SearchViewModel by activityViewModels()

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      private val itemAdapter: ItemAdapter = ItemAdapter()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentResultSingersBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.list.apply {
                  layoutManager =
                        GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)

                  adapter = itemAdapter
            }

            searchViewModel.singerResults.observe(viewLifecycleOwner) {
                  itemAdapter.list = it
            }
      }

      private inner class ItemViewHolder(private val mBinding: ItemSingerBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var singer: Singer? = null
                  set(value) {
                        field = value
                        if (field != null) {
                              mBinding.root.text = field!!.name
                        }
                  }

            init {
                  itemView.setOnClickListener {
                        trackViewModel.onSingerSelected(singer!!)
                        uiViewModel.onControlActionEvent("ACTION:TRACKS")
                  }
            }
      }

      private inner class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {
            var list: List<Singer>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return ItemViewHolder(ItemSingerBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.singer = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }
      }
}