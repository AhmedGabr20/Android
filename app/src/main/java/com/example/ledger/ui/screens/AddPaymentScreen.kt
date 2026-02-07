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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(customerId: Long, onSave: (Long, Double) -> Unit, onBack: () -> Unit) {
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("إضافة دفعة") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(value = date, onDateSelected = { date = it }, label = "التاريخ")
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("مدفوع") }, modifier = Modifier.fillMaxWidth())
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val a = amount.toDoubleOrNull() ?: 0.0
                if (a <= 0) error = "قيمة غير صحيحة" else onSave(date, a)
            }, modifier = Modifier.fillMaxWidth()) { Text("حفظ") }
            TextButton(onClick = onBack) { Text("رجوع") }
        }
    }
}
