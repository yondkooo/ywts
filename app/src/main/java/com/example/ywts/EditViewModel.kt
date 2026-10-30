package com.example.ywts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditViewModel(
    private val repo: RecordRepository,
    private val settings: SettingsStore,
) : ViewModel() {
    suspend fun load(id: Long): Record? = repo.get(id)

    suspend fun customCategories(type: String) = settings.customCategories(type)

    fun addCategory(type: String, name: String) {
        viewModelScope.launch { settings.addCategory(type, name) }
    }

    fun save(record: Record, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            onDone(repo.save(record))
        }
    }
}
