package com.example.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ledger.ui.components.DatePickerField
import com.example.ledger.util.pretty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(customerId: Long, onSave: (Long, String, Double, Double) -> Unit, onBack: () -> Unit) {
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var title by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val total = (qty.toDoubleOrNull() ?: 0.0) * (price.toDoubleOrNull() ?: 0.0)
    Scaffold(topBar = { TopAppBar(title = { Text("إضافة بند") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(value = date, onDateSelected = { date = it }, label = "التاريخ")
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("البند") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("الكمية") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("السعر") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                TextButton(onClick = {
                    price = if (price.isBlank()) "1000" else ((price.toDoubleOrNull() ?: 0.0) * 1000).toString()
                }) { Text("ألف") }
            })
            Text("الإجمالي = ${total.pretty()}")
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val q = qty.toDoubleOrNull() ?: 0.0
                val p0 = price.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || q <= 0 || p0 <= 0) error = "تحقق من البيانات" else onSave(date, title, q, p0)
            }, modifier = Modifier.fillMaxWidth()) { Text("حفظ") }
            TextButton(onClick = onBack) { Text("رجوع") }
        }
    }
}
