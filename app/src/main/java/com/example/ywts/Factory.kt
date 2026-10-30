package com.example.ywts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class Factory(private val app: App) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        MainViewModel::class.java -> MainViewModel(app.repository, app.settings)
        EditViewModel::class.java -> EditViewModel(app.repository, app.settings)
        SettingsViewModel::class.java -> SettingsViewModel(app.settings)
        else -> error("Unknown ViewModel")
    } as T
}
