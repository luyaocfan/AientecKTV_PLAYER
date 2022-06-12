package com.aientec.ktv_pos_tablet.fragment.dialog


import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.DialogCameraPlayBinding
import com.aientec.ktv_pos_tablet.structure.IpCamera
import com.aientec.ktv_pos_tablet.viewmodel.MarkerViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.*
import kotlin.collections.ArrayList

class CameraPlayDialog : DialogFragment() {
    private lateinit var binding: DialogCameraPlayBinding

    private val viewModel: MarkerViewModel by activityViewModels()

    private var streamUri: String? = null

    private lateinit var libVLC: LibVLC

    private lateinit var mediaPlayer: MediaPlayer

    private var isFullscreen: Boolean = false

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

        binding = DialogCameraPlayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setNormal()

        binding.close.setOnClickListener {
            dismiss()
        }

        binding.screenToggle.setOnClickListener {
            if (isFullscreen)
                setNormal()
            else
                setFullScreen()
        }

        val options: ArrayList<String> = ArrayList()

        libVLC = LibVLC(context, options)

        mediaPlayer = MediaPlayer(libVLC)

        mediaPlayer.aspectRatio = "16:9"

        binding.preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d("Trace", "surfaceCreated")

                mediaPlayer.vlcVout.setVideoView(binding.preview)

                mediaPlayer.vlcVout.attachViews()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d("Trace", "surfaceChanged : $width, $height")
                mediaPlayer.vlcVout.setWindowSize(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("Trace", "surfaceDestroyed")
                mediaPlayer.vlcVout.detachViews()
            }
        })

        viewModel.selectedCamera.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            play(it)
        })


    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying)
            mediaPlayer.stop()
        viewModel.onCameraSelected(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun play(cam: IpCamera) {
        streamUri =
            String.format(Locale.TAIWAN, "rtsp://%s/11", cam.host.trim())

        binding.url.text = streamUri

        val media: Media = Media(libVLC, Uri.parse(streamUri))

        mediaPlayer.media = media

        mediaPlayer.volume = 0

        mediaPlayer.play()

        mediaPlayer.setEventListener { event ->
            val state: String = when (event.type) {
                0x102 -> "開啟中"
                0x103 -> "緩存中"
                0x104 -> "播放中"
                0x105 -> "暫停"
                0x106 -> "停止"
                0x10b -> "播放中"
                else -> event.type.toString()
            }
            MainScope().launch {
                binding.url.text = "$streamUri ($state)"
            }
        }
    }

    private fun setNormal() {
        isFullscreen = false

        binding.screenToggle.setImageResource(R.drawable.ic_fullscreen)
        binding.root.setPadding(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                resources.displayMetrics
            ).toInt()
        )

        val dm = Resources.getSystem().displayMetrics
        val width: Int = (dm.widthPixels.toFloat() * 2.0f / 3.0f).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setFullScreen() {
        isFullscreen = true

        binding.screenToggle.setImageResource(R.drawable.ic_close_fullscreen)
        binding.root.setPadding(0)

        val dm = Resources.getSystem().displayMetrics
        val width: Int = dm.widthPixels
        val height: Int = (width.toFloat() * 9.0f / 16.0f).toInt()
        dialog?.window?.setLayout(width, height)
    }
}