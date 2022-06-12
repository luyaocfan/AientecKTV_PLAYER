package com.aientec.ktv_pantry.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_pantry.R
import com.aientec.ktv_pantry.databinding.FragmentMealsListBinding
import com.aientec.ktv_pantry.viewmodel.MealsViewModel
import com.aientec.structure.Meals

class MealsListFragment : Fragment() {
    private lateinit var binding: FragmentMealsListBinding

    private val mealsViewModel: MealsViewModel by activityViewModels()

    private var list: List<Meals>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMealsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mealsViewModel.mealsList.observe(viewLifecycleOwner) {
            if (it != null) {

                if (list == null) {
                    mealsViewModel.printItem(it)
                } else {
                    val printItem: ArrayList<Meals> = ArrayList()

                    for (item in it) {
                        if (!list!!.contains(item))
                            printItem.add(item)
                    }

                    mealsViewModel.printItem(printItem)
                }

                list = it
                updateItem(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mealsViewModel.stopUpdate()
    }

    override fun onResume() {
        super.onResume()
        mealsViewModel.startUpdate()
    }

    private fun updateItem(list: List<Meals>) {
        for (i in 0 until 2) {
            for (j in 0 until 10) {
                val index: Int = i * 10 + j

                val subViewGroup = if (i == 0) {
                    binding.listLeft.list.getChildAt(j)
                } else {
                    binding.listRight.list.getChildAt(j)
                }

                if (index >= list.size) {
                    updateItemInfo(index, subViewGroup, null)
                } else {
                    updateItemInfo(index, subViewGroup, list[index])
                }

            }
        }
    }

    private fun updateItemInfo(index: Int, view: View, item: Meals?) {
        view.findViewById<TextView>(R.id.sn).text = if (item == null) "" else index.toString()
        view.findViewById<TextView>(R.id.box).text = item?.boxName ?: ""

        view.findViewById<TextView>(R.id.time).apply {
            if (item == null) {
                this.setTextColor(Color.WHITE)
                this.text = ""
            } else {
                val nowTime: Long = System.currentTimeMillis()

                val min: Int = ((nowTime - item.time) / 1000L / 60L).toInt()

                when {
                    min > 5 -> this.setTextColor(Color.RED)
                    else -> this.setTextColor(Color.WHITE)
                }
                this.text = min.toString()
            }
        }

        view.findViewById<TextView>(R.id.content).text = item?.name ?: ""

        view.findViewById<TextView>(R.id.count).text = (item?.count ?: "").toString()

    }
}