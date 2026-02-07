package com.example.ledger.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ledger.util.pretty
import com.example.ledger.util.prettyDate
import com.example.ledger.viewmodel.CustomerDetailViewModel
import com.example.ledger.viewmodel.ItemSort
import com.example.ledger.viewmodel.PaymentSort
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    viewModel: CustomerDetailViewModel,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onAddPayment: () -> Unit,
    onSharePdf: (Uri) -> Unit
) {
    val customer by viewModel.customer(customerId).collectAsState(initial = null)
    val items by viewModel.items(customerId).collectAsState(initial = emptyList())
    val payments by viewModel.payments(customerId).collectAsState(initial = emptyList())
    val totals by viewModel.totals(customerId).collectAsState()
    val filter by viewModel.filterState.collectAsState()
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }

    Scaffold(topBar = {
        TopAppBar(title = { Text(customer?.name ?: "التفاصيل") }, navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }, actions = {
            IconButton(onClick = {
                scope.launch {
                    val file = viewModel.generateCustomerPdf(customerId)
                    onSharePdf(viewModel.shareableUri(file))
                }
            }) { Icon(Icons.Default.PictureAsPdf, null) }
        })
    }) { p ->
        Column(Modifier.padding(p).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("إجمالي ${totals.first.pretty()}")
                    Text("مدفوع ${totals.second.pretty()}")
                    Text("متبقي ${totals.third.pretty()}")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddItem) { Text("إضافة بند") }
                Button(onClick = onAddPayment) { Text("سداد") }
            }
            OutlinedTextField(value = filter.search, onValueChange = { viewModel.updateFilter(filter.copy(search = it)) }, label = { Text("بحث في البنود") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { viewModel.quickFilterToday() }, label = { Text("اليوم") })
                FilterChip(selected = false, onClick = { viewModel.updateFilter(filter.copy(fromDate = null, toDate = null)) }, label = { Text("الكل") })
            }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("البنود") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("الدفعات") })
            }
            if (tab == 0) {
                SortRowItem(filter.itemSort) { viewModel.updateFilter(filter.copy(itemSort = it)) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) {
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) { Text(it.title); Text(it.date.prettyDate(), style = MaterialTheme.typography.bodySmall) }
                                Text("${it.qty} × ${it.price.pretty()} = ${it.total.pretty()}")
                                IconButton(onClick = { viewModel.deleteItem(it.id) }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            } else {
                SortRowPayment(filter.paymentSort) { viewModel.updateFilter(filter.copy(paymentSort = it)) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(payments) {
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(it.date.prettyDate())
                                Text(it.amount.pretty())
                                IconButton(onClick = { viewModel.deletePayment(it.id) }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortRowItem(selected: ItemSort, onSelect: (ItemSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("ترتيب البنود") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("الأحدث") }, onClick = { expanded = false; onSelect(ItemSort.NEWEST) })
            DropdownMenuItem(text = { Text("الأقدم") }, onClick = { expanded = false; onSelect(ItemSort.OLDEST) })
            DropdownMenuItem(text = { Text("الأعلى إجمالي") }, onClick = { expanded = false; onSelect(ItemSort.HIGHEST) })
            DropdownMenuItem(text = { Text("الأقل إجمالي") }, onClick = { expanded = false; onSelect(ItemSort.LOWEST) })
        }
    }
}

@Composable
private fun SortRowPayment(selected: PaymentSort, onSelect: (PaymentSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("ترتيب الدفعات") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("الأحدث") }, onClick = { expanded = false; onSelect(PaymentSort.NEWEST) })
            DropdownMenuItem(text = { Text("الأقدم") }, onClick = { expanded = false; onSelect(PaymentSort.OLDEST) })
            DropdownMenuItem(text = { Text("الأعلى مبلغ") }, onClick = { expanded = false; onSelect(PaymentSort.HIGHEST) })
            DropdownMenuItem(text = { Text("الأقل مبلغ") }, onClick = { expanded = false; onSelect(PaymentSort.LOWEST) })
        }
    }
}
