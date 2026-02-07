package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.repository.LedgerRepository
import kotlinx.coroutines.launch

class EntryViewModel(private val repository: LedgerRepository) : ViewModel() {
    fun addItem(customerId: Long, date: Long, title: String, qty: Double, price: Double) = viewModelScope.launch {
        repository.addItem(customerId, date, title, qty, price)
    }

    fun addPayment(customerId: Long, date: Long, amount: Double) = viewModelScope.launch {
        repository.addPayment(customerId, date, amount)
    }
}
