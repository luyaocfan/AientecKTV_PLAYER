package com.aientec.appplayer.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SizeF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.appplayer.BuildConfig
import com.aientec.appplayer.R
import com.aientec.appplayer.data.EventBundle
import com.aientec.appplayer.data.MTVEvent
import com.aientec.appplayer.data.MessageBundle
import com.aientec.appplayer.databinding.FragmentPlayerDualBinding
import com.aientec.appplayer.databinding.OsdCenterNotificationBinding
import com.aientec.appplayer.databinding.OsdTextBinding
import com.aientec.appplayer.util.AudioStreamGate
import com.aientec.appplayer.viewmodel.DebugViewModel
import com.aientec.appplayer.viewmodel.PlayerViewModel
import com.aientec.appplayer.viewmodel.SystemViewModel
import com.aientec.ktv_wifiap.commands.DSData
import com.aientec.structure.Track
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor
import com.ine.ktv.playerengine.InePlayerController
import com.linecorp.apng.ApngDrawable
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OsdEventListener
import idv.bruce.ui.osd.items.*
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class PlayerFragment : Fragment() {
    companion object {
        const val MAXIMUM_CACHE_COUNT: Int = 2

        const val MAXIMUM_CACHE_SIZE: Int = 1024 * 1024 * 64

        const val ORDER_SONG_PLAYERING_BUFFER_SIZE: Int = 1024 * 1024 * 64
        const val PUBLIC_VIDEO_PLAYERING_BUFFER_SIZE: Int = 1024 * 1024 * 32
        val CACHE_BANDWIDTH_KBS = intArrayOf(65536, 65536)

        const val TAG: String = "PlayerFrag"
    }

    private lateinit var binding: FragmentPlayerDualBinding

    private lateinit var controller: InePlayerController
    private val playerViewModel: PlayerViewModel by activityViewModels()
    
    private val osdViewModel: PlayerViewModel by activityViewModels()
    //private val osdViewModel: OsdViewModel by activityViewModels()

    private val debugViewModel: DebugViewModel by activityViewModels()


    private val systemViewModel: SystemViewModel by activityViewModels()

    private val mediaUrl: String = BuildConfig.MTV_URL

    private val streamGate: AudioStreamGate = AudioStreamGate()

    private val processPipeLine: ProcessPipeLine = ProcessPipeLine()

    private lateinit var fileRoot: File

    private lateinit var am: AudioManager

    private var tName: String = ""

    private var timer: Timer? = null

    private var currantName: String? = null
    //begin OSD
    //private lateinit var binding: FragmentOsdBinding

    //private val playerViewModel: PlayerViewModel by activityViewModels()

    //private val osdViewModel: OsdViewModel by activityViewModels()

    private var statusOsd: OSDItem? = null

    private var notifyOsd: OSDItem? = null

    private var nextDisplayOsd: OSDItem? = null

    private var animationOsd: OSDItem? = null

    private var animationQueue: LinkedList<OSDApngItem> = LinkedList()

    private var videoOsd: OSDItem? = null

    private var videoQueue: LinkedList<OSDVideoItem> = LinkedList()

    private var idleTag: OSDViewItem? = null

    private var muteTag: OSDViewItem? = null

    private var scoreTag: OSDSwitchViewItem? = null
    
    //end OSD
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerDualBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
//            streamGate.enable()
    }

    override fun onPause() {
        super.onPause()
//            streamGate.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.close()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fileRoot = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "ai"
        )

        fileRoot.deleteRecursively()

        if (!fileRoot.exists()) {
            fileRoot.mkdirs()
        } else {
            val files: Array<File>? = fileRoot.listFiles()
            if (files != null) {
                for (file in files)
                    file.deleteRecursively()
            }
        }

        Log.d("Trace", "File root : ${fileRoot.exists()},  ${fileRoot.absolutePath}")

        am = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        streamGate.travelTime = 5000L

        streamGate.addStreamPipeline(DisplayPipeLine())
        streamGate.addStreamPipeline(processPipeLine)

        initPlayer()

//        binding.root.controllerAuto\Show = false

