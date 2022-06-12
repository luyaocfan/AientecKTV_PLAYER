package com.aientec.ktv_pos_tablet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_pos_tablet.databinding.FragmentReserveBinding
import com.aientec.ktv_pos_tablet.databinding.ItemReserveInfoBinding
import com.aientec.ktv_pos_tablet.viewmodel.ReserveViewModel
import com.aientec.structure.Reserve

class ReserveFragment : Fragment() {
    private lateinit var binding: FragmentReserveBinding

    private val reserveViewModel: ReserveViewModel by activityViewModels()

    private val itemAdapter: ItemAdapter = ItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReserveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = itemAdapter
        }

        reserveViewModel.reserveList.observe(viewLifecycleOwner, {
            itemAdapter.list = it
        })
    }

    override fun onResume() {
        super.onResume()
        reserveViewModel.updateReserveList()
    }

    private inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

        var list: List<Reserve>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder(ItemReserveInfoBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.reserve = list!![position]
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        inner class ItemViewHolder(private val mBinding: ItemReserveInfoBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var reserve: Reserve? = null
                set(value) {
                    field = value
                    if (field != null) {
                        mBinding.checkTime.text =
                            if (field!!.checkInTime.isEmpty()) "--" else field!!.checkInTime
                        mBinding.code.text = field!!.code
                        mBinding.name.text = field!!.memberName
                        mBinding.phone.text = field!!.memberPhone
                        mBinding.time.text = field!!.time
                        mBinding.count.text = field!!.personCount.toString()
                        mBinding.ctrl.isChecked = field!!.checkInTime.isNotEmpty()
                    }
                }

            init {
                mBinding.ctrl.setOnClickListener {
                    if (reserve == null) return@setOnClickListener
                    if (reserve!!.checkInTime.isEmpty())
                        reserveViewModel.onReserveCheckIn(reserve ?: return@setOnClickListener)
                    else
                        reserveViewModel.onReserveCancel(reserve ?: return@setOnClickListener)
                }
            }
        }


    }
}
