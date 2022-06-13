package com.aientec.ktv_vod.fragment.main

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.*
import com.aientec.ktv_vod.module.Environmental
import com.aientec.ktv_vod.viewmodel.ControlViewModel
import com.aientec.ktv_vod.viewmodel.SystemViewModel
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.bumptech.glide.Glide
import com.king.view.arcseekbar.ArcSeekBar
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.*

class ControlBarFragment : Fragment() {
      private lateinit var binding: FragmentControlBarBinding

      private val controlViewModel: ControlViewModel by activityViewModels()

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val systemViewModel: SystemViewModel by activityViewModels()

      private var showPopup: SimpleTooltip? = null

      private lateinit var settingContentBinding: ViewSettingContentBinding

      private lateinit var micControlBinding: PopMicValueControlBinding

      private lateinit var musicControlBinding: PopMusicValueControlBinding

      private lateinit var appQrCodeBinding: PopQrcodeBinding

      private var isPubVideo: Boolean = true

      private var toast: Toast? = null

      private var startTime: Long = 0L

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentControlBarBinding.inflate(inflater, container, false)

            settingContentBinding = ViewSettingContentBinding.inflate(inflater)

            micControlBinding = PopMicValueControlBinding.inflate(inflater)

            musicControlBinding = PopMusicValueControlBinding.inflate(inflater)

            appQrCodeBinding = PopQrcodeBinding.inflate(inflater)

            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            initActions(binding.buttons)
            initPopupWindow()

            systemViewModel.qrCodeData?.let {
                  it.observe(viewLifecycleOwner) { data ->
                        if (data != null)
                              appQrCodeBinding.qrcode.setImageBitmap(BitmapFactory.decodeFile(data))
                  }
            }


            controlViewModel.commandSendState.observe(viewLifecycleOwner) {
                  if (it == null) return@observe

                  val res: Int =
                        if (it) R.string.toast_command_success else R.string.toast_command_fail

                  if (toast != null) {
                        toast!!.cancel()
                  }

                  toast = Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT)
//                  toast!!.show()
            }

            controlViewModel.playState?.let {
                  it.observe(viewLifecycleOwner) { isPlay ->
                        if (isPlay == null) return@observe
                        val res: Pair<Int, Int> =
                              if (isPlay)
                                    Pair(R.string.label_music_pause, R.drawable.ic_control_pause)
                              else
                                    Pair(R.string.label_music_play, R.drawable.ic_control_play)

                        binding.root.findViewWithTag<Button>("ACTION:PAUSE").apply {
                              setText(res.first)
                              setCompoundDrawablesWithIntrinsicBounds(0, res.second, 0, 0)
                        }
                  }
            }

            controlViewModel.vocalType?.let {
                  it.observe(viewLifecycleOwner) { type ->
                        val res: Pair<Int, Int> = when (type) {
                              Environmental.VocalType.ORIGINAL -> Pair(
                                    R.string.label_music_original_vocal,
                                    R.drawable.ic_control_vocal_original
                              )
                              Environmental.VocalType.BACKING -> Pair(
                                    R.string.label_music_backing_vocal,
                                    R.drawable.ic_control_vocal_backing
                              )
                              Environmental.VocalType.GUIDE -> Pair(
                                    R.string.label_music_guide_vocal,
                                    R.drawable.ic_control_vocal_guide
                              )
                              null -> return@observe
                        }
                        binding.root.findViewWithTag<Button>("ACTION:VOCAL").apply {
                              setText(res.first)
                              setCompoundDrawablesWithIntrinsicBounds(0, res.second, 0, 0)
                        }
                  }
            }

            trackViewModel.currantPlayTrack.observe(viewLifecycleOwner) {
                  if (it == null) {
                        binding.currantDisplay.visibility = View.INVISIBLE
                        binding.emptyPlayInfo.visibility = View.VISIBLE
                        isPubVideo = true
                  } else {
                        startTime = System.currentTimeMillis()
                        isPubVideo = false
                        binding.emptyPlayInfo.visibility = View.GONE
                        binding.currantDisplay.visibility = View.VISIBLE
                        binding.currantTrackImg.visibility = View.VISIBLE
                        binding.currantTackAlbum.text = it.performer
                        binding.currantTrackName.text = it.name
                  }
            }

            trackViewModel.prepareTrack.observe(viewLifecycleOwner) {
                  if (it == null) {
                        binding.prepareDisplay.visibility = View.INVISIBLE
                  } else {
                        binding.prepareDisplay.visibility = View.VISIBLE
                        binding.prepareTrackName.text = it.name
                  }
            }

