package com.example.ledger.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.repository.BackupRepository
import com.example.ledger.repository.PdfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val loading: Boolean = false,
    val message: String = "",
    val merge: Boolean = false,
    val summary: BackupRepository.ImportResult? = null
)

class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val pdfRepository: PdfRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun toggleMerge(value: Boolean) { _state.value = _state.value.copy(merge = value) }

    fun exportCsv(uri: Uri) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        runCatching { backupRepository.exportCsv(uri) }
            .onSuccess { _state.value = _state.value.copy(loading = false, message = "تم التصدير بنجاح") }
            .onFailure { _state.value = _state.value.copy(loading = false, message = "فشل التصدير: ${it.message}") }
    }

    fun importCsv(uri: Uri) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        runCatching { backupRepository.importCsv(uri, _state.value.merge) }
            .onSuccess { _state.value = _state.value.copy(loading = false, message = "تم الاستيراد", summary = it) }
            .onFailure { _state.value = _state.value.copy(loading = false, message = "فشل الاستيراد: ${it.message}") }
    }

    fun exportAllCustomersPdf(uri: Uri) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        runCatching {
            val file = pdfRepository.generateAllCustomersPdf()
            pdfRepository.fileUri(file)
        }.onSuccess {
            _state.value = _state.value.copy(loading = false, message = "تم إنشاء PDF في الكاش. استخدم مشاركة PDF من شاشة العميل.")
        }.onFailure {
            _state.value = _state.value.copy(loading = false, message = "فشل إنشاء PDF")
        }
    }
}
