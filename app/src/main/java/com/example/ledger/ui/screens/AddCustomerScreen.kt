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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(onSave: (String, String?) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("إضافة عميل") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it; error = "" }, modifier = Modifier.fillMaxWidth(), label = { Text("الاسم") }, isError = error.isNotEmpty())
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("رقم العميل") })
            Button(onClick = {
                if (name.isBlank()) error = "الاسم مطلوب" else onSave(name.trim(), phone.ifBlank { null })
            }, modifier = Modifier.fillMaxWidth()) { Text("حفظ") }
            TextButton(onClick = onBack) { Text("رجوع") }
        }
    }
}
