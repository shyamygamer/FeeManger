package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import com.example.util.FeeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayFeeDialog(
    student: Student,
    selectedMonth: Int,
    selectedYear: Int,
    isCurrentlyPaid: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int, isPaid: Boolean, amount: Double, mode: String, receipt: String, remarks: String) -> Unit
) {
    var month by remember { mutableStateOf(selectedMonth) }
    var year by remember { mutableStateOf(selectedYear) }
    var isPaid by remember { mutableStateOf(!isCurrentlyPaid) }
    var amountText by remember { mutableStateOf(student.monthlyFee.toString()) }
    var paymentMode by remember { mutableStateOf("Cash") }
    var receiptNo by remember { mutableStateOf("REC-${System.currentTimeMillis().toString().takeLast(6)}") }
    var remarks by remember { mutableStateOf("") }

    var expandedMonth by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isPaid) "Record Fee Payment" else "Update Payment Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${student.name} (Father: ${student.fatherName})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth }
                ) {
                    OutlinedTextField(
                        value = "${FeeUtils.getMonthName(month, false)} ($year)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        FeeUtils.ACADEMIC_MONTHS.forEach { monthInfo ->
                            DropdownMenuItem(
                                text = { Text(monthInfo.englishName) },
                                onClick = {
                                    month = monthInfo.monthNumber
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                // Payment Status Switch / Radio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPaid) "Status: PAID" else "Status: UNPAID",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Switch(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it }
                    )
                }

                if (isPaid) {
                    // Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Fee Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Payment Mode Segmented Chips
                    Column {
                        Text(
                            text = "Payment Mode",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Cash", "UPI", "Bank", "Cheque").forEach { mode ->
                                FilterChip(
                                    selected = paymentMode == mode,
                                    onClick = { paymentMode = mode },
                                    label = { Text(mode) },
                                    leadingIcon = if (paymentMode == mode) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Receipt No
                    OutlinedTextField(
                        value = receiptNo,
                        onValueChange = { receiptNo = it },
                        label = { Text("Receipt No") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Remarks
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: student.monthlyFee
                    onConfirm(month, year, isPaid, amount, paymentMode, receiptNo, remarks)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isPaid) "Save Payment" else "Update Status")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