//            if (BuildConfig.DEBUG) {
//                  binding.emptyPlayInfo.visibility = View.GONE
//                  binding.currantDisplay.visibility = View.VISIBLE
//                  binding.prepareDisplay.visibility = View.VISIBLE
//            }


            controlViewModel.mainMode.observe(viewLifecycleOwner) {
                  val res = when (it) {
                        1 -> R.id.main_mode_1
                        2 -> R.id.main_mode_2
                        3 -> R.id.main_mode_3
                        4 -> R.id.main_mode_4
                        5 -> R.id.main_mode_5
                        else -> -1
                  }
                  settingContentBinding.mainModel.check(res)
            }

            controlViewModel.subMode.observe(viewLifecycleOwner) {
                  val res: Pair<RadioGroup, Int>? = when (it) {
                        10 -> Pair(settingContentBinding.subMode1, R.id.sub_mode_1_1)
                        11 -> Pair(settingContentBinding.subMode1, R.id.sub_mode_1_2)
                        12 -> Pair(settingContentBinding.subMode1, R.id.sub_mode_1_3)
                        13 -> Pair(settingContentBinding.subMode1, R.id.sub_mode_1_4)
                        14 -> Pair(settingContentBinding.subMode2, R.id.sub_mode_2_1)
                        15 -> Pair(settingContentBinding.subMode2, R.id.sub_mode_2_2)
                        16 -> Pair(settingContentBinding.subMode2, R.id.sub_mode_2_3)
                        17 -> Pair(settingContentBinding.subMode2, R.id.sub_mode_2_4)
                        else -> null
                  }
                  if (res == null) {
                        settingContentBinding.subMode1.clearCheck()
                        settingContentBinding.subMode2.clearCheck()
                  } else {
                        res!!.first.check(res.second)
                  }
            }

            controlViewModel.basicLight.observe(viewLifecycleOwner) {
                  val res = when (it) {
                        6 -> R.id.basic_light_1
                        7 -> R.id.basic_light_2
                        8 -> R.id.basic_light_3
                        9 -> R.id.basic_light_4
                        else -> -1
                  }
                  settingContentBinding.basicLight.check(res)
            }

            controlViewModel.musicVolume.observe(viewLifecycleOwner) {
                  musicControlBinding.volume.progress.progress = it
            }

            controlViewModel.micVolume.observe(viewLifecycleOwner) {
                  micControlBinding.volume.progress.progress = it
            }

            controlViewModel.micEffectVolume.observe(viewLifecycleOwner) {
                  micControlBinding.effect.progress.progress = it
            }

            controlViewModel.micEffectMode.observe(viewLifecycleOwner) {
                  Log.d("Trace", "Effect mode")
                  try {
                        micControlBinding.mode.setSelection(it - 1)
                  } catch (e: Exception) {
                        e.printStackTrace()
                  }
            }

            controlViewModel.micToneVolume.observe(viewLifecycleOwner) {
                  micControlBinding.tone.progress.progress = it
            }
      }

      private fun initActions(viewGroup: ViewGroup) {
            val count: Int = viewGroup.childCount
            for (i in 0 until count) {
                  val v = viewGroup.getChildAt(i)
                  if (v is ViewGroup)
                        initActions(v)
                  else {

                        v.setOnClickListener {
                              Log.d(
                                    "Trace",
                                    "View size : ${binding.root.width}, ${binding.root.height}"
                              )

                              val tag: Any = it.tag ?: return@setOnClickListener

                              val action: String = tag as String

                              if (!action.startsWith("ACTION:")) return@setOnClickListener

                              when (action.removePrefix("ACTION:")) {
                                    "CUT" -> {
                                          if (isPubVideo) return@setOnClickListener
                                          if (System.currentTimeMillis() - startTime > 5000L)
                                                controlViewModel.cut()
                                          else
                                                Toast.makeText(
                                                      requireContext(),
                                                      R.string.hint_cut_locked,
                                                      Toast.LENGTH_SHORT
                                                ).show()
                                    }
                                    "PAUSE" -> {
                                          if (isPubVideo) return@setOnClickListener
                                          controlViewModel.playToggle()
                                    }
                                    "REPLAY" -> {
                                          if (isPubVideo) return@setOnClickListener
                                          controlViewModel.replay()
                                    }
                                    "VOCAL" -> {
                                          if (isPubVideo) return@setOnClickListener
                                          controlViewModel.vocalSwitch()
                                    }
                                    "SCORE" -> controlViewModel.scoreToggle()
                                    "SETTING" -> showPopupWindow(it, settingContentBinding.root)
                                    "MIC_CONTROL" -> showPopupWindow(it, micControlBinding.root)
                                    "MUSIC_CONTROL" -> showPopupWindow(it, musicControlBinding.root)
                                    "MIC_DEC" -> controlViewModel.micVolumeDesc()
                                    "MIC_ADD" -> controlViewModel.micVolumeAdd()
                                    "MUSIC_DEC" -> controlViewModel.musicVolumeDesc()
                                    "MUSIC_ADD" -> controlViewModel.musicVolumeAdd()
                                    "MUTE" -> controlViewModel.muteToggle()
                                    "QRCODE" -> showPopupWindow(it, appQrCodeBinding.root)
                              }
                        }
                  }
            }
      }

      private fun initPopupWindow() {

            initMicPop()

            initSettingPopup()

            initMusicPop()
      }

      private fun initMusicPop() {
            musicControlBinding.volume.apply {
                  title.setText(R.string.label_mic_volume)
                  progress.max = 29

                  dec.setOnClickListener {
                        controlViewModel.musicVolumeDesc()
                  }

                  add.setOnClickListener {
                        controlViewModel.musicVolumeAdd()
                  }
            }
      }

      private fun initMicPop() {
            micControlBinding.volume.apply {
                  title.setText(R.string.label_mic_volume)
                  progress.max = 29
                  add.setOnClickListener { controlViewModel.micVolumeAdd() }
                  dec.setOnClickListener { controlViewModel.micVolumeDesc() }
            }

            micControlBinding.effect.apply {
                  title.setText(R.string.label_mic_echo)
                  progress.max = 29
                  add.setOnClickListener { controlViewModel.micEffectAdd() }
                  dec.setOnClickListener { controlViewModel.micEffectDesc() }
            }

            micControlBinding.tone.apply {
                  title.setText(R.string.label_mic_tone)
                  progress.max = 14
                  add.setOnClickListener { controlViewModel.micModulateAdd() }
                  dec.setOnClickListener { controlViewModel.micModulateDesc() }
            }
//            micControlBinding.mode.setSelection(0)
            micControlBinding.mode.onItemSelectedListener =
                  object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                              parent: AdapterView<*>?,
                              view: View?,
                              position: Int,
                              id: Long
                        ) {
                              controlViewModel.onMicModeSelected(position + 1)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                              controlViewModel.onMicModeSelected(-1)
                        }
                  }
      }

      private fun initSettingPopup() {
            settingContentBinding.subMode1.setOnCheckedChangeListener(subModeCheckChangedListener)
            settingContentBinding.subMode2.setOnCheckedChangeListener(subModeCheckChangedListener)

            settingContentBinding.tempLabel.text = "10\u00B0"
            settingContentBinding.tempProgress.setOnChangeListener(object :
                  ArcSeekBar.OnChangeListener {
                  private var value: Int = -1
                  override fun onStartTrackingTouch(isCanDrag: Boolean) {

                  }

                  override fun onProgressChanged(progress: Float, max: Float, fromUser: Boolean) {
                        value = progress.toInt() + 10
                        settingContentBinding.tempLabel.text = "$value\u00B0"
                  }

                  override fun onStopTrackingTouch(isCanDrag: Boolean) {
                        Log.d("Trace", "Sone")
                  }

                  override fun onSingleTapUp() {

                  }
            })

            settingContentBinding.add.setOnClickListener {
                  if (settingContentBinding.tempProgress.progress < settingContentBinding.tempProgress.max)
                        settingContentBinding.tempProgress.progress =
                              settingContentBinding.tempProgress.progress + 1
            }

            settingContentBinding.dec.setOnClickListener {
                  if (settingContentBinding.tempProgress.progress > 0)
                        settingContentBinding.tempProgress.progress =
                              settingContentBinding.tempProgress.progress - 1
            }

            settingContentBinding.close.setOnClickListener { closePopupWindow() }

            settingContentBinding.mainModel.setOnCheckedChangeListener { group, checkedId ->
                  settingContentBinding.subMode1.clearCheck()
                  settingContentBinding.subMode2.clearCheck()
                  val code = when (checkedId) {
                        R.id.main_mode_1 -> 1
                        R.id.main_mode_2 -> 2
                        R.id.main_mode_3 -> 3
                        R.id.main_mode_4 -> 4
                        R.id.main_mode_5 -> 5
                        else -> 1
                  }
                  try {
                        controlViewModel.onMainModeSelected(
                              code,
                              "${
                                    group.findViewById<RadioButton>(checkedId).text.toString()
                                          .replace("\n", "")
                              }模式"
                        )
                  } catch (e: Exception) {
                        e.printStackTrace()
                  }
            }

            settingContentBinding.subModeReset.setOnClickListener {
                  settingContentBinding.subMode1.clearCheck()
                  settingContentBinding.subMode2.clearCheck()
                  controlViewModel.onModeReset()
            }


            settingContentBinding.basicLight.setOnCheckedChangeListener { group, checkedId ->
                  val code: Int = when (checkedId) {
                        R.id.basic_light_1 -> 6
                        R.id.basic_light_2 -> 7
                        R.id.basic_light_3 -> 8
                        R.id.basic_light_4 -> 9
                        else -> 6
                  }
                  Log.d("Trace", "Code : $code")
                  try {
                        controlViewModel.onBasicLightSelected(
                              code,
                              "燈光 : ${group.findViewById<RadioButton>(checkedId).text}"
                        )
                  } catch (e: Exception) {
                        e.printStackTrace()
                  }
            }
            settingContentBinding.effect1.setOnTouchListener(EffectTouchEvent(18))

            settingContentBinding.effect2.setOnTouchListener(EffectTouchEvent(19))

            settingContentBinding.effect3.setOnTouchListener(EffectTouchEvent(20))

