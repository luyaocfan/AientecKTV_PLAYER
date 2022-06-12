package com.aientec.ktv_pos_tablet.fragment.dialog.box_open

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxReserveBinding
import com.aientec.ktv_pos_tablet.databinding.ItemCheckedReserveInfoBinding
import com.aientec.ktv_pos_tablet.viewmodel.OrderViewModel
import com.aientec.ktv_pos_tablet.viewmodel.ReserveViewModel
import com.aientec.structure.Reserve

class CheckedReserveFragment : Fragment() {
    private lateinit var binding: FragmentBoxReserveBinding

    private val reserveViewModel: ReserveViewModel by activityViewModels()

    private val orderViewModel: OrderViewModel by activityViewModels()

    private val itemAdapter: ItemAdapter = ItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxReserveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = itemAdapter
        }

        reserveViewModel.reserveList.observe(viewLifecycleOwner, {
            itemAdapter.list = it
        })

        orderViewModel.reserve.observe(viewLifecycleOwner, {
            itemAdapter.selectedItem = it
        })
    }

    override fun onResume() {
        super.onResume()
        reserveViewModel.updateCheckedReserveList()
    }

    private inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        var list: List<Reserve>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        var selectedItem: Reserve? = null
            set(value) {
                if (list == null) {
                    field = value
                } else {
                    if (field != null)
                        notifyItemChanged(list!!.indexOfFirst { it.id == field!!.id })
                    field = value
                    notifyItemChanged(list!!.indexOfFirst { it.id == field?.id })
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder(
                ItemCheckedReserveInfoBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.reserve = list!![position]
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        inner class ItemViewHolder(private val mBinding: ItemCheckedReserveInfoBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var reserve: Reserve? = null
                set(value) {
                    field = value
                    if (field != null) {
                        mBinding.code.text = field!!.code
                        mBinding.name.text = field!!.memberName
                        mBinding.phone.text = field!!.memberPhone
                        mBinding.time.text = field!!.time
                        mBinding.count.text = field!!.personCount.toString()
                        mBinding.ctrl.isChecked = field == selectedItem
                    }
                }

            init {
                mBinding.ctrl.setOnClickListener {
                    if (reserve == selectedItem) {
                        orderViewModel.setReserve(null)
                    } else {
                        orderViewModel.setReserve(reserve)
                    }
                }
            }
        }
    }
}