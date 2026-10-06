package com.kurbat.albionradar.di

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication

@Composable
fun InjectedApplication(content: @Composable () -> Unit) {
    KoinApplication(
        application = { modules(appModule) },
        content = content
    )
}
