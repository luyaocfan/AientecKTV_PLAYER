package com.aientec.ktv_vod.fragment.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.common.FakeTool
import com.aientec.ktv_vod.databinding.*
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.structure.Track
import com.squareup.picasso.Picasso
import kotlin.math.ceil

class PlaylistFragment : Fragment() {
      private lateinit var binding: FragmentPlaylistBinding

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val itemAdapter: PageAdapter = PageAdapter()

      private lateinit var popupWindow: PopupWindow

      private lateinit var popBinding: ViewTrackCommandBinding

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentPlaylistBinding.inflate(inflater, container, false)
            initPopupWindow()
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            binding.list.apply {
                  layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                  adapter = itemAdapter
            }

            trackViewModel.playingTracks.observe(viewLifecycleOwner) {
                  itemAdapter.list = it
            }

      }

      private fun onItemClick(view: View, track: Track) {
            trackViewModel.onTrackSelected(track)
            showPopup(view, track.state == Track.State.QUEUE)
      }

      private fun initPopupWindow() {
            popBinding = ViewTrackCommandBinding.inflate(layoutInflater)
            popBinding.order.setOnClickListener {
                  trackViewModel.onOrderTrack()
                  popupWindow.dismiss()
            }
            popBinding.insert.setOnClickListener {
                  trackViewModel.onInsertTrack()
                  popupWindow.dismiss()
            }
            popBinding.del.setOnClickListener {
                  trackViewModel.onPlaylistDeleteTrack()
                  popupWindow.dismiss()
            }
            popBinding.move.setOnClickListener {
                  trackViewModel.onPlayListMoveTrackToTop()
                  popupWindow.dismiss()
            }

            popupWindow = PopupWindow(popBinding.root).apply {
                  width = ViewGroup.LayoutParams.WRAP_CONTENT
                  height = ViewGroup.LayoutParams.WRAP_CONTENT
                  isOutsideTouchable = true
            }
      }

      private fun showPopup(parent: View, isOnQueue: Boolean) {
//        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
            if (isOnQueue) {
                  popBinding.insert.visibility = View.GONE
                  popBinding.order.visibility = View.GONE
                  popBinding.move.visibility = View.VISIBLE
                  popBinding.del.visibility = View.VISIBLE
            } else {
                  popBinding.insert.visibility = View.VISIBLE
                  popBinding.order.visibility = View.VISIBLE
                  popBinding.move.visibility = View.GONE
                  popBinding.del.visibility = View.GONE
            }
            popupWindow.showAsDropDown(parent)
      }


      private inner class PageAdapter : RecyclerView.Adapter<PageAdapter.ItemViewHolder>() {
            var list: List<Track>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun getItemViewType(position: Int): Int {
                  return when (list!![position].state) {
                        Track.State.NONE -> 1
                        Track.State.QUEUE -> 3
                        Track.State.NEXT -> 2
                        Track.State.PLAYING -> 2
                        Track.State.DONE -> 1
                  }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return when (viewType) {
                        1 -> DoneTrackItem(
                              ItemPlayListDoneBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                        2 -> LockedTrackItem(
                              ItemPlayListLockedBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                        3 -> QueueTrackItem(
                              ItemPlayListQueueBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                        else -> DoneTrackItem(
                              ItemPlayListDoneBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                  }
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.track = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }

            abstract inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                  open var track: Track? = null
            }

            inner class DoneTrackItem(private val mBinding: ItemPlayListDoneBinding) :
                  ItemViewHolder(mBinding.root) {

                  override var track: Track? = null
                        set(value) {
                              field = value
                              val t: Track = track ?: return
                              mBinding.apply {
                                    name.text = t.name
                                    name.setTextColor(Color.parseColor("#A8A7A7"))
                                    singer.text = t.performer
                                    singer.setTextColor(Color.parseColor("#A8A7A7"))

                                    state.text = "播畢"
                                    state.setTextColor(Color.parseColor("#A8A7A7"))
                              }
                        }

                  init {
                        itemView.setOnClickListener { onItemClick(itemView, track!!) }

                  }
            }

            inner class LockedTrackItem(private val mBinding: ItemPlayListLockedBinding) :
                  ItemViewHolder(mBinding.root) {

                  override var track: Track? = null
                        set(value) {
                              field = value
                              val t: Track = track ?: return
                              mBinding.apply {
                                    name.text = t.name
                                    name.setTextColor(
                                          if (t.state == Track.State.PLAYING) Color.parseColor(
                                                "#DB00FF"
                                          ) else Color.parseColor("#7B61FF")
                                    )
                                    singer.text = t.performer
                                    singer.setTextColor(
                                          if (t.state == Track.State.PLAYING) Color.parseColor(
                                                "#DB00FF"
                                          ) else Color.parseColor("#7B61FF")
                                    )

                                    state.text =
                                          if (t.state == Track.State.PLAYING) "播放中" else "準備中"
                                    state.setTextColor(
                                          if (t.state == Track.State.PLAYING) Color.parseColor(
                                                "#DB00FF"
                                          ) else Color.parseColor("#7B61FF")
                                    )
                              }

                        }
            }

            inner class QueueTrackItem(private val mBinding: ItemPlayListQueueBinding) :
                  ItemViewHolder(mBinding.root) {

                  override var track: Track? = null
                        set(value) {
                              field = value
                              val t: Track = track ?: return
                              mBinding.apply {
                                    name.text = t.name
                                    name.setTextColor(Color.parseColor("#FFFFFF"))
                                    singer.text = t.performer
                                    singer.setTextColor(Color.parseColor("#FFFFFF"))

                                    state.text = ""
                                    state.setTextColor(Color.parseColor("#FFFFFF"))
                              }

                        }

                  init {
                        itemView.setOnClickListener { onItemClick(itemView, track!!) }
                  }
            }
      }
}