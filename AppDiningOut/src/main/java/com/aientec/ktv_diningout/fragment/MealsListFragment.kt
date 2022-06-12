package com.aientec.ktv_diningout.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_diningout.common.MealsGroup
import com.aientec.ktv_diningout.databinding.FragmentMealsListBinding
import com.aientec.ktv_diningout.databinding.ItemOrderBinding
import com.aientec.ktv_diningout.databinding.ItemOrderContentBinding
import com.aientec.ktv_diningout.dialog.MealsListCheckDialog
import com.aientec.ktv_diningout.viewmodel.MealsViewModel
import com.aientec.structure.Meals

class MealsListFragment : Fragment() {
    private lateinit var binding: FragmentMealsListBinding

    private val mealsViewModel: MealsViewModel by activityViewModels()

    private val groupAdapter: GroupAdapter = GroupAdapter()

    private lateinit var checkDialogFragment: MealsListCheckDialog

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

        checkDialogFragment = MealsListCheckDialog()

        binding.list.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = groupAdapter
        }

        mealsViewModel.mealsGroupList.observe(viewLifecycleOwner, {
            groupAdapter.list = it
        })

        mealsViewModel.selectedMealsGroup.observe(viewLifecycleOwner, {
            if (it == null)
                closeDialog()
            else
                showDialog()
        })

        mealsViewModel.isUpload.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            val msg: String = "上傳" + if (it) "成功" else "失敗"

            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

            mealsViewModel.startUpdate()
        })
    }

    override fun onResume() {
        super.onResume()
        mealsViewModel.startUpdate()
    }

    override fun onPause() {
        super.onPause()
        mealsViewModel.stopUpdate()
    }

    private fun showDialog() {
        checkDialogFragment.show(childFragmentManager, "check")
    }

    private fun closeDialog() {
        checkDialogFragment.dismiss()
    }

    private inner class GroupAdapter : RecyclerView.Adapter<GroupAdapter.GroupItemViewHolder>() {
        var list: List<MealsGroup>? = null
            set(value) {
                field = value

                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
            return GroupItemViewHolder(ItemOrderBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
            holder.mGroup = list!![position]
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        private inner class GroupItemViewHolder(private val mBinding: ItemOrderBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var mGroup: MealsGroup? = null
                @SuppressLint("SetTextI18n")
                set(value) {
                    field = value
                    if (field != null) {
                        mBinding.groupName.text = "${field!!.boxName} : ${field!!.id}"
                        mealsAdapter.list = field!!.mealsList
                    }
                }

            private val mealsAdapter: MealsAdapter = MealsAdapter()

            init {
                mBinding.confirm.setOnClickListener {
                    if (mealsAdapter.selectMealsList.isEmpty()) return@setOnClickListener

                    mealsViewModel.onGroupSelected(
                        mGroup ?: return@setOnClickListener,
                        mealsAdapter.selectMealsList
                    )
                }
                mBinding.list.apply {
                    this.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    this.adapter = mealsAdapter
                }
            }

        }
    }

    private inner class MealsAdapter : RecyclerView.Adapter<MealsAdapter.MealsItemViewHolder>() {

        var list: List<Meals>? = null
            set(value) {
                field = value
                if (field != null) {
                    selectMealsList.removeIf {
                        !field!!.contains(it)
                    }
                }
                notifyDataSetChanged()
            }

        val selectMealsList: ArrayList<Meals> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealsItemViewHolder {
            return MealsItemViewHolder(
                ItemOrderContentBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: MealsItemViewHolder, position: Int) {
            holder.meals = list!![position]
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        private inner class MealsItemViewHolder(private val mBinding: ItemOrderContentBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var meals: Meals? = null
                set(value) {
                    field = value
                    if (field != null) {
                        mBinding.name.text = field!!.name
                        mBinding.count.text = field!!.count.toString()
                        mBinding.cjeck.isChecked = selectMealsList.contains(field)
                        itemView.setOnClickListener {
                            if (selectMealsList.contains(field))
                                selectMealsList.remove(field)
                            else
                                selectMealsList.add(field!!)
                            notifyItemChanged(list!!.indexOf(field))
                        }
                    }
                }


        }
    }
}