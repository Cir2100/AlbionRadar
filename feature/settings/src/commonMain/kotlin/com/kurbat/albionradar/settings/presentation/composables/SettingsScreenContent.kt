package com.kurbat.albionradar.settings.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.settings.presentation.SettingsViewModel
import com.kurbat.albionradar.settings.presentation.composables.sections.DebugSection
import com.kurbat.albionradar.settings.presentation.composables.sections.DisplaySection
import com.kurbat.albionradar.settings.presentation.composables.sections.FilterSection
import com.kurbat.albionradar.settings.presentation.composables.sections.NetworkSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    viewModel: SettingsViewModel,
    isDebugBuild: Boolean,
    onBack: () -> Unit
) {
    val config by viewModel.currentConfig.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val captureStats by viewModel.captureStats.collectAsState()

    val tabs = remember(isDebugBuild) {
        buildList {
            add("Network")
            add("Filters")
            add("Display")
            if (isDebugBuild) add("Debug")
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            viewModel.cancel(onBack)
                        } else {
                            onBack()
                        }
                    }) {
                        Text("←")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasUnsavedChanges) {
                        Text(
                            "Unsaved changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextButton(
                        onClick = { viewModel.reset() }
                    ) {
                        Text("Reset to Default")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.cancel(onBack) }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.save(onBack) },
                        enabled = hasUnsavedChanges
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (tabs.getOrNull(selectedTab)) {
                    "Network" -> NetworkSection(
                        adapter = config.adapter,
                        onAdapterChange = { viewModel.updateAdapter(it) }
                    )

                    "Filters" -> FilterSection(
                        filter = config.filter,
                        onFilterChange = { viewModel.updateFilter(it) }
                    )

                    "Display" -> DisplaySection(
                        display = config.display,
                        onDisplayChange = { viewModel.updateDisplay(it) }
                    )

                    "Debug" -> DebugSection(
                        captureStats = captureStats,
                        capture = config.capture,
                        onCaptureChange = { viewModel.updateCapture(it) }
                    )
                }
            }
        }
    }
}
