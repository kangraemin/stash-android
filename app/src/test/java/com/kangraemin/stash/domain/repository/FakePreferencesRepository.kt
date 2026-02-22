package com.kangraemin.stash.domain.repository

import com.kangraemin.stash.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository(
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
) : PreferencesRepository {

    private val _themeMode = MutableStateFlow(initialThemeMode)

    override fun getThemeMode(): Flow<ThemeMode> = _themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}
