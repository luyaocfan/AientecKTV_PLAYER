package com.aientec.ktv_pos_tablet.fragment.box

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxControlBinding
import com.aientec.ktv_pos_tablet.viewmodel.BoxViewModel
import com.aientec.structure.Room

class ControlFragment : Fragment() {
    private lateinit var binding: FragmentBoxControlBinding

    private val viewModel: BoxViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.open.setOnClickListener {
            viewModel.onEventChanged(BoxViewModel.EVENT_OPEN_BOX)
        }
        binding.close.setOnClickListener {
            viewModel.onEventChanged(BoxViewModel.EVENT_CLOSE_BOX)
        }
        binding.checkout.setOnClickListener {
            viewModel.onEventChanged(BoxViewModel.EVENT_CHECKOUT)
        }

        viewModel.selectedBox.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            when (it.state) {
                Room.STATE_IDLE -> {
                    binding.open.visibility = View.VISIBLE
                    binding.close.visibility = View.GONE
                    binding.checkout.visibility = View.GONE
                }
                Room.STATE_ON_USE -> {
                    binding.open.visibility = View.GONE
                    binding.close.visibility = View.GONE
                    binding.checkout.visibility = View.VISIBLE
                }
                Room.STATE_ON_CHECKED -> {
                    binding.open.visibility = View.GONE
                    binding.close.visibility = View.VISIBLE
                    binding.checkout.visibility = View.GONE
                }
            }
        })
    }
}