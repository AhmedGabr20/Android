package com.example.ledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Insert
    suspend fun insertCustomer(customer: Customer): Long

    @Insert
    suspend fun insertItem(itemEntry: ItemEntry)

    @Insert
    suspend fun insertPayment(paymentEntry: PaymentEntry)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Query("DELETE FROM items")
    suspend fun clearItems()

    @Query("DELETE FROM payments")
    suspend fun clearPayments()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query(
        """
        SELECT c.id,c.name,c.phone,c.createdAt,
        COALESCE(SUM(i.total),0) AS itemsTotal,
        COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customerId=c.id),0) AS paidTotal,
        COALESCE(SUM(i.total),0)-COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customerId=c.id),0) AS remaining
        FROM customers c
        LEFT JOIN items i ON i.customerId=c.id
        GROUP BY c.id
        ORDER BY c.createdAt DESC
        """
    )
    fun observeCustomerSummaries(): Flow<List<CustomerSummary>>

    @Query("SELECT * FROM customers WHERE id=:customerId")
    fun observeCustomer(customerId: Long): Flow<Customer>

    @Query("SELECT * FROM items WHERE customerId=:customerId ORDER BY date DESC")
    fun observeItems(customerId: Long): Flow<List<ItemEntry>>

    @Query("SELECT * FROM payments WHERE customerId=:customerId ORDER BY date DESC")
    fun observePayments(customerId: Long): Flow<List<PaymentEntry>>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ItemEntry>

    @Query("SELECT * FROM payments")
    suspend fun getAllPayments(): List<PaymentEntry>
}
