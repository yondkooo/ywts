package com.example.ywts

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ywts.databinding.ActivitySettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private val vm: SettingsViewModel by viewModels { Factory(application as App) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.applySystemBarInsets()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            b.group.check(
                when (vm.filter.first()) {
                    Record.INCOME -> R.id.rbIncome
                    Record.EXPENSE -> R.id.rbExpense
                    else -> R.id.rbAll
                }
            )
            b.save.setOnClickListener {
                vm.setFilter(
                    when (b.group.checkedRadioButtonId) {
                        R.id.rbIncome -> Record.INCOME
                        R.id.rbExpense -> Record.EXPENSE
                        else -> SettingsStore.ALL
                    }
                )
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
