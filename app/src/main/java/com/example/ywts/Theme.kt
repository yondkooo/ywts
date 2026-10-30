package com.example.ywts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Blue = Color(0xFF1565C0)
val IncomeColor = Color(0xFF2E7D32)
val ExpenseColor = Color(0xFFC62828)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blue, onPrimary = Color.White),
        content = content,
    )
}
