package com.example.ledger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ledger.util.prettyDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(value: Long, onDateSelected: (Long) -> Unit, label: String) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value.prettyDate(),
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth().clickable { show = true },
        label = { Text(label) }
    )
    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = value)
        DatePickerDialog(onDismissRequest = { show = false }, confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let(onDateSelected)
                show = false
            }) { Text("تأكيد") }
        }, dismissButton = { TextButton(onClick = { show = false }) { Text("إلغاء") } }) {
            DatePicker(state = state)
        }
    }
}
