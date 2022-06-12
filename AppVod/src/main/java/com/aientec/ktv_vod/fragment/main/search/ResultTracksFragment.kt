package com.aientec.ktv_vod.fragment.main.search

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentResultSingersBinding
import com.aientec.ktv_vod.databinding.ItemSingerBinding
import com.aientec.ktv_vod.databinding.ItemTrackBinding
import com.aientec.ktv_vod.databinding.ViewTrackCommandBinding
import com.aientec.ktv_vod.structure.Singer
import com.aientec.ktv_vod.viewmodel.SearchViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.structure.Track
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class ResultTracksFragment : Fragment() {
      private lateinit var binding: FragmentResultSingersBinding

      private val searchViewModel: SearchViewModel by activityViewModels()

      private val itemAdapter: ItemAdapter = ItemAdapter()

      private val trackViewModel: TrackViewModel by activityViewModels()

      private lateinit var popupWindow: SimpleTooltip

      private lateinit var popupBinding: ViewTrackCommandBinding

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentResultSingersBinding.inflate(inflater, container, false)
            initPopupWindow()
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.list.apply {
                  layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                  adapter = itemAdapter
            }

            searchViewModel.trackResults.observe(viewLifecycleOwner) {
                  itemAdapter.list = it
            }
      }

      private fun showPopup(parent: View) {
            if (popupBinding.root.parent != null)
                  (popupBinding.root.parent as ViewGroup).removeView(popupBinding.root)

            popupWindow = SimpleTooltip.Builder(requireContext())
                  .modal(true)
                  .focusable(true)
                  .anchorView(parent)
                  .gravity(Gravity.BOTTOM)
                  .contentView(popupBinding.root, 0)
                  .arrowColor(resources.getColor(R.color.accentPurple, null))
                  .dismissOnInsideTouch(false)
                  .build()

            popupWindow.show()
      }

      private fun closePopupWindow() {
            if (popupWindow.isShowing) {
                  popupWindow.dismiss()
            }
      }

      private fun initPopupWindow() {
            popupBinding = ViewTrackCommandBinding.inflate(layoutInflater)
            popupBinding.order.setOnClickListener {
                  trackViewModel.onOrderTrack()
                  closePopupWindow()
            }
            popupBinding.insert.setOnClickListener {
                  trackViewModel.onInsertTrack()
                  closePopupWindow()
            }
      }

      private inner class ItemViewHolder(private val mBinding: ItemTrackBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var track: Track? = null
                  set(value) {
                        field = value
                        if (field != null) {
                              mBinding.name.text = field!!.name
                              mBinding.performer.text = field!!.performer
                        }
                  }

            init {
                  itemView.setOnClickListener {
                        trackViewModel.onTrackSelected(track)
                        showPopup(it)
                  }
                  mBinding.order.setOnClickListener {
                        trackViewModel.onTrackSelected(track)
                        trackViewModel.onOrderTrack()
                  }
                  mBinding.insert.setOnClickListener {
                        trackViewModel.onTrackSelected(track)
                        trackViewModel.onInsertTrack()
                  }
            }
      }

      private inner class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {
            var list: List<Track>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return ItemViewHolder(ItemTrackBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.track = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }
      }
}