package com.aientec.player2

import android.os.Bundle
import android.util.Log
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
}
