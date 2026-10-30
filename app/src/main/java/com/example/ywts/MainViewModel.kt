package com.example.ywts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MainState(
    val records: List<Record> = emptyList(),
    val selected: Record? = null,
    val revealed: Set<Long> = emptySet(),
    val incomeTotal: Long = 0,
    val expenseTotal: Long = 0,
    val month: String = "",
    val hasAny: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repo: RecordRepository,
    settings: SettingsStore,
) : ViewModel() {
    private val selectedId = MutableStateFlow(0L)
    private val revealed = MutableStateFlow<Set<Long>>(emptySet())
    private val month = MutableStateFlow(Calendar.getInstance().let { it.get(Calendar.YEAR) * 12 + it.get(Calendar.MONTH) })

    private fun label(m: Int) = "%04d.%02d".format(m / 12, m % 12 + 1)

    private val records = settings.filter.flatMapLatest { f ->
        repo.records(if (f == SettingsStore.ALL) null else f)
    }
    private val all = repo.records(null)

    val state: StateFlow<MainState> = combine(records, selectedId, revealed, all, month) { full, sel, rev, everything, m ->
        val prefix = label(m)
        val list = full.filter { it.date.startsWith(prefix) }
        val every = everything.filter { it.date.startsWith(prefix) }
        MainState(
            list,
            list.firstOrNull { it.id == sel },
            rev,
            every.filter { it.type == Record.INCOME }.sumOf { it.amount },
            every.filter { it.type == Record.EXPENSE }.sumOf { it.amount },
            prefix,
            everything.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainState())

    fun moveMonth(delta: Int) { month.value += delta; revealed.value = emptySet() }

    /** Хадгалсан бүртгэлийн сар руу шилжинэ (огноо: yyyy.MM.dd). */
    fun showMonthOf(date: String) {
        val p = date.split(".").mapNotNull { it.toIntOrNull() }
        if (p.size >= 2) month.value = p[0] * 12 + p[1] - 1
    }

    /** mur deer daranguut nuuh eswel haruuldag bolgno  */
    fun tap(id: Long) {
        revealed.value = revealed.value.let { if (id in it) it - id else it + id }
    }

    /** udaan darahad zasah ustgah towch n hrgddg bolgno*/
    fun toggleSelect(id: Long) { selectedId.value = if (selectedId.value == id) 0L else id }

    fun select(id: Long) { selectedId.value = id }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
        }
    }
}
