package com.example.ledger.repository

import com.example.ledger.data.Customer
import com.example.ledger.data.ItemEntry
import com.example.ledger.data.LedgerDao
import com.example.ledger.data.PaymentEntry

class LedgerRepository(private val dao: LedgerDao) {
    fun customerSummaries() = dao.observeCustomerSummaries()
    fun customer(id: Long) = dao.observeCustomer(id)
    fun items(id: Long) = dao.observeItems(id)
    fun payments(id: Long) = dao.observePayments(id)

    suspend fun addCustomer(name: String, phone: String?) {
        dao.insertCustomer(Customer(name = name, phone = phone, createdAt = System.currentTimeMillis()))
    }

    suspend fun addItem(customerId: Long, date: Long, title: String, qty: Double, price: Double) {
        dao.insertItem(ItemEntry(customerId = customerId, date = date, title = title, qty = qty, price = price, total = qty * price))
    }

    suspend fun addPayment(customerId: Long, date: Long, amount: Double) {
        dao.insertPayment(PaymentEntry(customerId = customerId, date = date, amount = amount))
    }

    suspend fun deleteItem(id: Long) = dao.deleteItem(id)
    suspend fun deletePayment(id: Long) = dao.deletePayment(id)

    suspend fun insertDemoData() {
        val c1 = dao.insertCustomer(Customer(name = "عميل تجريبي", phone = "0500000000", createdAt = System.currentTimeMillis()))
        dao.insertItem(ItemEntry(customerId = c1, date = System.currentTimeMillis(), title = "بند تجريبي", qty = 2.0, price = 150.0, total = 300.0))
        dao.insertPayment(PaymentEntry(customerId = c1, date = System.currentTimeMillis(), amount = 100.0))
    }
}
