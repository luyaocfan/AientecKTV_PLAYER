package com.aientec.ktv_pos_tablet.fragment.dialog.box_open

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxOrderTimeBinding
import com.aientec.ktv_pos_tablet.viewmodel.BoxViewModel
import com.aientec.ktv_pos_tablet.viewmodel.OrderViewModel
import java.lang.Exception

class TimeFragment : Fragment() {
    private lateinit var binding: FragmentBoxOrderTimeBinding

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxOrderTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val count: Int = binding.layoutTime.childCount

        for (index in 0 until count) {
            binding.layoutTime.getChildAt(index).setOnClickListener {
                val duration: Int = Integer.valueOf(it.tag as String)
                orderViewModel.setDuration(duration)
            }
        }

        orderViewModel.duration.observe(viewLifecycleOwner, {
            if (it == null || it == -1)
                binding.min.setText("0")
            else
                binding.min.setText(it.toString())
        })

        orderViewModel.reserve.observe(viewLifecycleOwner, {
            binding.root.isEnabled = it != null
        })

        binding.min.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val str: String = p0.toString()
                val duration: Int = try {
                    Integer.valueOf(str)
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
                orderViewModel.setDuration(duration)
            }
        })
    }

}