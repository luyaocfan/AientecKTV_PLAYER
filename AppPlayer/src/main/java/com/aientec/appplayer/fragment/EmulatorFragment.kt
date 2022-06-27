package com.aientec.appplayer.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.appplayer.databinding.FragmentEmulatorBinding
import com.aientec.appplayer.viewmodel.PlayerViewModel
import com.aientec.ktv_wifiap.commands.DSData
import com.aientec.structure.Track
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EmulatorFragment : Fragment() {


    private lateinit var binding: FragmentEmulatorBinding

    private val playerViewModel: PlayerViewModel by activityViewModels()

    private var currantTrack: Track? = null

    private var nextTrack: Track? = null

    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.TAIWAN)

    private val logBuffer: StringBuffer = StringBuffer()

    private var timer: Timer? = null

    private var autoTimer: Timer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmulatorBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.log.movementMethod = ScrollingMovementMethod()

        initPlayer()


//        playerViewModel.idleTracks.observe(viewLifecycleOwner) { list ->
//            if (list == null) return@observe
//            updateIdleTracks(list)
//        }

        playerViewModel.nextTrack.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            updatePlayList(it)
        }

        playerViewModel.playerFunc.let {
            it.observe(viewLifecycleOwner) { func ->
                if (func == null) return@observe
                updatePlayerFunc(func)
            }
        }
        playerViewModel.scoreMode.observe(viewLifecycleOwner) {
            playerViewModel.onScoreToggle(it)
        }

        playerViewModel.openState.observe(viewLifecycleOwner) {
            if (it) {
//                        Toast.makeText(requireContext(), "包廂開啟", Toast.LENGTH_LONG).show()
                if (timer != null)
                    timer!!.cancel()
                timer = null
                reset()
                playerViewModel.onOpen()
            } else {
                timer = Timer().apply {
                    schedule(ResetTimerTask(), 900000L)
                }
//                        Toast.makeText(requireContext(), "包廂關閉", Toast.LENGTH_LONG).show()
            }
        }

        binding.nextResponse.setOnClickListener {
            onNext()
        }

        binding.autoNext.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.nextResponse.isEnabled = !isChecked

            if (isChecked) {
                autoTimer = Timer().apply {
                    schedule(AutoNextTask(), 10000L, 10000L)
                }
            } else {
                if (autoTimer != null)
                    autoTimer!!.cancel()
                autoTimer = null
            }
        }

        updateTrackInfo()
    }

    private fun reset() {
        currantTrack = null
        nextTrack = null
        logPrint("重置")
        updateTrackInfo()
    }

    private fun updatePlayerFunc(func: DSData.PlayerFunc) {
        when (func) {
            DSData.PlayerFunc.ORIGINAL_VOCALS -> {
                logPrint("原唱")

                playerViewModel.onOriginalVocal()
            }
            DSData.PlayerFunc.BACKING_VOCALS -> {
                logPrint("伴唱")
                playerViewModel.onBackingVocal()
            }
            DSData.PlayerFunc.GUIDE_VOCAL -> {
                logPrint("導唱")
                playerViewModel.onGuideVocal()
            }
            DSData.PlayerFunc.PLAY -> {
                logPrint("播放")
                playerViewModel.onResume()
            }
            DSData.PlayerFunc.PAUSE -> {
                logPrint("暫停")
                playerViewModel.onPause()
            }
            DSData.PlayerFunc.CUT -> {
                logPrint("切歌")
                onNext()
                playerViewModel.onCut()
            }
            DSData.PlayerFunc.REPLAY -> {
                logPrint("重播")
                playerViewModel.onReplay()
            }
            DSData.PlayerFunc.FORCE_PAUSE -> {

            }
            DSData.PlayerFunc.FORCE_PLAY -> {

            }
            DSData.PlayerFunc.MUTE -> {
                logPrint("靜音")
                playerViewModel.onMute()
            }
            DSData.PlayerFunc.UN_MUTE -> {
                logPrint("取消靜音")
                playerViewModel.onUnMute()
            }
            else -> {
            }
        }
    }

    private fun updateIdleTracks(list: List<Track>) {

    }

    private fun updatePlayList(track: Track) {
        nextTrack = track
        updateTrackInfo()
    }

    private fun initPlayer() {
        playerViewModel.onReady()
    }

    private fun onNext() {
        currantTrack = nextTrack
        nextTrack = null

        if (currantTrack != null)
            playerViewModel.onPlaying()
        else {
            binding.autoNext.isChecked = false
            playerViewModel.onStop()
        }
        updateTrackInfo()
    }

    private fun updateTrackInfo() {
        MainScope().launch {
            binding.currantTrack.text = currantTrack?.name ?: "無"

            binding.nextTrack.text = nextTrack?.name ?: "無"
        }
    }

    private fun logPrint(msg: String) {
        MainScope().launch {
            logBuffer.insert(0, "${simpleDateFormat.format(Date())} : $msg\n")
            binding.log.text = logBuffer.toString()
        }

    }

    private inner class ResetTimerTask : TimerTask() {
        override fun run() {
            reset()
        }
    }

    private inner class AutoNextTask : TimerTask() {
        override fun run() {
            MainScope().launch {
                onNext()
            }

        }
    }
}