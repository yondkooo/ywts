package com.example.ywts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ywts.databinding.ItemRecordBinding
import java.text.NumberFormat
import java.util.Locale

class RecordAdapter(
    private val onTap: (Record) -> Unit,
    private val onLongTap: (Record) -> Unit,
    private val onEdit: (Record) -> Unit,
    private val onDelete: (Record) -> Unit,
) : ListAdapter<Record, RecordAdapter.VH>(Diff) {
    private var selectedId = 0L
    private var revealed: Set<Long> = emptySet()

    fun update(list: List<Record>, selected: Long, revealed: Set<Long>) {
        this.selectedId = selected
        this.revealed = revealed
        submitList(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemRecordBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val r = getItem(position)
        val ctx = h.b.root.context
        val income = r.type == Record.INCOME
        val color = ctx.getColor(if (income) R.color.income else R.color.expense)
        h.b.icon.setImageResource(Record.icon(r.category))
        h.b.name.text = r.name
        h.b.category.text = if (r.note.isBlank()) r.category else "${r.category} · ${r.note}"
        h.b.date.text = r.date
        h.b.amount.setTextColor(color)
        h.b.amount.text = if (r.id in revealed)
            "${if (income) "+" else "-"} ${NumberFormat.getInstance(Locale.US).format(r.amount)}₮"
        else "**** ₮"
        h.b.root.isChecked = r.id == selectedId
        h.b.actions.visibility = if (r.id == selectedId) android.view.View.VISIBLE else android.view.View.GONE
        h.b.edit.setOnClickListener { onEdit(r) }
        h.b.delete.setOnClickListener { onDelete(r) }
        h.b.root.setOnClickListener { onTap(r) }
        h.b.root.setOnLongClickListener { onLongTap(r); true }
    }

    private object Diff : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(a: Record, b: Record) = a.id == b.id
        override fun areContentsTheSame(a: Record, b: Record) = a == b
    }
}
