package com.aientec.player2.ui.componants

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.aientec.player2.R
import com.aientec.player2.ui.theme.AientecKTV_PLAYERTheme
import com.aientec.player2.viewmodel.PlayerViewModel

@Composable
fun DisplayContainer(viewModel : PlayerViewModel = PlayerViewModel()) {
//    OSDContainer(viewModel = viewModel)

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (idle, notify, mute, state, rating) = createRefs()

        val isIdle by viewModel.isIdle.observeAsState(initial = true)

        val notifyMsg by viewModel.notifyMessage.observeAsState(initial = null)

        val isMute by viewModel.isMute.observeAsState(initial = false)

        val playerState by viewModel.playerState.observeAsState(initial = -1)

        val ratingState by viewModel.ratingState.observeAsState(initial = -1)

        Text(
            text = "公播", fontSize = 48.sp, color = Color.White, modifier = Modifier
                .constrainAs(idle) {
                    top.linkTo(parent.top, margin = 8.dp)
                    start.linkTo(parent.start, margin = 8.dp)
                }
                .background(
                    Color(0f, 0f, 0f, if (isIdle) 0.5f else 0f)
                )
                .padding(24.dp, 4.dp)
                .alpha(if (isIdle) 1f else 0f)
        )

        Text(
            text = notifyMsg.toString(), fontSize = 48.sp, color = Color.White,
            modifier = Modifier
                .constrainAs(notify) {
                    top.linkTo(idle.bottom, margin = 8.dp)
                    start.linkTo(idle.start)
                }
                .fillMaxWidth(0.20f)
                .padding(0.dp, 4.dp)
                .background(
                    Color(0f, 0f, 0f, if (notifyMsg != null) 0.5f else 0f)
                )
                .padding(24.dp, 4.dp)
                .alpha(if (notifyMsg != null) 1f else 0f),
        )

        if (isMute) {
            Text(text = "靜音", fontSize = 48.sp, color = Color.White, modifier = Modifier
                .constrainAs(mute) {
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }
                .background(
                    Color(0f, 0f, 0f, if (isMute) 0.5f else 0f)
                )
                .padding(24.dp, 4.dp)
                .alpha(if (isMute) 1f else 0f))
        }

        if (playerState != PlayerViewModel.PLAYER_STATE_NONE) {
            val stateRes : Pair<Int, Int>? = when (playerState) {
                PlayerViewModel.PLAYER_STATE_RESUME -> Pair(
                    R.drawable.ic_resume,
                    R.string.player_state_resume
                )
                PlayerViewModel.PLAYER_STATE_PAUSE -> Pair(
                    R.drawable.ic_pause,
                    R.string.player_state_pause
                )
                PlayerViewModel.PLAYER_STATE_CUT -> Pair(
                    R.drawable.ic_cut,
                    R.string.player_state_cut
                )
                PlayerViewModel.PLAYER_STATE_REPLAY -> Pair(
                    R.drawable.ic_replay,
                    R.string.player_state_replay
                )
                else -> null
            }
            Column(modifier = Modifier
                .constrainAs(state) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(0.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = stateRes!!.first),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = stringResource(id = stateRes!!.second),
                    fontSize = 48.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (ratingState != -1) {
            Image(
                painter = painterResource(if (ratingState == 0) R.drawable.img_start_score else R.drawable.img_on_score),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(rating) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .fillMaxWidth(0.2f),
                contentScale = ContentScale.FillWidth
            )
        }
    }


}

@Composable
@Preview(backgroundColor = 0xFF000000L, device = Devices.AUTOMOTIVE_1024p)
fun mPreview() {
    AientecKTV_PLAYERTheme {
        DisplayContainer()
    }
}