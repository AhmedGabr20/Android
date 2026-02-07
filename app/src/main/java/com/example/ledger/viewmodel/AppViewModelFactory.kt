package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ledger.repository.BackupRepository
import com.example.ledger.repository.LedgerRepository
import com.example.ledger.repository.PdfRepository

class AppViewModelFactory(
    private val ledgerRepository: LedgerRepository,
    private val backupRepository: BackupRepository,
    private val pdfRepository: PdfRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(ledgerRepository) as T
            modelClass.isAssignableFrom(CustomerDetailViewModel::class.java) -> CustomerDetailViewModel(ledgerRepository, pdfRepository) as T
            modelClass.isAssignableFrom(EntryViewModel::class.java) -> EntryViewModel(ledgerRepository) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> BackupViewModel(backupRepository, pdfRepository) as T
            else -> throw IllegalArgumentException("Unknown view model")
        }
    }
}