//            systemViewModel.connectionState.observe(viewLifecycleOwner) {
//                  if (it)
//                        playerViewModel.onOpen()
//            }

        playerViewModel.idleTracks.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "Idle tracks update : ${list?.size}")
            if (list == null) return@observe
            updateIdleTracks(list)
        }

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
        //begin OSD
        playerViewModel.mtvEvent.observe(viewLifecycleOwner) {
            val bundle: EventBundle = it ?: return@observe
            onPlayerEvent(bundle)
        }

        osdViewModel.message?.let {
            it.observe(viewLifecycleOwner) { bundle ->
                onMessageEvent(bundle)
            }
        }

        osdViewModel.notifyMassage.observe(viewLifecycleOwner) {
            if (it != null)
                updateNotifyOsd(it)
        }

        osdViewModel.nextTrack.observe(viewLifecycleOwner) {
            if (it != null)
                eventOnNextDisplay(it)
        }

        osdViewModel.barrageMessage.observe(viewLifecycleOwner) {
            if (it != null)
                addBarrageText(it)
        }

        osdViewModel.isPublish.observe(viewLifecycleOwner) {

            if (it)
                showIdleTag()
            else
                hideIdleTag()
        }

        binding.osd.eventListener = object : OsdEventListener {
            override fun onDone(item: OSDItem) {
                Log.d(TAG, "OnOsdDne")
                if (item is OSDVideoItem)
                    playerViewModel.onLocalPlay()
                item.release()
            }

            override fun onContainerReady() {
                showIdleTag()
            }

            override fun onContainerSizeChanged(width: Int, height: Int) {

            }
        }
        //end OSD
    }

    private fun reset() {
        Log.d("Trace", "Reset")
        val list = controller.GetOrderSongPlayList()
        for (i in 1 until list.size)
            controller.DeleteOrderSong(1)
        controller.cut()
        osdViewModel.onTypeChanged(true)
    }

    private fun updatePlayerFunc(func: DSData.PlayerFunc) {
        when (func) {
            DSData.PlayerFunc.ORIGINAL_VOCALS -> {
                controller.AudioControlOutput(
                    IneStereoVolumeProcessor.AudioControlOutput_LeftMono
                )

                playerViewModel.onOriginalVocal()
            }
            DSData.PlayerFunc.BACKING_VOCALS -> {
                controller.AudioControlOutput(
                    IneStereoVolumeProcessor.AudioControlOutput_RightMono
                )
                playerViewModel.onBackingVocal()
            }
            DSData.PlayerFunc.GUIDE_VOCAL -> {
                playerViewModel.onGuideVocal()
            }
            DSData.PlayerFunc.PLAY -> {
                if (controller.isPaused) {
                    controller.resume()
                    playerViewModel.onResume()
                }
            }
            DSData.PlayerFunc.PAUSE -> {
                if (!controller.isPaused) {
                    controller.pause()
                    playerViewModel.onPause()
                }
            }
            DSData.PlayerFunc.CUT -> {
                controller.cut()
                playerViewModel.onCut()
            }
            DSData.PlayerFunc.REPLAY -> {
                controller.replay()
                playerViewModel.onReplay()
            }
            DSData.PlayerFunc.FORCE_PAUSE -> {
                if (!controller.isPaused) {
//                              controller.pause(true)
                }
            }
            DSData.PlayerFunc.FORCE_PLAY -> {
                if (controller.isPaused) {
//                              controller.resume(true)
                }
            }
            DSData.PlayerFunc.MUTE -> {
                am.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    0
                );
                playerViewModel.onMute()
            }
            DSData.PlayerFunc.UN_MUTE -> {
                am.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
                playerViewModel.onUnMute()
            }
            else -> {
            }
        }
    }

    private fun updateIdleTracks(list: List<Track>) {
//        for (track in list) {
//            controller.AddPubVideo(
//                String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
//                track.name
//            )
//        }

//            controller.AddPubVideo("http://192.168.77.210/mtv/41006YH3.mp4", "tester")
       // controller.AddPubVideo("http://192.168.99.152/100011_re_h265.mp4", "Pop_0")
        controller.AddPubVideo("https://www.hassen.myds.me/h265_60/CM100001_re.mp4", "Pop_1")
        controller.AddPubVideo("https://www.hassen.myds.me/h265_60/CM100002_re.mp4", "Pop_2")
        controller.AddPubVideo("https://www.hassen.myds.me/h265_60/CM100003_re.mp4", "Pop_3")
        controller.AddPubVideo("https://www.hassen.myds.me/h265_60/CM100004_re.mp4", "Pop_4")


        controller.open()
    }

    private fun updatePlayList(track: Track) {
        val playList = controller.GetOrderSongPlayList()

        Log.d(
            TAG,
            "${track.name} : ${
                String.format(
                    Locale.TAIWAN,
                    BuildConfig.MTV_URL,
                    track.fileName
                )
            }"
        )

        when (playList.size) {
            0 -> {

                controller.AddOrderSong(
                    String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
                    track.name,
                    ORDER_SONG_PLAYERING_BUFFER_SIZE
                )
            }
            1 -> {
                controller.AddOrderSong(
                    String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
                    track.name,
                    ORDER_SONG_PLAYERING_BUFFER_SIZE
                )
            }
            2 -> {
                if (track.name == playList[1]) return
                controller.InsertOrderSong(
                    1, String.format(Locale.TAIWAN, BuildConfig.MTV_URL, track.fileName),
                    track.name,
                    ORDER_SONG_PLAYERING_BUFFER_SIZE
                )
                controller.DeleteOrderSong(2)
            }
        }
    }

    private fun initPlayer() {
        val config: InePlayerController.InePlayerControllerConfigure =
            InePlayerController.InePlayerControllerConfigure().apply {
                context = requireContext()
                publicVideoView = binding.publicView
                orderSongView = binding.playView
                maxCacheCount = MAXIMUM_CACHE_COUNT
                itemCacheSize = MAXIMUM_CACHE_SIZE
                publicVideoPlayingBufferSize = PUBLIC_VIDEO_PLAYERING_BUFFER_SIZE
                cacheBandwidthKBS = CACHE_BANDWIDTH_KBS
                listener = eventListener
            }


        controller = InePlayerController(config)

        controller.SetVODServerList(Array<String>(1) {
            BuildConfig.MTV_URL.replace("%s", "")
        })

        for (server in controller.GetVODServerList()) {
            Log.d("Trace", "server : $server")
        }

//            controller.addDataFrameCallback(audioCallback)

        playerViewModel.onReady()
    }

    inner class DisplayPipeLine : AudioStreamGate.AudioStreamPipeline() {
        override fun onStartTrigger(volume: Double) {
            debugViewModel.addLog(
                "Vox On : ${
                    String.format(
                        Locale.TAIWAN,
                        "%.2f",
                        volume
                    )
                }"
            )
        }

        override fun onAudioDataFrame(data: ByteArray) {

        }

        override fun onStopTrigger() {
            debugViewModel.addLog("Vox off")
        }
    }

    inner class ProcessPipeLine : AudioStreamGate.AudioStreamPipeline() {
        private var toggle: Boolean = false

        private val thread: ExecutorService = Executors.newSingleThreadExecutor()

        private var future: Future<*>? = null

        private var micQueue: LinkedList<ByteArray?> = LinkedList()

        private var audioQueue: LinkedList<ByteArray?> = LinkedList()

        private var mName: String? = null

        private var mCount: Int = 1


        override fun onEnable() {
            super.onEnable()
            micQueue.clear()
            audioQueue.clear()
            future = thread.submit(WorkerRunnable(micQueue, audioQueue))
        }

        override fun onDisable() {
            super.onDisable()
            future?.cancel(false)
        }

        override fun onStartTrigger(volume: Double) {
            Log.d("Trace", "Start record")
            toggle = true

            if (mName == tName)
                mCount++
            else
                mCount = 0

            mName = tName


            micQueue.add(null)
            audioQueue.add(null)
        }

        override fun onAudioDataFrame(data: ByteArray) {
            onMicAudioIn(data)
        }

        override fun onStopTrigger() {
            Log.d("Trace", "Stop record")
            toggle = false

            micQueue.add(null)
            audioQueue.add(null)
        }

        private fun onMicAudioIn(data: ByteArray) {
            if (!toggle) return
            micQueue.add(data)
        }

        fun onAudioIn(data: ByteArray) {
            if (!toggle) return
            audioQueue.add(data)
        }

        inner class WorkerRunnable(
            micQueue: LinkedList<ByteArray?>,
            audioQueue: LinkedList<ByteArray?>
        ) :
            Runnable {
            private var micFile: RandomAccessFile? = null

            private var bgmFile: RandomAccessFile? = null

            private var vocalFile: RandomAccessFile? = null

            private val mMicQueue: LinkedList<ByteArray?> = micQueue

            private val mAudioQueue: LinkedList<ByteArray?> = audioQueue


            override fun run() {

                var data: ByteArray?

                while (true) {

                    if (mMicQueue.isNotEmpty()) {
                        data = mMicQueue.remove()

                        if (data == null) {
                            micFile = if (micFile != null) {
                                closeWaveFile(micFile!!)
                                null
                            } else {
                                createTempFile("mic")
                            }
                        } else {
                            micFile?.write(data)
                        }
                    }

                    if (mAudioQueue.isNotEmpty()) {
                        data = mAudioQueue.remove()

                        if (data == null) {
                            vocalFile = if (vocalFile != null) {
                                closeWaveFile(vocalFile!!)
                                null
                            } else
                                createTempFile("vocal")
                            bgmFile = if (bgmFile != null) {
                                closeWaveFile(bgmFile!!)
                                null
                            } else
                                createTempFile("bgm")
                        } else {
                            for (i in 0 until data!!.size step 4) {
                                vocalFile?.write(data, i, 2)
                                bgmFile?.write(data, i + 2, 2)
                            }
                        }
                    }
                    Thread.yield()
                }
            }

            private fun createTempFile(type: String): RandomAccessFile {
                Log.d("Trace", "Open file")
                return createWavFile("${mName}_${type}_${mCount}.wav")
            }
        }

        private fun createFile(name: String, mode: String): RandomAccessFile {
            val file: File = File(fileRoot, name)

            if (file.exists())
                file.delete()

            return RandomAccessFile(file, mode)
        }

        private fun createWavFile(
            name: String, sampleRate: Int = 48000,
            channelCount: Int = 1,
            encoding: Int = AudioFormat.ENCODING_PCM_16BIT
        ): RandomAccessFile {
            val file: RandomAccessFile = createFile(name, "rw")

            file.writeBytes("RIFF")
            file.write(getIntBytes(0, ByteOrder.LITTLE_ENDIAN))
            file.writeBytes("WAVEfmt ")
            file.write(getIntBytes(16, ByteOrder.LITTLE_ENDIAN))
            file.write(getShortBytes(1, ByteOrder.LITTLE_ENDIAN))
            file.write(getShortBytes(channelCount.toShort(), ByteOrder.LITTLE_ENDIAN))
            file.write(getIntBytes(sampleRate, ByteOrder.LITTLE_ENDIAN))
            file.write(getIntBytes(sampleRate * encoding, ByteOrder.LITTLE_ENDIAN))
            file.write(getShortBytes(4, ByteOrder.LITTLE_ENDIAN))
            file.write(getShortBytes((8 * encoding).toShort(), ByteOrder.LITTLE_ENDIAN))
            file.writeBytes("data")
            file.write(getIntBytes(0, ByteOrder.LITTLE_ENDIAN))

            return file
        }

        private fun closeWaveFile(
            file: RandomAccessFile
        ) {
            Log.d("Trace", "Close")
            val totalLength: Int = file.length().toInt()

            val audioLength: Int = totalLength - 44

            file.seek(4)

            file.write(getIntBytes(audioLength + 36, ByteOrder.LITTLE_ENDIAN))

            file.seek(40)

            file.write(getIntBytes(audioLength, ByteOrder.LITTLE_ENDIAN))

            file.close()

        }

        private fun getIntBytes(value: Int, order: ByteOrder): ByteArray {
            return ByteBuffer.allocate(4).order(order).putInt(value).array()
        }

        private fun getShortBytes(value: Short, order: ByteOrder): ByteArray {
            return ByteBuffer.allocate(2).order(order).putShort(value).array()
        }


    }

