package com.kurbat.albionradar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import com.kurbat.albionradar.di.InjectedApplication
import com.kurbat.albionradar.overlay.OverlayWindow

fun main() = application {
    InjectedApplication {
        val showOverlay = remember { mutableStateOf(false) }

        AppWindow(
            showOverlay = showOverlay
        )

        OverlayWindow(
            showOverlay = showOverlay
        )
    }
}
