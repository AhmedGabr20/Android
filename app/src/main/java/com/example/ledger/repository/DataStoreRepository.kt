package com.example.ledger.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("backup_prefs")

class DataStoreRepository(private val context: Context) {
    private val lastPath = stringPreferencesKey("last_path")
    private val lastTime = longPreferencesKey("last_time")

    val lastExportInfo: Flow<Pair<String, Long>> = context.dataStore.data.map {
        (it[lastPath] ?: "-") to (it[lastTime] ?: 0L)
    }

    suspend fun setLastExport(path: String) {
        context.dataStore.edit { pref: MutablePreferences ->
            pref[lastPath] = path
            pref[lastTime] = System.currentTimeMillis()
        }
    }
}

typealias MutablePreferences = androidx.datastore.preferences.core.MutablePreferences
