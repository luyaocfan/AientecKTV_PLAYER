package com.aientec.appplayer.fragment

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.appplayer.R
import com.aientec.appplayer.data.EventBundle
import com.aientec.appplayer.data.MTVEvent
import com.aientec.appplayer.data.MessageBundle
import com.aientec.appplayer.databinding.*
import com.aientec.appplayer.viewmodel.*
import com.aientec.structure.Track
import com.linecorp.apng.ApngDrawable
import idv.bruce.ui.osd.items.OSDBarrageItem
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OsdEventListener
import idv.bruce.ui.osd.items.OSDViewItem
import idv.bruce.ui.osd.items.*
import java.io.File
import java.io.InputStream
import java.util.*

class OsdFragment : Fragment() {
      companion object {
            const val TAG: String = "OsdFrag"
      }

      private lateinit var binding: FragmentOsdBinding

      private val playerViewMode: PlayerViewModel by activityViewModels()

      private val osdViewModel: OsdViewModel by activityViewModels()

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

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentOsdBinding.inflate(inflater, container, false)

            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            playerViewMode.mtvEvent.observe(viewLifecycleOwner) {
                  val bundle: EventBundle = it ?: return@observe
                  onPlayerEvent(bundle)
            }

            osdViewModel.message?.let {
                  it.observe(viewLifecycleOwner) { bundle ->
//                        onMessageEvent(bundle)
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

//            binding.osd.eventListener = object : OsdEventListener {
//                  override fun onDone(item: OSDItem) {
//                        Log.d(TAG, "OnOsdDne")
//                        if (item is OSDVideoItem)
//                              playerViewMode.onLocalPlay()
//                        item.release()
//                  }
//
//                  override fun onContainerReady() {
//                        showIdleTag()
//                  }
//
//                  override fun onContainerSizeChanged(width: Int, height: Int) {
//
//                  }
//            }

      }

      @SuppressLint("ResourceType")
//      private fun onMessageEvent(messageBundle: MessageBundle) {
//
//            val item: OSDItem? = when (messageBundle.type) {
//
//                  MessageBundle.Type.TEST -> {
//                        testFn()
//                  }
//                  MessageBundle.Type.TXT -> {
//                        OSDBarrageItem(
//                              " ${messageBundle.data as String}",
//                              OSDBarrageItem.Direction.RIGHT_TO_LEFT,
//                              2.0f,
//                              Color.WHITE,
//                              false,
//                              20000L,
//                              null,
//                              Pair(0.07f, 0.6f)
//                        )
//                  }
//                  MessageBundle.Type.IMAGE -> {
//                        OSDPictureItem(
//                              messageBundle.data as String, 5000, PointF(-1f, -1f),
//                              SizeF(0.5f, 0f)
//                        )
//                  }
//
//                  MessageBundle.Type.NONE -> {
//                        null
//                  }
//                  MessageBundle.Type.VIDEO -> {
//                        videoOsd = OSDVideoItem(
//                              requireContext(),
//                              messageBundle.data as String,
//                              PointF(-1f, -1f),
//                              SizeF(0f, 0.5f)
//                        )
//                        playerViewMode.onLocalPause()
//                        videoOsd
////                if (videoOsd == null) {
////                    videoOsd = OSDVideoItem(
////                        requireContext(),
////                        messageBundle.data as String,
////                        PointF(-1f, -1f),
////                        SizeF(0f, 0.5f)
////                    )
////                    playerViewMode.onLocalPause()
////                    videoOsd
////                } else {
////                    videoQueue.push(
////                        OSDVideoItem(
////                            requireContext(),
////                            messageBundle.data as String,
////                            PointF(-1f, -1f),
////                            SizeF(0f, 0.5f)
////                        )
////                    )
////                    null
////                }
//                  }
//                  MessageBundle.Type.EMOJI -> {
//                        animationOsd = OSDApngItem(
//                              messageBundle.data as ApngDrawable, 2,
//                              PointF(-1f, -1f), SizeF(0f, 0.7f)
//                        )
//                        animationOsd
////                if (animationOsd == null) {
////                    animationOsd = OSDApngItem(
////                        messageBundle.data as ApngDrawable, 2,
////                        PointF(-1f, -1f), SizeF(0f, 0.7f)
////                    )
////                    animationOsd
////                } else {
////                    animationQueue.push(
////                        OSDApngItem(
////                            messageBundle.data as ApngDrawable, 2,
////                            PointF(-1f, -1f), SizeF(0f, 0.7f)
////                        )
////                    )
////                    null
////                }
//                  }
//                  MessageBundle.Type.VOD -> {
//                        updateNotifyOsd(messageBundle.data.toString())
//                        null
//                  }
//            }
//
//            binding.osd.addOsdItem(item ?: return)
//      }

      private fun onPlayerEvent(bundle: EventBundle) {
            Log.d(TAG, "OnEvent : ${bundle.event}")

            val item: OSDItem? = when (bundle.event) {
                  MTVEvent.ON_PAUSE -> eventOnPause()

                  MTVEvent.ON_RESUME -> eventOnResume()

                  MTVEvent.ON_STOP -> {
                        if (scoreTag != null) {
                              playerViewMode.onLocalPause()
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

//            binding.osd.addOsdItem(item ?: return)
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
//                  binding.osd.removeOsdItem(scoreTag!!)
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

            playerViewMode.onLocalPause()

//            binding.osd.addOsdItem(item)
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

//            binding.osd.addOsdItem(item)
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
//                  binding.osd.removeOsdItem(statusOsd!!)
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

}