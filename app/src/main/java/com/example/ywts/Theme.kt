package com.example.ywts

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Blue = Color(0xFF1565C0)
val IncomeColor = Color(0xFF2E7D32)
val ExpenseColor = Color(0xFFC62828)

/** Хуучин XML (Material Components) загвартай ижил: 4dp булантай товч. */
val ButtonShape = RoundedCornerShape(4.dp)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blue, onPrimary = Color.White, background = Color.White, surface = Color.White),
        content = content,
    )
}
