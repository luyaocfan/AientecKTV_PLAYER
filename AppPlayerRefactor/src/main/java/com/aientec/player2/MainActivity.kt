package com.aientec.player2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aientec.player2.ui.theme.AientecKTV_PLAYERTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AientecKTV_PLAYERTheme {
                // A surface container using the 'background' color from the theme
                MTVContainer()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AientecKTV_PLAYERTheme {
        MTVContainer()
    }
}