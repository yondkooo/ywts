package com.example.ywts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

/** Харуулах бүртгэлийн төрөл: ALL, INCOME, EXPENSE */
class SettingsStore(private val context: Context) {
    private val key = stringPreferencesKey("filter")

    val filter: Flow<String> = context.dataStore.data.map { it[key] ?: ALL }

    private fun catKey(type: String) = stringPreferencesKey("cat_$type")

    /** Хэрэглэгчийн нэмсэн ангиллууд (төрлөөр). */
    suspend fun customCategories(type: String): List<String> =
        context.dataStore.data.first()[catKey(type)]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()

    suspend fun addCategory(type: String, name: String) {
        val list = customCategories(type) + name
        context.dataStore.edit { it[catKey(type)] = list.joinToString("|") }
    }

    suspend fun setFilter(value: String) {
        context.dataStore.edit { it[key] = value }
    }

    companion object {
        const val ALL = "ALL"
    }
}
