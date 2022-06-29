package com.aientec.player2

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aientec.player2.ui.componants.MTVContainer
import com.aientec.player2.ui.componants.Prepare
import com.aientec.player2.ui.theme.AientecKTV_PLAYERTheme
import com.aientec.player2.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        setContent {
            AientecKTV_PLAYERTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "mtv") {
                    composable("prepare") { Prepare(viewModel) }
                    composable("mtv") { MTVContainer(viewModel) }
                }

                viewModel.dataSyn.observe(this) {
                    if (it)
                        navController.navigate("mtv")
                    else
                        navController.navigate("prepare")
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


