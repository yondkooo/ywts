package com.example.ywts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    var selected by remember { mutableStateOf(SettingsStore.ALL) }

    LaunchedEffect(Unit) { selected = vm.filter.first() }

    val options = listOf(
        SettingsStore.ALL to R.string.all,
        Record.INCOME to R.string.only_income,
        Record.EXPENSE to R.string.only_expense,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←", color = Color.White, fontSize = 22.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue, titleContentColor = Color.White),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.filter_title), fontSize = 18.sp)
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { selected = value }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == value, onClick = { selected = value })
                    Text(stringResource(label))
                }
            }
            Button(
                onClick = { vm.setFilter(selected); onBack() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) { Text(stringResource(R.string.save)) }
        }
    }
}