//      private val audioCallback: IneStereoVolumeProcessor.DataframeCallback =
//            IneStereoVolumeProcessor.DataframeCallback { pcmData ->
//                  processPipeLine.onAudioIn(pcmData)
//            }
//begin OSD
@SuppressLint("ResourceType")
private fun onMessageEvent(messageBundle: MessageBundle) {

    val item: OSDItem? = when (messageBundle.type) {

        MessageBundle.Type.TEST -> {
            testFn()
        }
        MessageBundle.Type.TXT -> {
            OSDBarrageItem(
                " ${messageBundle.data as String}",
                OSDBarrageItem.Direction.RIGHT_TO_LEFT,
                2.0f,
                Color.WHITE,
                false,
                20000L,
                null,
                Pair(0.07f, 0.6f)
            )
        }
        MessageBundle.Type.IMAGE -> {
            OSDPictureItem(
                messageBundle.data as String, 5000, PointF(-1f, -1f),
                SizeF(0.5f, 0f)
            )
        }

        MessageBundle.Type.NONE -> {
            null
        }
        MessageBundle.Type.VIDEO -> {
            videoOsd = OSDVideoItem(
                requireContext(),
                messageBundle.data as String,
                PointF(-1f, -1f),
                SizeF(0f, 0.5f)
            )
            playerViewModel.onLocalPause()
            videoOsd
//                if (videoOsd == null) {
//                    videoOsd = OSDVideoItem(
//                        requireContext(),
//                        messageBundle.data as String,
//                        PointF(-1f, -1f),
//                        SizeF(0f, 0.5f)
//                    )
//                    playerViewModel.onLocalPause()
//                    videoOsd
//                } else {
//                    videoQueue.push(
//                        OSDVideoItem(
//                            requireContext(),
//                            messageBundle.data as String,
//                            PointF(-1f, -1f),
//                            SizeF(0f, 0.5f)
//                        )
//                    )
//                    null
//                }
        }
        MessageBundle.Type.EMOJI -> {
            animationOsd = OSDApngItem(
                messageBundle.data as ApngDrawable, 2,
                PointF(-1f, -1f), SizeF(0f, 0.7f)
            )
            animationOsd
//                if (animationOsd == null) {
//                    animationOsd = OSDApngItem(
//                        messageBundle.data as ApngDrawable, 2,
//                        PointF(-1f, -1f), SizeF(0f, 0.7f)
//                    )
//                    animationOsd
//                } else {
//                    animationQueue.push(
//                        OSDApngItem(
//                            messageBundle.data as ApngDrawable, 2,
//                            PointF(-1f, -1f), SizeF(0f, 0.7f)
//                        )
//                    )
//                    null
//                }
        }
        MessageBundle.Type.VOD -> {
            updateNotifyOsd(messageBundle.data.toString())
            null
        }
    }

    binding.osd.addOsdItem(item ?: return)
}

    private fun onPlayerEvent(bundle: EventBundle) {
        Log.d(TAG, "OnEvent : ${bundle.event}")

        val item: OSDItem? = when (bundle.event) {
            MTVEvent.ON_PAUSE -> eventOnPause()

            MTVEvent.ON_RESUME -> eventOnResume()

            MTVEvent.ON_STOP -> {
                if (scoreTag != null) {
                    playerViewModel.onLocalPause()
                    eventScoreRes()
                }
                null
            }

            MTVEvent.ON_CUT -> {

                binding.event.removeCallbacks(eventHiddenTask)
                binding.eventIcon.setImageResource(R.drawable.ic_cut)
                binding.eventLabel.text = "切歌"
                binding.event.visibility = View.VISIBLE
                binding.event.postDelayed(eventHiddenTask, 2500L)

                null
//                        removeStatusOsd()
//                        val view: View = generateCenterNotifyView(R.drawable.ic_cut, "切歌")
//
//                        OSDViewItem(
//                              view, 2500,
//                              PointF(-1f, -1f), SizeF(0.33f * 9f / 16f, 0.33f)
//                        )
            }
            MTVEvent.ON_REPLAY -> {
                binding.event.removeCallbacks(eventHiddenTask)
                binding.eventIcon.setImageResource(R.drawable.ic_replay)
                binding.eventLabel.text = "重唱"
                binding.event.visibility = View.VISIBLE
                binding.event.postDelayed(eventHiddenTask, 2500L)

                null
//                        removeStatusOsd()
//                        val view: View = generateCenterNotifyView(R.drawable.ic_replay, "重唱")
//
//                        OSDViewItem(
//                              view, 2500,
//                              PointF(0 - 1f, -1f), SizeF(0.33f * 9f / 16f, 0.33f)
//                        )
            }
            MTVEvent.ON_NEXT_DISPLAY -> {
                val track: Track = bundle.data as Track
                eventOnNextDisplay(track)
                null
            }
            MTVEvent.ON_VOCAL_ORIGINAL -> {
                updateNotifyOsd("原唱")
                null
            }
            MTVEvent.ON_VOCAL_BACKING -> {
                updateNotifyOsd("伴唱")
                null
            }
            MTVEvent.ON_VOCAL_GUIDE -> {
                updateNotifyOsd("導唱")
                null
            }
            MTVEvent.ON_MUTE -> {
                showMuteTag()
                null
            }
            MTVEvent.ON_UN_MUTE -> {
                hideMuteTag()
                null
            }

            MTVEvent.ON_PLAY_TYPE_CHANGED -> null
            MTVEvent.ON_SCORE_ENABLE -> {
                eventOnScore()
            }
            MTVEvent.ON_SCORE_DISABLE -> {
                removeScore()
                null
            }
            else -> {
                null
            }
        }

        binding.osd.addOsdItem(item ?: return)
    }


    private fun eventOnResume(): OSDItem? {
        binding.event.removeCallbacks(eventHiddenTask)
        binding.eventIcon.setImageResource(R.drawable.ic_resume)
        binding.eventLabel.text = "繼續"
        binding.event.visibility = View.VISIBLE
        binding.event.postDelayed(eventHiddenTask, 1000L)
        return null
//            removeStatusOsd()
//
//            val view: View = generateCenterNotifyView(R.drawable.ic_resume, "繼續")
//
//            return OSDViewItem(
//                  view, 1000,
//                  PointF(0 - 1f, -1f), SizeF(0.33f * 9f / 16f, 0.33f)
//            )
    }

    private fun eventOnPause(): OSDItem? {
        binding.event.removeCallbacks(eventHiddenTask)
        binding.eventIcon.setImageResource(R.drawable.ic_pause)
        binding.eventLabel.text = "暫停"
        binding.event.visibility = View.VISIBLE
        return null
//            removeStatusOsd()
//
//            val firstView: View = generateCenterNotifyView(R.drawable.ic_pause, "暫停")
//
//            val secondView: View = generateTextView("暫停")
//
//
//            statusOsd = OSDSwitchViewItem(
//                  firstView,
//                  PointF(-1f, -1f),
//                  SizeF(0.33f * 9f / 16f, 0.33f),
//                  1000L,
//                  secondView,
//                  PointF(0.85f, 0.01f),
//                  SizeF(0.1f, 0.07f),
//                  -1L
//            )
//
//            return statusOsd!!
    }

    private fun eventOnScore(): OSDItem {
        removeScore()

        val firstView: View =
            generateCenterNotifyView(
                R.drawable.img_start_score,
                "",
                ImageView.ScaleType.FIT_XY
            )

        val secondView: View =
            generateCenterNotifyView(R.drawable.img_on_score, "", ImageView.ScaleType.FIT_XY)

        scoreTag = OSDSwitchViewItem(
            firstView,
            PointF(0f, 0f),
            SizeF(0f, 0.33f),
            1000L,
            secondView,
            PointF(0f, 0f),
            SizeF(0f, 0.33f),
            -1L
        )
        return scoreTag!!
    }

    private fun removeScore() {
        if (scoreTag != null) {
            binding.osd.removeOsdItem(scoreTag!!)
            scoreTag = null
        }
    }

    private fun eventScoreRes() {
        val reslist = arrayOf(
            "asset:///video/score_again.mp4",
            "asset:///video/score_excellent.mp4",
            "asset:///video/score_good.mp4"
        )

        val url: String = reslist[(Math.random() * 100).toInt() % 3]

        val item: OSDVideoItem =
            OSDVideoItem(requireContext(), url, PointF(0f, 0f), SizeF(1f, 1f), true)

        playerViewModel.onLocalPause()

        binding.osd.addOsdItem(item)
    }

    private fun eventOnNextDisplay(track: Track) {
        binding.nextSong.apply {
            root.removeCallbacks(nextSongHiddenTask)
            name.text = track.name
            sn.text = track.sn
            singer.text = track.performer
            root.visibility = View.VISIBLE
            root.postDelayed(nextSongHiddenTask, 5000L)
        }
//            removeStatusOsd()
//
//            if (nextDisplayOsd != null) {
//                  binding.osd.removeOsdItem(nextDisplayOsd!!)
//                  nextDisplayOsd = null
//            }
//
//            val mBinding: OsdNextTrackBinding =
//                  OsdNextTrackBinding.inflate(layoutInflater, null, false)
//
//            mBinding.name.text = track.name
//            mBinding.sn.text = track.sn
//            mBinding.singer.text = track.performer
//
//
//            nextDisplayOsd = OSDViewItem(
//                  mBinding.root,
//                  5000,
//                  PointF(0.02f, 0.072f),
//                  SizeF(0.2f, 0.2f)
//            )
//
//            binding.osd.addOsdItem(nextDisplayOsd!!)
    }

    private fun addBarrageText(msg: String) {
        val item: OSDItem = OSDBarrageItem(
            msg,
            OSDBarrageItem.Direction.RIGHT_TO_LEFT,
            1.2f,
            Color.WHITE,
            false,
            30000L,
            null,
            Pair(0.07f, 0.6f)
        )

        binding.osd.addOsdItem(item)
    }

    private fun showIdleTag() {
        Log.d(TAG, "Show Idle tag")
        binding.idleTag.visibility = View.VISIBLE
//        if (idleTag == null) {
//            idleTag = OSDViewItem(
//                OsdIdelTagBinding.inflate(layoutInflater, null, false).root,
//                -1,
//                PointF(0.01f, 0.01f),
//                SizeF(0.1f, 0.07f)
//            )
//
//            binding.osd.addOsdItem(idleTag!!)
//        }
//
//        idleTag!!.hidden = false
    }

    private fun hideIdleTag() {
        Log.d(TAG, "Hide Idle tag")
//        idleTag?.hidden = true
        binding.idleTag.visibility = View.INVISIBLE
    }

    private fun showMuteTag() {
        binding.muteTag.visibility = View.VISIBLE
//            Log.d(TAG, "Show Idle tag")
//
//            if (muteTag == null) {
//                  val mBinding: OsdIdelTagBinding =
//                        OsdIdelTagBinding.inflate(layoutInflater, null, false)
//                  mBinding.root.text = "靜音"
//
//                  muteTag = OSDViewItem(
//                        mBinding.root,
//                        -1,
//                        PointF(0.01f, 0.07f),
//                        SizeF(0.1f, 0.07f)
//                  )
//
//                  binding.osd.addOsdItem(muteTag!!)
//            }
//
//            muteTag!!.hidden = false
    }

    private fun hideMuteTag() {
        binding.muteTag.visibility = View.INVISIBLE
//            Log.d(TAG, "Hide Idle tag")
//            muteTag?.hidden = true
    }

    private fun generateCenterNotifyView(
        res: Int,
        msg: String,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_INSIDE
    ): View {
        val mBinding: OsdCenterNotificationBinding =
            OsdCenterNotificationBinding.inflate(layoutInflater, null, false)
        mBinding.icon.scaleType = scaleType
        mBinding.icon.setImageResource(res)
        mBinding.title.text = msg
        return mBinding.root
    }

    private fun generateTextView(msg: String): View {
        val mBinding: OsdTextBinding = OsdTextBinding.inflate(layoutInflater, null, false)

        mBinding.root.text = msg

        return mBinding.root
    }

    private fun removeStatusOsd() {
        if (statusOsd != null) {
            binding.osd.removeOsdItem(statusOsd!!)
            statusOsd = null
        }
    }

    private fun updateNotifyOsd(massage: String) {
        binding.notifyMsg.removeCallbacks(notifyMagHiddenTask)
        binding.notifyMsg.text = massage
        binding.notifyMsg.visibility = View.VISIBLE
        binding.notifyMsg.postDelayed(notifyMagHiddenTask, 2000L)
//            if (notifyOsd != null) {
//                  binding.osd.removeOsdItem(notifyOsd!!)
//                  notifyOsd = null
//            }
//
//            val view: View = generateTextView(massage)
//
//            notifyOsd =
//                  OSDViewItem(view, 2500, PointF(0.01f, 0.08f), SizeF(0.2f, 0.07f))
//
//            binding.osd.addOsdItem(notifyOsd!!)

    }

    private fun testFn(): OSDItem? {
        val inputStream: InputStream = requireContext().assets.open("anime/elephant.gif")

        var apngDrawable: ApngDrawable = ApngDrawable.Companion.decode(inputStream)

        return OSDApngItem(
            apngDrawable, 2,
            PointF(-1f, -1f), SizeF(0f, 0.7f)
        )
    }

    private val notifyMagHiddenTask = Runnable { binding.notifyMsg.visibility = View.INVISIBLE }

    private val eventHiddenTask = Runnable { binding.event.visibility = View.INVISIBLE }

    private val nextSongHiddenTask =
        Runnable { binding.nextSong.root.visibility = View.INVISIBLE }

    //end OSD    
    private val eventListener: InePlayerController.EventListen =
        object : InePlayerController.EventListen {
            override fun onPlayListChange(controller: InePlayerController?) {
                super.onPlayListChange(controller)
            }

            override fun onOrderSongFinish(controller: InePlayerController?) {
                super.onOrderSongFinish(controller)
                Log.d(TAG, "onOrderSongFinish")
            }

            override fun onStop(
                controller: InePlayerController?,
                Name: String?,
                isPublicVideo: Boolean
            ) {
                super.onStop(controller, Name, isPublicVideo)
                Log.d(TAG, "onStop : $Name, $isPublicVideo")

                if (!isPublicVideo) {
                    currantName = null
                    playerViewModel.onStop()
                }
                osdViewModel.onTypeChanged(!isPublicVideo)
            }

            override fun onNext(
                controller: InePlayerController?,
                Name: String?,
                isPublicVideo: Boolean
            ) {
                super.onNext(controller, Name, isPublicVideo)
                Log.d(TAG, "onNext : $Name, $isPublicVideo")
                tName = Name ?: "Null"
                if (!isPublicVideo) {
                    if (Name != currantName) {
                        currantName = Name
                        playerViewModel.onPlaying()
                    }
                }
                osdViewModel.onTypeChanged(isPublicVideo)
            }


            override fun onNextSongDisplay(
                controller: InePlayerController?,
                Name: String?
            ) {
                super.onNextSongDisplay(controller, Name)
                Log.d(TAG, "onNextSongDisplay : $Name")
                playerViewModel.onNextDisplay()
            }

            override fun onLoadingError(
                controller: InePlayerController?,
                Name: String?
            ) {
                super.onLoadingError(controller, Name)
                Log.d(TAG, "onLoadingError : $Name")
                if(currantName != null){
                    currantName = null
                    playerViewModel.onStop()
                }
                osdViewModel.onTypeChanged(true)
            }

            override fun onPlayingError(
                controller: InePlayerController?,
                Name: String?,
                Message: String?
            ) {
                super.onPlayingError(controller, Name, Message)
                Log.d(TAG, "onPlayingError : $Name, $Message")
                try {
                    if(currantName != null){
                        currantName = null
                        playerViewModel.onStop()
                    }
                    osdViewModel.onTypeChanged(true)

                    Toast.makeText(
                        requireContext(),
                        "$Name : $Message",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onAudioChannelMappingChanged(
                controller: InePlayerController?,
                type: Int
            ) {
                super.onAudioChannelMappingChanged(controller, type)
                Log.d(TAG, "onAudioChannelMappingChanged : $type")
            }
        }

    private inner class ResetTimerTask : TimerTask() {
        override fun run() {
            reset()
        }
    }


}