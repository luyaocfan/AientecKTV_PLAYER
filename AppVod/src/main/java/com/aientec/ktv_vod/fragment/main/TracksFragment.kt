package com.aientec.ktv_vod.fragment.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.FakeTool
import com.aientec.ktv_vod.databinding.*
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel
import com.aientec.structure.Track
import com.squareup.picasso.Picasso
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class TracksFragment : Fragment() {
      private val trackViewModel: TrackViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      private lateinit var binding: FragmentTracksBinding

      private val itemAdapter: ItemAdapter = ItemAdapter()

      private lateinit var popupWindow: SimpleTooltip

      private lateinit var popupBinding: ViewTrackCommandBinding

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentTracksBinding.inflate(layoutInflater, container, false)
            initPopupWindow()
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            binding.list.apply {
                  this.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                  this.adapter = itemAdapter
            }

            trackViewModel.tracks.observe(viewLifecycleOwner) {
                  itemAdapter.list = it
            }

            trackViewModel.playingTracks.observe(viewLifecycleOwner) {
                  itemAdapter.compareTracks = it
            }



            binding.ads.testCount(6)
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

      private inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
            var list: List<Track>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            var compareTracks: List<Track>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return ItemViewHolder(
                        ItemTrackBinding.inflate(
                              layoutInflater,
                              parent,
                              false
                        )
                  )
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.track = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }

            inner class ItemViewHolder(private val mBinding: ItemTrackBinding) :
                  RecyclerView.ViewHolder(mBinding.root) {
                  var track: Track? = null
                        set(value) {
                              field = value
                              if (field != null) {
                                    mBinding.name.text = field!!.name
                                    mBinding.performer.text = field!!.performer

                                    val color: Int =
                                          if (compareTracks?.contains(field) == true) resources.getColor(
                                                R.color.accentPurple,
                                                null
                                          ) else Color.WHITE
                                    mBinding.name.setTextColor(color)
                                    mBinding.performer.setTextColor(color)
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
      }
}