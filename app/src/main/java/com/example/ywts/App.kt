package com.example.ywts

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    val repository by lazy { RecordRepository(this) }
    val settings by lazy { SettingsStore(this) }

    override fun onCreate() {
        super.onCreate()
        Reminder.createChannel(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
