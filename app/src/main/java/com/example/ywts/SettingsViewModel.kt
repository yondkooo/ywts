package com.example.ywts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {
    val filter = settings.filter

    fun setFilter(value: String) {
        viewModelScope.launch { settings.setFilter(value) }
    }
}
