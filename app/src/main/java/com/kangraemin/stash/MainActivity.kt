package com.kangraemin.stash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kangraemin.stash.features.home.PlaceholderScreen
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StashTheme {
                val backStack = rememberSaveableBackStack(root = PlaceholderScreen)
                val navigator = rememberCircuitNavigator(backStack)
                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(navigator = navigator, backStack = backStack)
                }
            }
        }
    }
}
