package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.data.CustomerSummary
import com.example.ledger.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LedgerRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val customers = combine(repository.customerSummaries(), query) { list, q ->
        if (q.isBlank()) list else list.filter { it.name.contains(q, true) || (it.phone ?: "").contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String, phone: String?) = viewModelScope.launch { repository.addCustomer(name, phone) }
    fun addDemo() = viewModelScope.launch { repository.insertDemoData() }
}
