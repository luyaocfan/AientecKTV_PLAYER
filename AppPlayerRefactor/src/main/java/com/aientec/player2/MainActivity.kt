package com.aientec.player2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aientec.player2.ui.componants.MTVContainer
import com.aientec.player2.ui.componants.OSDContainer
import com.aientec.player2.ui.componants.Prepare
import com.aientec.player2.ui.theme.AientecKTV_PLAYERTheme
import com.aientec.player2.ui.theme.Purple200
import com.aientec.player2.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()


        setContent {
            AientecKTV_PLAYERTheme {
                val navController = rememberNavController()

                val connectState by viewModel.isConnected.observeAsState(initial = true)

                LaunchedEffect(LocalContext.current) {
                    viewModel.dataSyn.observe(this@MainActivity) {
                        if (it)
                            navController.navigate("mtv"){popUpTo("mtv")
                                launchSingleTop=true
                            }
                        else
                            navController.navigate("prepare"){popUpTo("prepare")
                                launchSingleTop=true}
                    }

                    viewModel.toastMsg.observe(this@MainActivity) {
                        if (it != null) {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }


                NavHost(navController = navController, startDestination = "mtv") {
                    composable("prepare") { Prepare(viewModel) }
                    composable("mtv") { MTVContainer(viewModel) }
                }

                OSDContainer(viewModel)


                if (!connectState) {
                    Dialog(
                        onDismissRequest = { },
                        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(Color.Black)
                                .border(width = 1.dp, color = Purple200)
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator()
                                Text(text = "服務器連線中...", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
//            val windowInsetsController =
//                  ViewCompat.getWindowInsetsController(window.decorView) ?: return
//            // Configure the behavior of the hidden system bars
//            windowInsetsController.systemBarsBehavior =
//                  WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            // Hide both the status bar and the navigation bar
//            windowInsetsController.isAppearanceLightNavigationBars = false
//            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}


