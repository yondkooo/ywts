package com.example.ywts

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.TimeZone

private fun formatDate(y: Int, m: Int, d: Int) = "%04d.%02d.%02d".format(y, m, d)

private fun today(): String = Calendar.getInstance().let {
    formatDate(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
}

/** "yyyy.MM.dd" -> DatePicker-ийн UTC миллисекунд */
private fun toMillis(date: String): Long {
    val p = date.split(".").mapNotNull { it.toIntOrNull() }
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    if (p.size == 3) c.set(p[0], p[1] - 1, p[2], 0, 0, 0)
    return c.timeInMillis
}

private fun fromMillis(ms: Long): String {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
    return formatDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    vm: EditViewModel,
    id: Long,
    initialType: String,
    onBack: () -> Unit,
    onSaved: (savedId: Long, date: String) -> Unit,
) {
    val context = LocalContext.current
    var type by rememberSaveable { mutableStateOf(initialType) }
    var category by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(today()) }
    var note by rememberSaveable { mutableStateOf("") }
    var custom by remember { mutableStateOf<Map<String, List<String>>>(mapOf(Record.INCOME to emptyList(), Record.EXPENSE to emptyList())) }
    var menuOpen by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(id == 0L) }

    val categories = Record.categories(type) + custom.getValue(type)

    // Хэрэглэгчийн ангилал болон (засах үед) хуучин бүртгэлийг уншина.
    LaunchedEffect(Unit) {
        custom = mapOf(
            Record.INCOME to vm.customCategories(Record.INCOME),
            Record.EXPENSE to vm.customCategories(Record.EXPENSE),
        )
        if (!loaded) {
            vm.load(id)?.let {
                type = it.type; category = it.category; name = it.name
                amount = it.amount.toString(); date = it.date; note = it.note
            }
            loaded = true
        }
    }
    // Төрөл солигдоход ангилал тухайн төрлийнх биш бол эхнийхийг сонгоно.
    LaunchedEffect(type, custom) {
        if (category !in categories) category = categories.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (id == 0L) R.string.add_title else R.string.edit_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←", color = Color.White, fontSize = 22.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue, titleContentColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Орлого | Зарлага
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeButton(stringResource(R.string.income), type == Record.INCOME, Modifier.weight(1f)) { type = Record.INCOME }
                TypeButton(stringResource(R.string.expense), type == Record.EXPENSE, Modifier.weight(1f)) { type = Record.EXPENSE }
            }

            // Ангилал (dropdown)
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category_hint)) },
                    trailingIcon = { Text("▾") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(Modifier.matchParentSize().clickable { menuOpen = true })
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { category = c; menuOpen = false })
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.new_category)) },
                        onClick = { menuOpen = false; showNewCategory = true },
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { v -> amount = v.filter { it.isDigit() } },
                label = { Text(stringResource(R.string.amount_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Box {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.date_hint)) },
                    trailingIcon = { Text("📅") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(Modifier.matchParentSize().clickable { showDate = true })
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val value = amount.toLongOrNull()
                    if (name.isBlank() || value == null) {
                        Toast.makeText(context, R.string.fill_all, Toast.LENGTH_SHORT).show()
                    } else {
                        vm.save(Record(id, type, name.trim(), category, value, date, note.trim())) { saved ->
                            onSaved(saved, date)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text(stringResource(R.string.save)) }
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = toMillis(date))
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = fromMillis(it) }
                    showDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = pickerState) }
    }

    if (showNewCategory) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategory = false },
            title = { Text(stringResource(R.string.new_category)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.new_category_hint)) },
                    singleLine = true,
                )
            },
            dismissButton = { TextButton(onClick = { showNewCategory = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = {
                TextButton(onClick = {
                    val n = newName.trim().replace("|", "")
                    if (n.isNotEmpty() && n !in categories) {
                        vm.addCategory(type, n)
                        custom = custom + (type to (custom.getValue(type) + n))
                        category = n
                    }
                    showNewCategory = false
                }) { Text(stringResource(R.string.ok)) }
            },
        )
    }
}

@Composable
private fun TypeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(text) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
}
