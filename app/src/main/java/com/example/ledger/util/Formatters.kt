package com.example.ledger.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val arabicSymbols = DecimalFormatSymbols(Locale("ar")).apply { groupingSeparator = ',' }
private val numberFormat = DecimalFormat("#,##0.##", arabicSymbols)

fun Double.pretty(): String = numberFormat.format(this)
fun Long.prettyDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(this))
