package com.example.ledger.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ledger.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(viewModel: BackupViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { viewModel.exportCsv(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importCsv(it) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("النسخ الاحتياطي") }, navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(onClick = { exportLauncher.launch("ledger_backup.csv") }, modifier = Modifier.fillMaxWidth()) { Text("تصدير CSV") }
            Button(onClick = { importLauncher.launch(arrayOf("text/csv")) }, modifier = Modifier.fillMaxWidth()) { Text("استيراد CSV") }
            Button(onClick = { viewModel.exportAllCustomersPdf(android.net.Uri.EMPTY) }, modifier = Modifier.fillMaxWidth()) { Text("تصدير PDF لكل العملاء") }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = state.merge, onCheckedChange = viewModel::toggleMerge)
                Text("دمج")
            }
            if (state.message.isNotBlank()) Text(state.message)
            state.summary?.let { Text("عملاء: ${it.customers} | بنود: ${it.items} | دفعات: ${it.payments} | أخطاء: ${it.errors}") }
        }
    }
}
