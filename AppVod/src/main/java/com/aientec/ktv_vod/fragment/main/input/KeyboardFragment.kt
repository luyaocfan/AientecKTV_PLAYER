package com.aientec.ktv_vod.fragment.main.input

import android.content.res.XmlResourceParser
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.FragmentInputKeyboardBinding
import com.aientec.ktv_vod.databinding.ViewKeyboardKeyBinding
import com.aientec.ktv_vod.databinding.ViewKeyboardRowBinding
import com.aientec.ktv_vod.module.Repository
import com.aientec.ktv_vod.viewmodel.TrackViewModel
import com.aientec.ktv_vod.viewmodel.UiViewModel

class KeyboardFragment : Fragment() {
    private lateinit var binding: FragmentInputKeyboardBinding

    private val trackViewModel: TrackViewModel by activityViewModels()

    private val keyMap: HashMap<String, ViewKeyboardKeyBinding> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputKeyboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        trackViewModel.inputType.observe(viewLifecycleOwner) {
            when (it) {
                Repository.InputType.PHONETIC -> updateKeyboardFromXml(R.xml.keyboard_phonetic)
                Repository.InputType.SPELLING -> updateKeyboardFromXml(R.xml.keyboard_spell)
                Repository.InputType.OTHER -> TODO()
            }
            binding.root.invalidate()
            trackViewModel.updateSearchKeys("")
        }


        trackViewModel.searchingKeys.observe(viewLifecycleOwner) {
            if (it == null) return@observe

//            Log.d("Trace", "Search keys update : ${it.size}")

            for (key in keyMap.keys) {
                if (it.contains(key))
                    keyMap[key]!!.root.visibility = View.VISIBLE
                else
                    keyMap[key]!!.root.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateKeyboardFromXml(xmlRes: Int) {
        binding.root.removeAllViews()
        keyMap.clear()

        val p: XmlResourceParser = resources.getXml(xmlRes)

        var event: Int = p.eventType

        var rowGroup: LinearLayout? = null

        while (p.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
            when (event) {
                XmlResourceParser.START_TAG -> {
//                    Log.d("Trace", "START Tag : ${p.name}")

                    when (p.name) {
                        "Row" -> {
                            rowGroup =
                                ViewKeyboardRowBinding.inflate(
                                    layoutInflater,
                                    binding.root,
                                    true
                                ).root
                        }
                        "Key" -> {
                            ViewKeyboardKeyBinding.inflate(layoutInflater, rowGroup, true)
                                .apply {
                                    for (i in 0 until p.attributeCount) {
//                                        Log.d("Trace", "Attr : ${p.getAttributeName(i)}")
                                        when (p.getAttributeName(i)) {
                                            "keyLabel" -> root.text = p.getAttributeValue(i)
                                            "codes" -> root.setTag(
                                                R.id.tag_codes,
                                                p.getAttributeIntValue(i, -1)
                                            )
                                            "keyOutputText" -> root.setTag(
                                                R.id.tag_output,
                                                p.getAttributeValue(i)
                                            )
                                        }
                                    }
                                    if ((root.getTag(R.id.tag_codes) as Int) > 0)
                                        keyMap[root.text.toString()] = this
                                    root.setOnClickListener(onKeyClickListener)
                                }
                        }
                    }
                }
            }
        }
    }

    private val onKeyClickListener: View.OnClickListener = View.OnClickListener {
        val code: Int = it.getTag(R.id.tag_codes) as Int ?: return@OnClickListener

        when (code) {
            -21 -> trackViewModel.onKeyBack()
            -22 -> trackViewModel.onKeyClean()
            else -> {
                val out: String = it.getTag(R.id.tag_output) as String ?: return@OnClickListener
                trackViewModel.onKeyInput(out)
            }
        }
    }
}