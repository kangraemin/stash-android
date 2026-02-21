package com.kangraemin.stash.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kangraemin.stash.domain.model.ThemeMode
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(SettingsScreen::class, SingletonComponent::class)
@Composable
fun Settings(state: SettingsScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(SettingsScreen.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            ThemeModeSection(
                currentMode = state.themeMode,
                onModeSelected = { state.eventSink(SettingsScreen.Event.OnThemeModeChanged(it)) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            DataManagementSection(
                onDeleteAllClicked = { state.eventSink(SettingsScreen.Event.OnDeleteAllClicked) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppInfoSection()
        }
    }

    if (state.showDeleteAllDialog) {
        DeleteAllConfirmDialog(
            onConfirm = { state.eventSink(SettingsScreen.Event.OnDeleteAllConfirmed) },
            onDismiss = { state.eventSink(SettingsScreen.Event.OnDeleteAllDismissed) },
        )
    }
}

@Composable
private fun ThemeModeSection(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Text(
        text = "테마",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp).semantics { heading() },
    )
    ThemeMode.entries.forEach { mode ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(mode) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
            )
            Text(
                text = when (mode) {
                    ThemeMode.SYSTEM -> "시스템 설정"
                    ThemeMode.LIGHT -> "라이트 모드"
                    ThemeMode.DARK -> "다크 모드"
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun DataManagementSection(
    onDeleteAllClicked: () -> Unit,
) {
    Text(
        text = "데이터 관리",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp).semantics { heading() },
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDeleteAllClicked)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "모든 콘텐츠 삭제",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun AppInfoSection() {
    Text(
        text = "앱 정보",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp).semantics { heading() },
    )
    Text(
        text = "버전 1.0",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DeleteAllConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("모든 콘텐츠 삭제") },
        text = { Text("저장된 모든 콘텐츠가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    StashTheme {
        Settings(
            state = SettingsScreen.State(
                themeMode = ThemeMode.SYSTEM,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDeleteDialogPreview() {
    StashTheme {
        Settings(
            state = SettingsScreen.State(
                themeMode = ThemeMode.DARK,
                showDeleteAllDialog = true,
            ),
        )
    }
}