//            settingContentBinding.effect1.setOnClickListener {
//                  controlViewModel.onEffectSend(18)
//            }
//            settingContentBinding.effect2.setOnClickListener {
//                  controlViewModel.onEffectSend(19)
//            }
//            settingContentBinding.effect3.setOnClickListener {
//                  controlViewModel.onEffectSend(20)
//            }
      }

      private fun showPopupWindow(anchorView: View, contentView: View) {
            closePopupWindow()

            if (contentView.parent != null)
                  (contentView.parent as ViewGroup).removeView(contentView)

            showPopup = SimpleTooltip.Builder(requireContext())
                  .onShowListener {
                        if (anchorView is CompoundButton)
                              anchorView.isChecked = true
                  }
                  .onDismissListener {
                        if (anchorView is CompoundButton)
                              anchorView.isChecked = false
                  }
                  .modal(true)
                  .focusable(true)
                  .anchorView(anchorView)
                  .gravity(Gravity.TOP)
                  .contentView(contentView, 0)
                  .arrowColor(resources.getColor(R.color.accentPurple, null))
                  .dismissOnInsideTouch(false)
                  .build()

            showPopup!!.show()
      }

      private fun closePopupWindow() {
            if (showPopup != null && showPopup!!.isShowing) {
                  showPopup!!.dismiss()
                  showPopup = null
            }
      }

      private val subModeCheckChangedListener: RadioGroup.OnCheckedChangeListener =
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                  if (checkedId != -1 && settingContentBinding.root.findViewById<RadioButton>(
                              checkedId
                        ).isChecked
                  ) {
                        when (group) {
                              settingContentBinding.subMode1 -> settingContentBinding.subMode2.clearCheck()
                              settingContentBinding.subMode2 -> settingContentBinding.subMode1.clearCheck()
                        }
                        val code: Int = when (checkedId) {
                              R.id.sub_mode_1_1 -> 10
                              R.id.sub_mode_1_2 -> 11
                              R.id.sub_mode_1_3 -> 12
                              R.id.sub_mode_1_4 -> 13
                              R.id.sub_mode_2_1 -> 14
                              R.id.sub_mode_2_2 -> 15
                              R.id.sub_mode_2_3 -> 16
                              R.id.sub_mode_2_4 -> 17
                              else -> -1
                        }
                        controlViewModel.onSubModeSelected(
                              code,
                              "${group.findViewById<RadioButton>(checkedId).text.toString()}模式"
                        )
                  }
            }

      private inner class EffectTouchEvent(private val code: Int) : View.OnTouchListener {
            private var isPresses: Boolean = false

            private var timer: Timer? = null

            private var isRun: Boolean = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                  when (event!!.action) {
                        MotionEvent.ACTION_DOWN -> {
                              isPresses = true
                              if (timer == null) {
                                    timer = Timer()
                                    timer?.schedule(object : TimerTask() {
                                          override fun run() {
                                                if (isPresses) {
                                                      isRun = true
                                                      controlViewModel.onEffectSend(code, 2)
                                                }
                                          }
                                    }, 300L)
                              }
                        }
                        MotionEvent.ACTION_UP -> {
                              isPresses = false
                              if (isRun) {
                                    controlViewModel.onEffectSend(code, 3)
                                    isRun = false
                              } else {
                                    controlViewModel.onEffectSend(code, 1)
                              }
                              if (timer != null)
                                    timer?.cancel()
                              timer = null
                        }
                  }

                  return false
            }
      }
}