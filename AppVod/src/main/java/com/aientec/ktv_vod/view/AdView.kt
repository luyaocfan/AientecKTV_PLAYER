package com.aientec.ktv_vod.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.ItemHomeAdsBinding
import com.aientec.ktv_vod.databinding.ViewAdBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class AdView : FrameLayout {
      private lateinit var binding: ViewAdBinding

      private val adResList: ArrayList<Int> = ArrayList()

      private lateinit var layoutInflater: LayoutInflater

      private val adAdapter: AdsAdapter = AdsAdapter()

      private val timer: Timer = Timer("adTimer")

      constructor(context: Context) : super(context) {
            initViews(context)
      }

      constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
            initViews(context)
      }

      constructor(context: Context, attributeSet: AttributeSet, defaultAttr: Int) : super(
            context,
            attributeSet,
            defaultAttr
      ) {
            initViews(context)
      }

      private fun initViews(context: Context) {
            layoutInflater = LayoutInflater.from(context)

            binding = ViewAdBinding.inflate(layoutInflater, this, true)

            binding.pages.adapter = adAdapter

            TabLayoutMediator(binding.ind, binding.pages) { _, _ ->
            }.attach()

            val tabs = binding.ind.getChildAt(0) as ViewGroup

            for (i in 0 until tabs.childCount) {
                  val tab = tabs.getChildAt(i)
                  val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
                  layoutParams.marginEnd = 8
                  layoutParams.marginStart = 8
                  tab.layoutParams = layoutParams
            }

            timer.scheduleAtFixedRate(object : TimerTask() {
                  private var index: Int = 0
                  override fun run() {
                        MainScope().launch {
                              if (adResList.size > 0) {
                                    index = (index + 1) % adResList.size
                                    binding.pages.setCurrentItem(index, true)
                              }
                        }
                  }

            }, 3000, 3000)
      }

      fun testCount(count: Int) {
            for (i in 0 until count)
                  adResList.add(R.drawable.img_demo_pic)
            adAdapter.notifyDataSetChanged()

      }

      private inner class AdsAdapter() : RecyclerView.Adapter<AdsAdapter.AdViewHolder>() {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
                  return AdViewHolder(ItemHomeAdsBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
                  holder.res = adResList[position]
            }

            override fun getItemCount(): Int {
                  return adResList.size
            }

            inner class AdViewHolder(private val mBinding: ItemHomeAdsBinding) :
                  RecyclerView.ViewHolder(mBinding.root) {
                  var res: Int? = null
                        set(value) {
                              field = value
                              if (field != null) {
                                    mBinding.picture.setImageResource(field!!)
                                    mBinding.root.setOnClickListener {
                                          Log.d(
                                                "Trace",
                                                "Root size : ${mBinding.root.width}, ${mBinding.root.height}"
                                          )
                                          Log.d(
                                                "Trace",
                                                "Pic size : ${mBinding.picture.width}, ${mBinding.picture.height}"
                                          )
                                          Log.d(
                                                "Trace",
                                                "sub title size : ${mBinding.subTitle.width}, ${mBinding.subTitle.height}"
                                          )
                                          Log.d(
                                                "Trace",
                                                "title size : ${mBinding.title.width}, ${mBinding.title.height}"
                                          )
                                    }
                              }
                        }
            }
      }
}