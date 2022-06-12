package idv.bruce.vod.component

import android.graphics.drawable.Drawable
import androidx.annotation.Px
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import idv.bruce.vod.R

@OptIn(ExperimentalUnitApi::class)
@Composable
fun StatusBarButton(
      label: String = "",
      icon: Int = -1,
      count: Int = 0,
      showCount: Boolean = false,
      onClick: () -> Unit
) {
      Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
      ) {
            if (icon != -1)
                  Image(painter = painterResource(id = icon), contentDescription = "")
            Text(text = label, color = Color.White)
      }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MyPreview() {
      StatusBarButton(label = "Test", icon = R.drawable.ic_launcher_foreground) {

      }
}