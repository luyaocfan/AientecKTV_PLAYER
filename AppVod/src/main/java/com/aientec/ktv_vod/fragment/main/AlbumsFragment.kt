package com.aientec.ktv_vod.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.databinding.FragmentAlbumsBinding
import com.aientec.ktv_vod.databinding.ItemSongListBinding
import com.aientec.ktv_vod.viewmodel.AlbumViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel
import com.aientec.structure.Album
import com.squareup.picasso.Picasso

class AlbumsFragment : Fragment() {
      private lateinit var binding: FragmentAlbumsBinding

      private val itemAdapter: ItemAdapter = ItemAdapter()

      private val albumViewModel: AlbumViewModel by activityViewModels()

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentAlbumsBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.list.apply {
                  layoutManager =
                        GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
                  adapter = itemAdapter
            }

            albumViewModel.lists.observe(viewLifecycleOwner) {
                  itemAdapter.list = it
            }

            binding.ad.testCount(4)
      }

      override fun onResume() {
            super.onResume()
            val type: Int = arguments?.getInt("type", -1) ?: -2

            if (type > 0)
                  albumViewModel.updateAlbums(type)
      }

      private inner class ItemViewHolder(private val mBinding: ItemSongListBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var album: Album? = null
                  set(value) {
                        field = value
                        if (field != null) {
                              mBinding.title.text = field!!.name
                              Picasso.get().load(BuildConfig.FILE_ROOT + field!!.cover)
                                    .into(mBinding.picture)
                        }
                  }

            init {
                  itemView.setOnClickListener {
                        if (album != null) {
                              trackViewModel.onAlbumSelected(album!!)
                              uiViewModel.onControlActionEvent("ACTION:TRACKS")
                        }
                  }
            }
      }

      private inner class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {
            var list: List<Album>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return ItemViewHolder(ItemSongListBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.album = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }
      }
}