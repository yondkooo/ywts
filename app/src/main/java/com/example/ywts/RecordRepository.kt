package com.example.ywts

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private class Db(context: Context) : SQLiteOpenHelper(context, "finance.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE records (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL DEFAULT '', note TEXT NOT NULL DEFAULT '', amount INTEGER NOT NULL, date TEXT NOT NULL)"
        )
        val seed = listOf(
            Triple(Record.INCOME, "Цалин", 2_000_000L),
            Triple(Record.EXPENSE, "Хоол", 15_000L),
            Triple(Record.EXPENSE, "Унаа", 3_000L),
        )
        seed.forEach { (t, n, a) ->
            db.insert("records", null, ContentValues().apply {
                put("type", t); put("name", n); put("category", n); put("amount", a); put("date", "2026.10.23")
            })
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE records ADD COLUMN category TEXT NOT NULL DEFAULT ''")
        if (oldVersion < 3) db.execSQL("ALTER TABLE records ADD COLUMN note TEXT NOT NULL DEFAULT ''")
    }
}

private fun Cursor.toRecord() = Record(
    getLong(getColumnIndexOrThrow("id")),
    getString(getColumnIndexOrThrow("type")),
    getString(getColumnIndexOrThrow("name")),
    getString(getColumnIndexOrThrow("category")),
    getLong(getColumnIndexOrThrow("amount")),
    getString(getColumnIndexOrThrow("date")),
    getString(getColumnIndexOrThrow("note")),
)

class RecordRepository(context: Context) {
    private val db = Db(context.applicationContext)
    private val version = MutableStateFlow(0)

    /** type == null бол бүх бүртгэл. Өгөгдөл өөрчлөгдөх бүрт шинэчлэгдэнэ. */
    fun records(type: String?): Flow<List<Record>> =
        version.map { query(type) }.flowOn(Dispatchers.IO)

    private fun query(type: String?): List<Record> {
        val list = mutableListOf<Record>()
        db.readableDatabase.query(
            "records", null,
            type?.let { "type = ?" }, type?.let { arrayOf(it) },
            null, null, "id DESC",
        ).use { c ->
            while (c.moveToNext()) {
                list += c.toRecord()
            }
        }
        return list
    }

    suspend fun get(id: Long): Record? = withContext(Dispatchers.IO) {
        db.readableDatabase.query("records", null, "id = ?", arrayOf("$id"), null, null, null).use { c ->
            if (!c.moveToFirst()) null else c.toRecord()
        }
    }

    suspend fun save(r: Record): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("type", r.type); put("name", r.name); put("category", r.category); put("note", r.note); put("amount", r.amount); put("date", r.date)
        }
        val id = if (r.id == 0L) db.writableDatabase.insert("records", null, cv)
        else { db.writableDatabase.update("records", cv, "id = ?", arrayOf("${r.id}")); r.id }
        version.value++
        id
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.writableDatabase.delete("records", "id = ?", arrayOf("$id"))
        version.value++
    }
}
