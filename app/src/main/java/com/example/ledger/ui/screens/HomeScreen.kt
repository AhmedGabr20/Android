package com.example.ledger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ledger.BuildConfig
import com.example.ledger.util.pretty
import com.example.ledger.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onAdd: () -> Unit, onOpenDetails: (Long) -> Unit, onOpenBackup: () -> Unit) {
    val customers by viewModel.customers.collectAsState()
    val query by viewModel.query.collectAsState()
    var menu by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("العملاء") }, actions = {
            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("النسخ الاحتياطي") }, onClick = { menu = false; onOpenBackup() })
                if (BuildConfig.DEBUG) DropdownMenuItem(text = { Text("بيانات تجريبية") }, onClick = { menu = false; viewModel.addDemo() })
            }
        })
    }, floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = query, onValueChange = { viewModel.query.value = it }, modifier = Modifier.fillMaxWidth(), label = { Text("بحث") })
            if (customers.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null)
                    Text("لا يوجد عملاء")
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(customers) { c ->
                    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpenDetails(c.id) }) {
                        Column(Modifier.padding(14.dp)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium)
                            c.phone?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                AssistChip(onClick = {}, label = { Text("إجمالي ${c.itemsTotal.pretty()}") })
                                AssistChip(onClick = {}, label = { Text("متبقي ${c.remaining.pretty()}") })
                            }
                        }
                    }
                }
            }
        }
    }
}
