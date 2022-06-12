package com.aientec.ktv_vod.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentSingerListBinding
import com.aientec.ktv_vod.databinding.ItemSingerBinding
import com.aientec.ktv_vod.structure.Singer
import com.aientec.ktv_vod.viewmodel.SingerViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel

class SingerListFragment : Fragment() {
    private lateinit var binding: FragmentSingerListBinding

    private val singerViewModel: SingerViewModel by activityViewModels()

    private val trackViewModel: TrackViewModel by activityViewModels()

    private val uiViewModel: UiViewModel by activityViewModels()

    private val itemAdapter: ItemAdapter = ItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingerListBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.pageName.text = "歌星點歌"

        binding.list.apply {
            this.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            this.adapter = itemAdapter
        }

        singerViewModel.singerList.observe(viewLifecycleOwner) {
            itemAdapter.list = it
        }

        binding.home.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment,true)
        }


        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        singerViewModel.singerListUpdate()
    }


    private inner class ItemHolder(private val mBinding: ItemSingerBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        var singer: Singer? = null
            set(value) {
                field = value
//                mBinding.name.text = field?.name ?: ""
            }

        init {
            itemView.setOnClickListener {
                if (singer != null) {
//                    trackViewModel.onSingerSelected(singer!!)
//                    uiViewModel.setMainPage(R.id.trackListFragment)
                }
            }
        }
    }

    private inner class ItemAdapter : RecyclerView.Adapter<ItemHolder>() {
        var list: List<Singer>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(ItemSingerBinding.inflate(layoutInflater))
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.singer = list?.get(position)
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }
    }
}