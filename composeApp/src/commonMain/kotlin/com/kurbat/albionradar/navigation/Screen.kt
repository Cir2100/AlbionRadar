package com.kurbat.albionradar.navigation

sealed class Screen {
    data object Radar : Screen()
    data object Settings : Screen()
}
