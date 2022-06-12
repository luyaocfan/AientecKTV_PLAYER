package com.aientec.ktv_vod.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentQrCodeBinding
import com.aientec.ktv_vod.viewmodel.RoomViewModel
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrCodeFragment : Fragment() {
    private lateinit var binding: FragmentQrCodeBinding

    private val systemViewModel: SystemViewModel by activityViewModels()

    private val service: ExecutorService = Executors.newSingleThreadExecutor()

    private var imageSize: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        systemViewModel.qrCodeData?.let {
            it.observe(viewLifecycleOwner, { data ->
                if (data != null)
                    binding.display.setImageBitmap(BitmapFactory.decodeFile(data))
                else
                    showData("Qr code data is null")
            })
        }

        binding.start.setOnClickListener {
            findNavController().navigate(R.id.action_qrcodeFragment_to_authorizationFragment)
        }

        binding.log.movementMethod = ScrollingMovementMethod()
    }

    override fun onResume() {
        super.onResume()
        binding.start.performClick()
    }

    private fun showData(msg: String, image: Bitmap? = null) {
        MainScope().launch {
            binding.log.text = msg

            if (image != null)
                binding.display.setImageBitmap(image)
        }
    }

}