package com.aientec.appplayer.fragment

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.appplayer.BuildConfig
import com.aientec.appplayer.databinding.FragmentDebugBinding
import com.aientec.appplayer.databinding.ItemLogBinding
import com.aientec.appplayer.util.AudioStreamGate
import com.aientec.appplayer.viewmodel.DebugViewModel
import com.aientec.appplayer.viewmodel.OsdViewModel
import com.aientec.appplayer.viewmodel.PlayerViewModel
import idv.bruce.ui.osd.container.MarqueView
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DebugFragment : Fragment() {
      private lateinit var binding: FragmentDebugBinding

      private val debugViewModel: DebugViewModel by activityViewModels()

      private val osdViewModel: OsdViewModel by activityViewModels()

      private val playerViewModel: PlayerViewModel by activityViewModels()

      private val itemAdapter: ItemAdapter = ItemAdapter()


      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentDebugBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


            binding.list.apply {
                  layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                  adapter = itemAdapter
            }

            debugViewModel.logMsg.observe(viewLifecycleOwner) {
                  Log.d("Trace", "Log : $it")
                  if (!it.isNullOrEmpty())
                        itemAdapter.addMsg(it)
            }

            binding.log.setOnClickListener {
//            osdViewModel.test()
//                  throw RuntimeException()

//                  playerViewModel.testFn()

                  if (binding.list.visibility == View.VISIBLE)
                        binding.list.visibility = View.GONE
                  else
                        binding.list.visibility = View.VISIBLE
            }

      }

      override fun onResume() {
            super.onResume()
            debugViewModel.addLog("Version : ${BuildConfig.VERSION_NAME}")
      }


      inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

            private val msgList: ArrayList<String> = ArrayList()

            private val dateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.TAIWAN)

            fun addMsg(msg: String) {
                  msgList.add(0, "${dateFormat.format(Date())} : $msg")
                  notifyDataSetChanged()
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                  return ItemViewHolder(ItemLogBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                  holder.msg = msgList[position]
            }

            override fun getItemCount(): Int {
                  return msgList.size
            }

            inner class ItemViewHolder(private val mBinding: ItemLogBinding) :
                  RecyclerView.ViewHolder(mBinding.root) {

                  var msg: String? = null
                        set(value) {
                              field = value
                              mBinding.root.text = field
                        }
            }
      }
}