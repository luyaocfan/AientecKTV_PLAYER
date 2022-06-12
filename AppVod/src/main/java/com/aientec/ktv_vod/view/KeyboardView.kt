package com.aientec.ktv_vod.view

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.ViewKeyboardBinding
import com.aientec.ktv_vod.databinding.ViewKeyboardKeyBinding
import com.aientec.ktv_vod.databinding.ViewKeyboardRowBinding

class KeyboardView : FrameLayout {
      interface OnKeyInputListener {
            fun onEvent(text: String, code: Int)
      }

      private lateinit var binding: ViewKeyboardBinding

      private lateinit var layoutInflater: LayoutInflater

      private val keyMap: HashMap<String, ViewKeyboardKeyBinding> = HashMap()


      var onKeyInputListener: OnKeyInputListener? = null

      constructor(context: Context) : super(context) {
            initViews(context)
      }

      constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
            initViews(context)
      }

      constructor(context: Context, attributeSet: AttributeSet, defaultAttr: Int) : super(
            context,
            attributeSet,
            defaultAttr
      ) {
            initViews(context)
      }

      private fun initViews(context: Context) {
            layoutInflater = LayoutInflater.from(context)

            binding = ViewKeyboardBinding.inflate(layoutInflater, this, true)
      }

      fun filterKeys(visibleKeys: List<String>?) {
            if (visibleKeys != null) {
                  for (key in keyMap.keys) {
                        if (visibleKeys.contains(key))
                              keyMap[key]!!.root.visibility = View.VISIBLE
                        else
                              keyMap[key]!!.root.visibility = View.INVISIBLE
                  }
            } else {
                  for (key in keyMap.keys) {
                        keyMap[key]!!.root.visibility = VISIBLE
                  }
            }
      }

      fun setKeyType(type: Int) {
            val xmlRes = when (type) {
                  1 -> R.xml.keyboard_phonetic
                  2 -> R.xml.keyboard_spell
                  else -> R.xml.keyboard_other
            }

            binding.root.removeAllViews()
            keyMap.clear()

            Log.d("Trace", "Set key : $type")

            val p: XmlResourceParser = resources.getXml(xmlRes)

            var event: Int = p.eventType

            var rowGroup: LinearLayout? = null

            while (p.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
                  when (event) {
                        XmlResourceParser.START_TAG -> {
//

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
                                          ViewKeyboardKeyBinding.inflate(
                                                layoutInflater,
                                                rowGroup,
                                                true
                                          )
                                                .apply {
                                                      for (i in 0 until p.attributeCount) {
                                                            Log.d(
                                                                  "Trace",
                                                                  "Attr : ${p.getAttributeName(i)}"
                                                            )
                                                            when (p.getAttributeName(i)) {
                                                                  "keyLabel" -> root.text =
                                                                        p.getAttributeValue(i)
                                                                  "codes" -> root.setTag(
                                                                        R.id.tag_codes,
                                                                        p.getAttributeIntValue(
                                                                              i,
                                                                              -1
                                                                        )
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
            invalidate()
      }

      private val onKeyClickListener: View.OnClickListener = View.OnClickListener {
            val code: Int = it.getTag(R.id.tag_codes) as Int ?: return@OnClickListener

            val text: String = (it.getTag(R.id.tag_output) ?: "") as String

            onKeyInputListener?.onEvent(text, code)
      }
}