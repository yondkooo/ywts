package com.example.ywts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

class SettingsActivity : ComponentActivity() {
    private val vm: SettingsViewModel by viewModels { Factory(application as App) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { SettingsScreen(vm, onBack = ::finish) } }
    }
}
