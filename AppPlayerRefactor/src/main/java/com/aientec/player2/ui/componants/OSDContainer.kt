package com.aientec.player2.ui.componants

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.aientec.player2.data.MessageBundle
import com.aientec.player2.viewmodel.PlayerViewModel
import idv.bruce.ui.osd.container.OSDContainerView
import idv.bruce.ui.osd.items.OSDBarrageItem

@Composable
fun OSDContainer(viewModel: PlayerViewModel) {
    val mLifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val osdMessage by viewModel.osdMessage.observeAsState()

//    if (osdMessage != null) {
//        when (osdMessage!!.type) {
//
//
//            MessageBundle.Type.IMAGE -> {
//
//                val bitmap = BitmapFactory.decodeFile(osdMessage!!.data as String)
//                Image(
//                    bitmap = bitmap.asImageBitmap(),
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxWidth(0.3f),
//                    contentScale = ContentScale.FillWidth,
//
//                )
//            }
//            MessageBundle.Type.VIDEO -> {}
//            MessageBundle.Type.EMOJI -> {}
//        }
//    }


    AndroidView(factory = {
        OSDContainerView(it).apply {
            viewModel.osdMessage.observe(mLifecycleOwner) { msg ->
                if (msg != null) {
                    when (msg.type) {
                        MessageBundle.Type.TXT -> {
                            OSDBarrageItem(
                                " ${msg.data as String}",
                                OSDBarrageItem.Direction.RIGHT_TO_LEFT,
                                2.0f,
                                Color.WHITE,
                                false,
                                20000L,
                                null,
                                Pair(0.07f, 0.6f)
                            )
                        }
                        MessageBundle.Type.IMAGE -> {}
                        MessageBundle.Type.VIDEO -> {}
                        MessageBundle.Type.EMOJI -> {}
                        MessageBundle.Type.VOD -> {}
                        else -> {}
                    }
                }
            }
        }
    }, modifier = Modifier.fillMaxSize())


}