package com.aientec.ktv_pos_tablet.fragment

import android.database.DataSetObserver
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxBinding
import com.aientec.ktv_pos_tablet.databinding.ViewFloorItemsBinding
import com.aientec.ktv_pos_tablet.fragment.dialog.BoxOpenDialog
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.ktv_pos_tablet.structure.Floor
import com.aientec.ktv_pos_tablet.util.avoidDropdownFocus
import com.aientec.ktv_pos_tablet.viewmodel.FloorViewModel
import com.aientec.ktv_pos_tablet.viewmodel.BoxViewModel
import com.aientec.ktv_pos_tablet.viewmodel.OrderViewModel
import com.aientec.ktv_pos_tablet.viewmodel.TypeViewModel
import com.aientec.structure.Room

class BoxFragment : Fragment() {
    private lateinit var binding: FragmentBoxBinding

    private val floorViewModel: FloorViewModel by activityViewModels()

    private val boxViewModel: BoxViewModel by activityViewModels()

    private val typeViewModel: TypeViewModel by activityViewModels()

    private val orderViewModel: OrderViewModel by activityViewModels()

    private val floorAdapter: FloorListAdapter = FloorListAdapter()

    private val typeAdapter: TypeListAdapter = TypeListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.selector.setOnCheckedChangeListener { radioGroup, checkedId ->
            val dest: Int =
                when (radioGroup.findViewById<RadioButton>(checkedId).tag as String) {
                    "LIST" -> {
                        floorViewModel.updateFloors(true)
                        R.id.listFragment
                    }
                    "MAP" -> {
                        floorViewModel.updateFloors(false)
                        R.id.mapFragment
                    }
                    else -> return@setOnCheckedChangeListener
                }


            activity?.let {
                Navigation.findNavController(binding.root.findViewById(R.id.nav_fragment_room))
                    .navigate(dest)
            }
        }

        binding.floorSpinner.apply {
            this.avoidDropdownFocus()
            this.adapter = floorAdapter
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    floorViewModel.onFloorSelected(position)
                    boxViewModel.onBoxSelected(null)
                }
            }
        }

        binding.typeSpinner.apply {
            this.avoidDropdownFocus()
            this.adapter = typeAdapter
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    typeViewModel.onTypeSelected(position)
                    boxViewModel.onBoxSelected(null)
                }
            }
        }

        floorViewModel.floors.observe(viewLifecycleOwner, {
            floorAdapter.list = it
            binding.floorSpinner.adapter = floorAdapter
            binding.floorSpinner.setSelection(0)
        })

        typeViewModel.types.observe(viewLifecycleOwner, {
            typeAdapter.list = it
            binding.typeSpinner.adapter = typeAdapter
            binding.typeSpinner.setSelection(0)
        })

        boxViewModel.selectedBox.observe(viewLifecycleOwner, {
            if (it == null)
                binding.root.transitionToStart()
            else
                binding.root.transitionToEnd()

            orderViewModel.setBox(it)
        })

        boxViewModel.event.observe(viewLifecycleOwner, {
            when (it) {
                BoxViewModel.EVENT_NONE -> {
                }
                BoxViewModel.EVENT_OPEN_BOX -> {
//                    boxViewModel.createOrder(boxViewModel.selectedBox.value!!)

                    BoxOpenDialog().show(childFragmentManager, "box_open")
                }
                BoxViewModel.EVENT_CLOSE_BOX -> {
                    orderViewModel.closeBox()
                }
                BoxViewModel.EVENT_CHECKOUT -> {
                    boxViewModel.checkBill()
                }
            }
        })

        floorViewModel.selectFloor.observe(viewLifecycleOwner, {
            binding.floorSpinner.setSelection(floorViewModel.getSelectionFloorIndex())
        })

        orderViewModel.isCreated.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            val msg: String = "包廂開啟" + if (it) "成功" else "失敗"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            boxViewModel.onBoxSelected(null)
            orderViewModel.orderInit()
        })

        orderViewModel.isBoxClose.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            val msg: String = "包廂關閉" + if (it) "成功" else "失敗"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            boxViewModel.onBoxSelected(null)
            orderViewModel.orderInit()
        })

    }

    override fun onResume() {
        super.onResume()
        boxViewModel.updateBoxes()
        binding.rbList.isChecked = true

    }

    private inner class TypeListAdapter : SpinnerAdapter {
        var list: ArrayList<Room.Type>? = null

        override fun isEmpty(): Boolean {
            return list == null
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ViewFloorItemsBinding
            if (convertView == null) {
                mBinding = ViewFloorItemsBinding.inflate(layoutInflater, parent, false)
                mBinding.root.tag = mBinding
                mBinding.root.setTextColor(Color.WHITE)
            } else {
                mBinding = convertView.tag as ViewFloorItemsBinding
            }

            mBinding.root.text = list?.get(position)?.name ?: ""

            return mBinding.root
        }

        override fun registerDataSetObserver(observer: DataSetObserver?) {

        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun getItem(position: Int): Any {
            return list!![position]
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return list!![position].hashCode().toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ViewFloorItemsBinding
            if (convertView == null) {
                mBinding = ViewFloorItemsBinding.inflate(layoutInflater, parent, false)
                mBinding.root.tag = mBinding

            } else {
                mBinding = convertView.tag as ViewFloorItemsBinding
            }

            mBinding.root.text = list?.get(position)?.name ?: ""

            return mBinding.root
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver?) {

        }

        override fun getCount(): Int {
            return list?.size ?: 0
        }
    }

    private inner class FloorListAdapter : SpinnerAdapter {
        var list: ArrayList<Floor>? = null


        override fun isEmpty(): Boolean {
            return list == null
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ViewFloorItemsBinding
            if (convertView == null) {
                mBinding = ViewFloorItemsBinding.inflate(layoutInflater, parent, false)
                mBinding.root.tag = mBinding
                mBinding.root.setTextColor(Color.WHITE)
            } else {
                mBinding = convertView.tag as ViewFloorItemsBinding
            }

            mBinding.root.text = list?.get(position)?.name ?: ""

            return mBinding.root
        }

        override fun registerDataSetObserver(observer: DataSetObserver?) {

        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun getItem(position: Int): Any {
            return list!![position]
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return -1L
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ViewFloorItemsBinding
            if (convertView == null) {
                mBinding = ViewFloorItemsBinding.inflate(layoutInflater, parent, false)
                mBinding.root.tag = mBinding

            } else {
                mBinding = convertView.tag as ViewFloorItemsBinding
            }

            mBinding.root.text = list?.get(position)?.name ?: ""

            return mBinding.root
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver?) {

        }

        override fun getCount(): Int {
            return list?.size ?: 0
        }
    }
}