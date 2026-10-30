package com.example.ywts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

class EditActivity : ComponentActivity() {
    private val vm: EditViewModel by viewModels { Factory(application as App) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: Record.EXPENSE

        setContent {
            AppTheme {
                EditScreen(
                    vm = vm,
                    id = id,
                    initialType = type,
                    onBack = ::finish,
                    onSaved = { saved, date ->
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(EXTRA_SAVED_ID, saved).putExtra(EXTRA_DATE, date),
                        )
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TYPE = "type"
        const val EXTRA_SAVED_ID = "saved_id"
        const val EXTRA_DATE = "date"
    }
}
