package com.example.ywts

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale

private fun money(v: Long) = NumberFormat.getInstance(Locale.US).format(v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel,
    scrollTick: Int,
    onAdd: (String) -> Unit,
    onEdit: (Record) -> Unit,
    onSettings: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Record?>(null) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()
    var handledTick by remember { mutableIntStateOf(0) }

    // Хадгалсны дараа сонгогдсон бүртгэл рүү гүйлгэнэ.
    LaunchedEffect(scrollTick, state.selected?.id) {
        val index = state.records.indexOfFirst { it.id == state.selected?.id }
        if (scrollTick > handledTick && index >= 0) {
            listState.scrollToItem(index)
            handledTick = scrollTick
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = { Text(stringResource(R.string.app_name), fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                actions = {
                    TextButton(onClick = { menuOpen = true }) {
                        Text("⋮", color = Color.White, fontSize = 22.sp)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = { menuOpen = false; onSettings() },
                        )
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { onAdd(Record.INCOME) },
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeColor),
                ) { Text(stringResource(R.string.add_income).uppercase()) }
                Button(
                    onClick = { onAdd(Record.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor),
                ) { Text(stringResource(R.string.add_expense).uppercase()) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
        ) {
            if (landscape) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MonthBar(state, vm, Modifier.weight(1f))
                    Totals(state, Modifier.weight(1f))
                }
            } else {
                MonthBar(state, vm, Modifier.fillMaxWidth())
                Totals(state, Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.records.isEmpty()) {
                    Text(
                        stringResource(R.string.empty),
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                    )
                }
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.records, key = { it.id }) { r ->
                        RecordRow(
                            record = r,
                            selected = r.id == state.selected?.id,
                            revealed = r.id in state.revealed,
                            onTap = { vm.tap(r.id) },
                            onLongTap = { vm.toggleSelect(r.id) },
                            onEdit = { onEdit(r) },
                            onDelete = { toDelete = r },
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.no)) } },
            confirmButton = {
                TextButton(onClick = { vm.delete(r.id); toDelete = null }) {
                    Text(stringResource(R.string.yes), color = ExpenseColor)
                }
            },
        )
    }
}

/** ‹ ӨМНӨХ | 2026-10 | ДАРААХ › */
@Composable
private fun MonthBar(state: MainState, vm: MainViewModel, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F1F1)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { vm.moveMonth(-1) }, enabled = state.hasAny) { Text("‹ " + stringResource(R.string.prev)) }
        Text(
            state.month.replace(".", "-"),
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = { vm.moveMonth(1) }, enabled = state.hasAny) { Text(stringResource(R.string.next) + " ›") }
    }
}

@Composable
private fun Totals(state: MainState, modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TotalBox("↑ " + stringResource(R.string.income), state.incomeTotal, IncomeColor, Color(0xFFE3F5E6), Modifier.weight(1f))
        TotalBox("↓ " + stringResource(R.string.expense), state.expenseTotal, ExpenseColor, Color(0xFFFDE3E3), Modifier.weight(1f))
    }
}

@Composable
private fun TotalBox(label: String, amount: Long, color: Color, bg: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp),
    ) {
        Text(label, color = color)
        Text("${money(amount)}₮", color = Color(0xFF1B1B1B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordRow(
    record: Record,
    selected: Boolean,
    revealed: Boolean,
    onTap: () -> Unit,
    onLongTap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val income = record.type == Record.INCOME
    val color = if (income) IncomeColor else ExpenseColor
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onTap, onLongClick = onLongTap),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (selected) BorderStroke(2.dp, Blue) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Record.icon(record.category)),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(record.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (record.note.isBlank()) record.category else "${record.category} · ${record.note}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (revealed) "${if (income) "+" else "-"} ${money(record.amount)}₮" else "**** ₮",
                        color = color,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(record.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (selected) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.edit)) }
                    TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.delete), color = ExpenseColor)
                    }
                }
            }
        }
    }
}
