package com.aientec.ktv_vod.fragment.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentMainHomeMenuBinding
import com.aientec.ktv_vod.databinding.ItemHomeAdsBinding
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
      private lateinit var binding: FragmentMainHomeMenuBinding

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentMainHomeMenuBinding.inflate(inflater, container, false)

            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            binding.ads.testCount(8)

            binding.menu.apply {
                  for (i in 0 until childCount) {
                        val v = getChildAt(i)
                        if (v is TableRow) {
                              val count: Int = v.childCount
                              for (j in 0 until count) {
                                    val tag: String = (v.getChildAt(j).tag ?: continue) as String

                                    if (tag.startsWith("ACTION:")) {
                                          v.getChildAt(j).setOnClickListener(onMenuClickListener)
                                    }
                              }
                        }
                  }
            }
      }

      private val onMenuClickListener: View.OnClickListener = View.OnClickListener {
            uiViewModel.onControlActionEvent(it.tag as String)
      }

}