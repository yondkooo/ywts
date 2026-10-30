package com.example.ywts

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels { Factory(application as App) }
    private var scrollTick by mutableIntStateOf(0)

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.getLongExtra(EditActivity.EXTRA_SAVED_ID, 0L)?.takeIf { id -> id != 0L }?.let { id ->
                it.data?.getStringExtra(EditActivity.EXTRA_DATE)?.let(vm::showMonthOf)
                vm.select(id)
                scrollTick++
            }
        }

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            AppTheme {
                MainScreen(
                    vm = vm,
                    scrollTick = scrollTick,
                    onAdd = { type ->
                        editLauncher.launch(Intent(this, EditActivity::class.java).putExtra(EditActivity.EXTRA_TYPE, type))
                    },
                    onEdit = { r ->
                        editLauncher.launch(Intent(this, EditActivity::class.java).putExtra(EditActivity.EXTRA_ID, r.id))
                    },
                    onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Reminder.cancel(this)
    }

    override fun onStop() {
        super.onStop()
        Reminder.schedule(this)
    }
}
