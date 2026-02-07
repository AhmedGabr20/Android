package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.data.ItemEntry
import com.example.ledger.data.PaymentEntry
import com.example.ledger.repository.LedgerRepository
import com.example.ledger.repository.PdfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class ItemSort { NEWEST, OLDEST, HIGHEST, LOWEST }
enum class PaymentSort { NEWEST, OLDEST, HIGHEST, LOWEST }

data class FilterState(
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val itemSort: ItemSort = ItemSort.NEWEST,
    val paymentSort: PaymentSort = PaymentSort.NEWEST,
    val search: String = ""
)

class CustomerDetailViewModel(
    private val repository: LedgerRepository,
    private val pdfRepository: PdfRepository
) : ViewModel() {
    val filterState = MutableStateFlow(FilterState())

    fun customer(id: Long) = repository.customer(id)

    fun items(id: Long) = combine(repository.items(id), filterState) { list, filter ->
        list.filterByDate(filter.fromDate, filter.toDate)
            .filter { it.title.contains(filter.search, true) }
            .let {
                when (filter.itemSort) {
                    ItemSort.NEWEST -> it.sortedByDescending(ItemEntry::date)
                    ItemSort.OLDEST -> it.sortedBy(ItemEntry::date)
                    ItemSort.HIGHEST -> it.sortedByDescending(ItemEntry::total)
                    ItemSort.LOWEST -> it.sortedBy(ItemEntry::total)
                }
            }
    }

    fun payments(id: Long) = combine(repository.payments(id), filterState) { list, filter ->
        list.filterByDate(filter.fromDate, filter.toDate).let {
            when (filter.paymentSort) {
                PaymentSort.NEWEST -> it.sortedByDescending(PaymentEntry::date)
                PaymentSort.OLDEST -> it.sortedBy(PaymentEntry::date)
                PaymentSort.HIGHEST -> it.sortedByDescending(PaymentEntry::amount)
                PaymentSort.LOWEST -> it.sortedBy(PaymentEntry::amount)
            }
        }
    }

    fun totals(id: Long) = combine(repository.items(id), repository.payments(id)) { items, pays ->
        Triple(items.sumOf { it.total }, pays.sumOf { it.amount }, items.sumOf { it.total } - pays.sumOf { it.amount })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0.0, 0.0, 0.0))

    fun deleteItem(id: Long) = viewModelScope.launch { repository.deleteItem(id) }
    fun deletePayment(id: Long) = viewModelScope.launch { repository.deletePayment(id) }

    fun quickFilterToday() {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, 1)
        filterState.value = filterState.value.copy(fromDate = start, toDate = c.timeInMillis)
    }

    fun updateFilter(newState: FilterState) { filterState.value = newState }
    suspend fun generateCustomerPdf(customerId: Long): File = pdfRepository.generateCustomerStatement(customerId)
    fun shareableUri(file: File) = pdfRepository.fileUri(file)
}

private fun <T> List<T>.filterByDate(from: Long?, to: Long?): List<T> where T : Any {
    return filter {
        val date = when (it) {
            is ItemEntry -> it.date
            is PaymentEntry -> it.date
            else -> Long.MIN_VALUE
        }
        (from == null || date >= from) && (to == null || date <= to)
    }
}
