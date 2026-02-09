package com.dynapharm.owner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.presentation.navigation.NavGraph
import com.dynapharm.owner.presentation.theme.OwnerHubTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point for the Dynapharm Owner Hub app.
 * Sets up the navigation graph with Login as the start destination.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OwnerHubTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
