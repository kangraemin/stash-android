package com.kangraemin.stash.domain.repository

import com.kangraemin.stash.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
