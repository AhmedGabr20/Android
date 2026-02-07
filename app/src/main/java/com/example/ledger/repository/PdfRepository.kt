package com.example.ledger.repository

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.ledger.data.AppDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfRepository(private val context: Context, private val db: AppDatabase) {
    suspend fun generateCustomerStatement(customerId: Long): File {
        val dao = db.ledgerDao()
        val customer = dao.getAllCustomers().first { it.id == customerId }
        val items = dao.getAllItems().filter { it.customerId == customerId }
        val payments = dao.getAllPayments().filter { it.customerId == customerId }
        val itemsTotal = items.sumOf { it.total }
        val paid = payments.sumOf { it.amount }
        val remaining = itemsTotal - paid

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 28f }
        var y = 80f
        canvas.drawText("كشف حساب عميل", 900f, y, paint)
        y += 40
        canvas.drawText("الاسم: ${customer.name}", 900f, y, paint)
        y += 40
        canvas.drawText("الهاتف: ${customer.phone ?: "-"}", 900f, y, paint)
        y += 40
        canvas.drawText("التاريخ: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())}", 900f, y, paint)
        y += 60
        canvas.drawText("إجمالي: $itemsTotal | مدفوع: $paid | متبقي: $remaining", 200f, y, paint)
        y += 60
        canvas.drawText("البنود:", 1000f, y, paint)
        y += 40
        items.forEach {
            canvas.drawText("${fmt(it.date)} | ${it.title.take(20)} | ${it.qty}x${it.price} = ${it.total}", 100f, y, paint)
            y += 35
        }
        y += 20
        canvas.drawText("الدفعات:", 1000f, y, paint)
        y += 40
        payments.forEach {
            canvas.drawText("${fmt(it.date)} | ${it.amount}", 100f, y, paint)
            y += 35
        }
        canvas.drawText("تم إنشاء التقرير بواسطة التطبيق - 1/1", 100f, 1650f, paint)
        document.finishPage(page)

        val file = File(context.cacheDir, "statement_${customerId}_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    suspend fun generateAllCustomersPdf(): File {
        val dao = db.ledgerDao()
        val customers = dao.getAllCustomers()
        val document = PdfDocument()
        val paint = Paint().apply { textSize = 26f }
        customers.forEachIndexed { index, c ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(1200, 1700, index + 1).create())
            val itemsTotal = dao.getAllItems().filter { it.customerId == c.id }.sumOf { it.total }
            val paid = dao.getAllPayments().filter { it.customerId == c.id }.sumOf { it.amount }
            page.canvas.drawText("${c.name} | إجمالي: $itemsTotal | مدفوع: $paid | متبقي: ${itemsTotal - paid}", 100f, 100f, paint)
            page.canvas.drawText("تم إنشاء التقرير بواسطة التطبيق - ${index + 1}/${customers.size}", 100f, 1650f, paint)
            document.finishPage(page)
        }
        val file = File(context.cacheDir, "customers_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    fun fileUri(file: File) = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun fmt(time: Long) = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(time))
}
