package com.aientec.ktv_staff.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_staff.databinding.FragmentSimpleRoomOcBinding
import com.aientec.ktv_staff.databinding.ItemSpinnerBinding
import com.aientec.ktv_staff.viewmodel.RoomViewModel
import com.aientec.structure.Room
import com.aientec.structure.Store

class SimpleRoomOCFragment : Fragment() {
    private val roomViewModel: RoomViewModel by activityViewModels()

    private lateinit var binding: FragmentSimpleRoomOcBinding

    private val storeAdapter: StoreItemAdapter = StoreItemAdapter()

    private val roomAdapter: RoomItemAdapter = RoomItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimpleRoomOcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stores.apply {
            adapter = storeAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val store: Store? = storeAdapter.getItem(position) as Store?
                    roomViewModel.onStoreSelect(store)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }

        binding.rooms.apply {
            adapter = roomAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val room: Room? = roomAdapter.getItem(position) as Room?
                    roomViewModel.onRoomSelected(room)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }

        binding.open.setOnClickListener {
            roomViewModel.onRoomOpen()
        }

        binding.close.setOnClickListener {
            roomViewModel.onRoomClose()
        }

        roomViewModel.rooms.observe(viewLifecycleOwner, {
            roomAdapter.list = it
        })

        roomViewModel.selectedRoom.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.open.isEnabled = false
                binding.close.isEnabled = false
                binding.rooms.setSelection(0)
            } else {
                binding.open.isEnabled = true
                binding.close.isEnabled = true
                val index: Int = (roomAdapter.list?.indexOf(it) ?: -1) + 1
                binding.rooms.setSelection(index)
            }
        })



        roomViewModel.stores.observe(viewLifecycleOwner, {
            storeAdapter.list = it
        })

        roomViewModel.selectStore.observe(viewLifecycleOwner, {
            if (it == null)
                binding.stores.setSelection(0)
            else {
                val index: Int = (storeAdapter.list?.indexOf(it) ?: -1) + 1
                binding.stores.setSelection(index)
            }
        })

        roomViewModel.isStart.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            Toast.makeText(requireContext(), "包廂計時 : ${if (it) "成功" else "失敗"}", Toast.LENGTH_LONG)
                .show()

            roomViewModel.onInit()
        })

        roomViewModel.isClose.observe(viewLifecycleOwner,{
            if (it == null) return@observe

            Toast.makeText(requireContext(), "包廂關閉 : ${if (it) "成功" else "失敗"}", Toast.LENGTH_LONG)
                .show()

            roomViewModel.onInit()
        })

    }

    override fun onResume() {
        super.onResume()
        roomViewModel.onStoresUpdate()
    }

    private inner class StoreItemAdapter() : BaseAdapter() {
        var list: List<Store>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return (list?.size ?: 0) + 1
        }

        override fun getItem(position: Int): Any? {
            return if (position == 0) null else list!![position - 1]
        }

        override fun getItemId(position: Int): Long {

            return if (position == 0) -1L else list!![position - 1].id.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ItemSpinnerBinding =
                if (convertView == null) {
                    ItemSpinnerBinding.inflate(layoutInflater, parent, false).apply {
                        this.root.tag = this
                    }
                } else
                    convertView.tag as ItemSpinnerBinding

            if (position == 0)
                mBinding.root.text = "請選擇店面"
            else
                mBinding.root.text = list!![position - 1].name

            return mBinding.root
        }
    }

    private inner class RoomItemAdapter : BaseAdapter() {
        var list: List<Room>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return (list?.size ?: 0) + 1
        }

        override fun getItem(position: Int): Any? {
            return if (position == 0) null else list!![position - 1]
        }

        override fun getItemId(position: Int): Long {
            return if (position == 0) -1L else list!![position - 1].id.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mBinding: ItemSpinnerBinding =
                if (convertView == null) {
                    ItemSpinnerBinding.inflate(layoutInflater, parent, false).apply {
                        this.root.tag = this
                    }
                } else
                    convertView.tag as ItemSpinnerBinding

            if (position == 0)
                mBinding.root.text = "請選擇包廂"
            else
                mBinding.root.text = list!![position - 1].name

            return mBinding.root
        }
    }
}