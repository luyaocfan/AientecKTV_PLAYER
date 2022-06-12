package com.aientec.ktv_pos_tablet.fragment.dialog

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxOrderBinding
import com.aientec.ktv_pos_tablet.viewmodel.BoxViewModel
import com.aientec.ktv_pos_tablet.viewmodel.OrderViewModel

class BoxOpenDialog : DialogFragment() {
    private lateinit var binding: FragmentBoxOrderBinding

    private val orderViewModel: OrderViewModel by activityViewModels()

    private val boxViewModel:BoxViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        dialog!!.window?.decorView?.systemUiVisibility =
            requireActivity().window.decorView.systemUiVisibility

        dialog!!.setOnShowListener { //Clear the not focusable flag from the window
            dialog!!.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            //Update the WindowManager with the new attributes (no nicer way I know of to do this)..
            val wm: WindowManager =
                requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(dialog!!.window?.decorView, dialog!!.window?.attributes)
        }

        binding = FragmentBoxOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.accept.setOnClickListener {
            orderViewModel.createOrder()
        }

        orderViewModel.box.observe(viewLifecycleOwner,{
            if (it == null) return@observe
            binding.room.text = it.name
        })

        orderViewModel.isCreated.observe(viewLifecycleOwner,{
            if(it == null) return@observe
            dismiss()
        })


        setFullScreen()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        boxViewModel.onEventChanged(BoxViewModel.EVENT_NONE)
    }

    private fun setFullScreen() {
        val dm = Resources.getSystem().displayMetrics
        val width: Int = dm.widthPixels
        val height: Int = (width.toFloat() * 9.0f / 16.0f).toInt()
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.color.primaryDarkColor)
    }
}