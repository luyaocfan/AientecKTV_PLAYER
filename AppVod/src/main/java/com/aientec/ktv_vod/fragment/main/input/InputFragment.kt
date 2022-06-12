package com.aientec.ktv_vod.fragment.main.input

import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentInputBinding
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel

class InputFragment : Fragment() {
    private lateinit var binding: FragmentInputBinding

    private val trackViewModel: TrackViewModel by activityViewModels()

    private val uiViewModel: UiViewModel by activityViewModels()

    private val phoneticChars: String = "ㄅㄉㄓㄚㄞㄢㄦㄆㄊㄍㄐㄔㄗㄧㄛㄟㄣㄇㄋㄎㄑㄕㄘㄨㄜㄠㄤㄈㄌㄏㄒㄖㄙㄩㄝㄡㄥ"

    private lateinit var phoneticKeyboard: Keyboard

    private lateinit var spellKeyboard: Keyboard

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        phoneticKeyboard = Keyboard(requireContext(), R.xml.keyboard_template, phoneticChars, -1, 0)

        spellKeyboard = Keyboard(requireContext(), R.xml.keyboard_spell)

        binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.keyboard.apply {
            isEnabled = true
            isPreviewEnabled = false
            setOnKeyboardActionListener(keyboardActionListener)
        }

        binding.inputSelector.setOnCheckedChangeListener { _, checkedId ->
            val inputType: Repository.InputType = when (checkedId) {
                R.id.phonetic -> Repository.InputType.PHONETIC
                R.id.spell -> Repository.InputType.SPELLING
                R.id.other -> Repository.InputType.OTHER
                else -> Repository.InputType.OTHER
            }

            trackViewModel.onInputTypeChange(inputType)
        }

        trackViewModel.searchingText.observe(viewLifecycleOwner, {
            binding.display.text = it ?: ""
        })

        trackViewModel.inputType.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            binding.keyboard.keyboard = when (it) {
                Repository.InputType.PHONETIC -> phoneticKeyboard
                Repository.InputType.SPELLING -> spellKeyboard
                Repository.InputType.OTHER -> Keyboard(requireContext(), R.xml.keyboard_other)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.phonetic.isChecked = true
    }

    private val keyboardActionListener: KeyboardView.OnKeyboardActionListener =
        object : KeyboardView.OnKeyboardActionListener {


            override fun onPress(primaryCode: Int) {
                Log.d("Trace", "OnPress : $primaryCode")
            }

            override fun onRelease(primaryCode: Int) {
                Log.d("Trace", "onRelease : $primaryCode")
            }

            override fun onText(text: CharSequence?) {
                Log.d("Trace", "onText : $text")
                trackViewModel.onKeyInput(text.toString())
            }

            override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
                Log.d("Trace", "onKey : $primaryCode, KeyCodes : ${keyCodes.toString()}")
                if (primaryCode == -21) {
                    trackViewModel.onKeyBack()
                }
                if (primaryCode == -22) {
                    trackViewModel.onKeyClean()
                }
            }


            override fun swipeLeft() {
                Log.d("Trace", "swipeLeft")
            }

            override fun swipeRight() {
                Log.d("Trace", "swipeRight")
            }

            override fun swipeDown() {
                Log.d("Trace", "swipeDown")
            }

            override fun swipeUp() {
                Log.d("Trace", "swipeUp")
            }
        }
}