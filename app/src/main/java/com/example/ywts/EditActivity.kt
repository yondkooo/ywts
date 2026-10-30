package com.example.ywts

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ywts.databinding.ActivityEditBinding
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditActivity : AppCompatActivity() {
    private lateinit var b: ActivityEditBinding
    private val vm: EditViewModel by viewModels { Factory(application as App) }
    private var id = 0L
    private val custom = mutableMapOf<String, List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.applySystemBarInsets()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        custom[Record.INCOME] = runBlocking { vm.customCategories(Record.INCOME) }
        custom[Record.EXPENSE] = runBlocking { vm.customCategories(Record.EXPENSE) }

        id = intent.getLongExtra(EXTRA_ID, 0L)
        supportActionBar?.setTitle(if (id == 0L) R.string.add_title else R.string.edit_title)
        if (savedInstanceState == null) {
            b.typeGroup.check(if (intent.getStringExtra(EXTRA_TYPE) == Record.INCOME) R.id.btnIncome else R.id.btnExpense)
            setCategories(currentType())
            val c = Calendar.getInstance()
            b.date.setText("%04d.%02d.%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)))
            if (id != 0L) lifecycleScope.launch {
                vm.load(id)?.let {
                    b.typeGroup.check(if (it.type == Record.INCOME) R.id.btnIncome else R.id.btnExpense)
                    setCategories(it.type, it.category)
                    b.name.setText(it.name)
                    b.note.setText(it.note)
                    b.amount.setText(it.amount.toString())
                    b.date.setText(it.date)
                }
            }
        }

        b.typeGroup.addOnButtonCheckedListener { _, _, checked ->
            if (checked) setCategories(currentType())
        }
        b.date.setOnClickListener { pickDate() }
        b.dateLayout.setEndIconOnClickListener { pickDate() }
        b.save.setOnClickListener { save() }
    }

    private fun currentType() =
        if (b.typeGroup.checkedButtonId == R.id.btnIncome) Record.INCOME else Record.EXPENSE

    /** Төрлөөс хамаарсан ангиллын жагсаалтыг dropdown-д хийнэ (төгсгөлд "+ Шинэ ангилал"). */
    private fun setCategories(type: String, selected: String? = null) {
        val list = Record.categories(type) + custom.getValue(type)
        val newLabel = getString(R.string.new_category)
        val items = list + newLabel
        b.category.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, items))
        val current = selected?.takeIf { it in list } ?: list.first()
        showCategory(current)
        b.category.setOnItemClickListener { _, _, pos, _ ->
            if (pos < list.size) showCategory(list[pos])
            else {
                showCategory(current)
                askNewCategory(type)
            }
        }
    }

    private fun askNewCategory(type: String) {
        val input = android.widget.EditText(this).apply {
            hint = getString(R.string.new_category_hint)
            setSingleLine()
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.new_category)
            .setView(input, 48, 16, 48, 0)
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = input.text.toString().trim().replace("|", "")
                if (name.isNotEmpty() && name !in Record.categories(type) + custom.getValue(type)) {
                    custom[type] = custom.getValue(type) + name
                    vm.addCategory(type, name)
                    setCategories(type, name)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCategory(category: String) {
        b.category.setText(category, false)
    }

    private fun pickDate() {
        val p = b.date.text.toString().split(".").mapNotNull { it.toIntOrNull() }
        val c = Calendar.getInstance()
        val (y, m, d) = if (p.size == 3) Triple(p[0], p[1] - 1, p[2])
        else Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        DatePickerDialog(this, { _, yy, mm, dd -> b.date.setText("%04d.%02d.%02d".format(yy, mm + 1, dd)) }, y, m, d).show()
    }

    private fun save() {
        val name = b.name.text.toString().trim()
        val amount = b.amount.text.toString().trim().toLongOrNull()
        if (name.isEmpty() || amount == null) {
            Toast.makeText(this, R.string.fill_all, Toast.LENGTH_SHORT).show()
            return
        }
        val type = currentType()
        val category = b.category.text.toString()
        vm.save(Record(id, type, name, category, amount, b.date.text.toString(), b.note.text.toString().trim())) { saved ->
            setResult(RESULT_OK, android.content.Intent().putExtra(EXTRA_SAVED_ID, saved).putExtra(EXTRA_DATE, b.date.text.toString()))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TYPE = "type"
        const val EXTRA_SAVED_ID = "saved_id"
        const val EXTRA_DATE = "date"
    }
}
