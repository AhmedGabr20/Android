package com.example.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ledger.data.AppDatabase
import com.example.ledger.repository.BackupRepository
import com.example.ledger.repository.DataStoreRepository
import com.example.ledger.repository.LedgerRepository
import com.example.ledger.repository.PdfRepository
import com.example.ledger.ui.navigation.AppNavHost
import com.example.ledger.ui.theme.ArabicLedgerTheme
import com.example.ledger.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        val ledgerRepository = LedgerRepository(db.ledgerDao())
        val dataStoreRepository = DataStoreRepository(applicationContext)
        val backupRepository = BackupRepository(applicationContext, db, dataStoreRepository)
        val pdfRepository = PdfRepository(applicationContext, db)
        val factory = AppViewModelFactory(ledgerRepository, backupRepository, pdfRepository)

        setContent {
            ArabicLedgerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppNavHost(appViewModelFactory = factory)
                    }
                }
            }
        }
    }
}
