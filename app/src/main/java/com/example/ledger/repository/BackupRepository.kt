package com.example.ledger.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.Customer
import com.example.ledger.data.ItemEntry
import com.example.ledger.data.PaymentEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class BackupRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val dataStoreRepository: DataStoreRepository
) {
    data class ImportResult(val customers: Int, val items: Int, val payments: Int, val errors: Int)

    suspend fun exportCsv(uri: Uri) = withContext(Dispatchers.IO) {
        val dao = db.ledgerDao()
        val customers = dao.getAllCustomers()
        val items = dao.getAllItems()
        val payments = dao.getAllPayments()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            out.write("#LEDGER_BACKUP_V1\n".toByteArray())
            out.write("[CUSTOMERS]\n".toByteArray())
            out.write("id,name,phone,createdAt\n".toByteArray())
            customers.forEach { out.write("${it.id},${escape(it.name)},${escape(it.phone ?: "")},${it.createdAt}\n".toByteArray()) }
            out.write("[ITEMS]\n".toByteArray())
            out.write("id,customerId,date,title,qty,price,total\n".toByteArray())
            items.forEach {
                out.write("${it.id},${it.customerId},${it.date},${escape(it.title)},${it.qty.toCsv()},${it.price.toCsv()},${it.total.toCsv()}\n".toByteArray())
            }
            out.write("[PAYMENTS]\n".toByteArray())
            out.write("id,customerId,date,amount\n".toByteArray())
            payments.forEach { out.write("${it.id},${it.customerId},${it.date},${it.amount.toCsv()}\n".toByteArray()) }
        }
        dataStoreRepository.setLastExport(uri.toString())
    }

    suspend fun importCsv(uri: Uri, merge: Boolean): ImportResult = withContext(Dispatchers.IO) {
        val dao = db.ledgerDao()
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return@withContext ImportResult(0,0,0,1)
        val lines = text.replace("\uFEFF", "").lines()
        if (lines.firstOrNull()?.trim() != "#LEDGER_BACKUP_V1") return@withContext ImportResult(0, 0, 0, 1)

        val customers = mutableListOf<Customer>()
        val items = mutableListOf<ItemEntry>()
        val payments = mutableListOf<PaymentEntry>()
        var section = ""
        var errors = 0
        lines.drop(1).forEach { lineRaw ->
            val line = lineRaw.trim()
            if (line.isEmpty() || line.startsWith("id,")) return@forEach
            if (line.startsWith("[")) { section = line; return@forEach }
            val parts = parseCsv(line)
            try {
                when (section) {
                    "[CUSTOMERS]" -> customers += Customer(id = parts[0].toLong(), name = parts[1], phone = parts.getOrNull(2)?.ifBlank { null }, createdAt = parts[3].toLong())
                    "[ITEMS]" -> items += ItemEntry(id = parts[0].toLong(), customerId = parts[1].toLong(), date = parts[2].toLong(), title = parts[3], qty = parts[4].toDoubleLocale(), price = parts[5].toDoubleLocale(), total = parts[6].toDoubleLocale())
                    "[PAYMENTS]" -> payments += PaymentEntry(id = parts[0].toLong(), customerId = parts[1].toLong(), date = parts[2].toLong(), amount = parts[3].toDoubleLocale())
                }
            } catch (_: Exception) { errors++ }
        }

        if (!merge) {
            db.withTransaction {
                dao.clearItems(); dao.clearPayments(); dao.clearCustomers()
                val idMap = mutableMapOf<Long, Long>()
                customers.forEach { idMap[it.id] = dao.insertCustomer(it.copy(id = 0)) }
                items.forEach { dao.insertItem(it.copy(id = 0, customerId = idMap[it.customerId] ?: return@forEach)) }
                payments.forEach { dao.insertPayment(it.copy(id = 0, customerId = idMap[it.customerId] ?: return@forEach)) }
            }
            return@withContext ImportResult(customers.size, items.size, payments.size, errors)
        }

        val existing = dao.getAllCustomers()
        val map = existing.associateBy { it.phone?.takeIf { p -> p.isNotBlank() } ?: it.name }
        val idMap = mutableMapOf<Long, Long>()
        customers.forEach {
            val key = it.phone?.takeIf { p -> p.isNotBlank() } ?: it.name
            idMap[it.id] = map[key]?.id ?: dao.insertCustomer(it.copy(id = 0))
        }
        items.forEach { dao.insertItem(it.copy(id = 0, customerId = idMap[it.customerId] ?: return@forEach)) }
        payments.forEach { dao.insertPayment(it.copy(id = 0, customerId = idMap[it.customerId] ?: return@forEach)) }
        ImportResult(customers.size, items.size, payments.size, errors)
    }

    private fun escape(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun parseCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') { current.append('"'); i++ }
                else inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result += current.toString(); current.clear()
            } else current.append(c)
            i++
        }
        result += current.toString()
        return result
    }

    private fun String.toDoubleLocale(): Double = toDoubleOrNull() ?: toDouble(Locale.US)
    private fun Double.toCsv(): String = String.format(Locale.US, "%.2f", this)
}
