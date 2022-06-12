package com.aientec.ktv_vod.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.databinding.DialogRoomSelectBinding
import com.aientec.ktv_vod.databinding.ItemRoomBinding
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.aientec.structure.Room

class RoomSelectDialog : DialogFragment() {

    private lateinit var binding: DialogRoomSelectBinding

    private val systemViewModel: SystemViewModel by activityViewModels()

    private val infoAdapter: InfoAdapter = InfoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.decorView?.systemUiVisibility =
            requireActivity().window.decorView.systemUiVisibility

        dialog!!.setOnShowListener { //Clear the not focusable flag from the window
            dialog!!.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            //Update the WindowManager with the new attributes (no nicer way I know of to do this)..
            val wm: WindowManager =
                requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(dialog!!.window?.decorView, dialog!!.window?.attributes)
        }

        dialog!!.setCancelable(false)

        dialog!!.setCanceledOnTouchOutside(false)

        binding = DialogRoomSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = infoAdapter
        }

        systemViewModel.rooms.observe(viewLifecycleOwner, {
            infoAdapter.list = it
        })
    }

    override fun onStart() {
        super.onStart()
        systemViewModel.updateRooms()
    }

    private inner class InfoViewHolder(private val mBinding: ItemRoomBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        var room: Room? = null
            set(value) {
                field = value
                if (field != null)
                    mBinding.root.text = field!!.name
            }

        init {
            itemView.setOnClickListener {
                dismiss()
                systemViewModel.onRoomSelected(room!!)
            }
        }
    }

    private inner class InfoAdapter : RecyclerView.Adapter<InfoViewHolder>() {
        var list: List<Room>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder =
            InfoViewHolder(ItemRoomBinding.inflate(layoutInflater, parent, false))

        override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
            holder.room = list!![position]
        }

        override fun getItemCount(): Int = list?.size ?: 0
    }
}