package com.example.ywts

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ywts.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private var pendingScroll = false
    private val adapter = RecordAdapter(
        onTap = { vm.tap(it.id) },
        onLongTap = { vm.toggleSelect(it.id) },
        onEdit = { openEdit(it) },
        onDelete = { confirmDelete(it) },
    )
    private val vm: MainViewModel by viewModels { Factory(application as App) }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.getLongExtra(EditActivity.EXTRA_SAVED_ID, 0L)?.takeIf { id -> id != 0L }?.let { id ->
                it.data?.getStringExtra(EditActivity.EXTRA_DATE)?.let(vm::showMonthOf)
                vm.select(id)
                pendingScroll = true
            }
        }

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.applySystemBarInsets()

        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

        b.prev.setOnClickListener { vm.moveMonth(-1) }
        b.next.setOnClickListener { vm.moveMonth(1) }
        b.list.layoutManager = LinearLayoutManager(this)
        b.list.adapter = adapter
        b.addIncome.setOnClickListener { openAdd(Record.INCOME) }
        b.addExpense.setOnClickListener { openAdd(Record.EXPENSE) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { vm.state.collect(::render) }
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

    private fun openAdd(type: String) {
        editLauncher.launch(Intent(this, EditActivity::class.java).putExtra(EditActivity.EXTRA_TYPE, type))
    }

    private fun openEdit(r: Record) {
        editLauncher.launch(Intent(this, EditActivity::class.java).putExtra(EditActivity.EXTRA_ID, r.id))
    }

    private fun confirmDelete(r: Record) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ -> vm.delete(r.id) }
            .setNegativeButton(R.string.no, null)
            .show()
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(getColor(R.color.expense))
    }

    private fun render(s: MainState) {
        val fmt = NumberFormat.getInstance(Locale.US)
        b.totalIncome.text = "${fmt.format(s.incomeTotal)}₮"
        b.totalExpense.text = "${fmt.format(s.expenseTotal)}₮"
        b.prev.isEnabled = s.hasAny
        b.next.isEnabled = s.hasAny
        b.month.text = s.month.replace(".", "-")
        b.empty.visibility = if (s.records.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        adapter.update(s.records, s.selected?.id ?: 0L, s.revealed)
        if (pendingScroll && s.selected != null) {
            pendingScroll = false
            b.list.scrollToPosition(s.records.indexOfFirst { it.id == s.selected.id })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
