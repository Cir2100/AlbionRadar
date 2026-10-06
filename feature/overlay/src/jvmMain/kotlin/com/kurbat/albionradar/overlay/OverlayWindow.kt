package com.kurbat.albionradar.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.kurbat.albionradar.overlay.presentation.OverlayViewModel
import com.kurbat.albionradar.overlay.presentation.composables.MinimapOverlay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverlayWindow(
    showOverlay: MutableState<Boolean> // todo
) {
    if (showOverlay.value) {
        Window(
            onCloseRequest = { showOverlay.value = false },
            visible = true,
            transparent = true,
            undecorated = true,
            alwaysOnTop = true,
            resizable = false,
            state = rememberWindowState(
                position = WindowPosition(300.dp, 300.dp),
                size = DpSize(300.dp, 300.dp)
            )
        ) {
            LaunchedEffect(Unit) {
                try {
//                        WindowUtils.makeWindowClickThrough(window)
                } catch (e: Exception) {
                    println("Warning: Could not make window click-through: ${e.message}")
                }
            }

            val overlayViewModel = koinViewModel<OverlayViewModel>()
            MinimapOverlay(overlayViewModel)
        }
    }
}
