package com.example.ywts

data class Record(
    val id: Long,
    val type: String,
    val name: String,
    val category: String = "",
    val amount: Long,
    val date: String,
    val note: String = "",
) {
    companion object {
        val INCOME_CATEGORIES = listOf("Цалин", "Шагнал", "Бусад орлого")
        val EXPENSE_CATEGORIES = listOf("Хоол", "Унаа", "Дэлгүүр", "Гэр бүл", "Бусад зарлага")
        private val ICONS = mapOf(
            "Цалин" to R.drawable.ic_cat_work, "Шагнал" to R.drawable.ic_cat_trophy, "Бусад орлого" to R.drawable.ic_cat_other,
            "Хоол" to R.drawable.ic_cat_food, "Унаа" to R.drawable.ic_cat_bus, "Дэлгүүр" to R.drawable.ic_cat_shop,
            "Гэр бүл" to R.drawable.ic_cat_home, "Бусад зарлага" to R.drawable.ic_cat_other,
        )

        fun icon(category: String): Int = ICONS[category] ?: R.drawable.ic_cat_other

        fun categories(type: String) = if (type == INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES

        const val INCOME = "INCOME"
        const val EXPENSE = "EXPENSE"
    }
}
