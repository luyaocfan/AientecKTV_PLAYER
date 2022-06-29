package com.aientec.player2.ui.componants

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aientec.player2.R
import com.aientec.player2.viewmodel.PlayerViewModel

@Composable
fun Prepare(viewModel: PlayerViewModel = PlayerViewModel()) {

    val mContext: Context = LocalContext.current

    LaunchedEffect(key1 = viewModel) {
        viewModel.systemInit(mContext)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(id = R.string.state_prepare),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 84.sp
        )
    }

}

@Composable
@Preview
fun PreparePreview() {
    Prepare()
}