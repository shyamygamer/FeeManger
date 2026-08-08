package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.FeeViewModel
import com.example.ui.components.StudentAvatar
import com.example.util.FeeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnpaidRemindersScreen(
    viewModel: FeeViewModel,
    students: List<Student>,
    payments: List<FeePayment>,
    onStudentClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val schoolSettings by viewModel.schoolSettings.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var showMonthPicker by remember { mutableStateOf(false) }

    // Unpaid students list
    val unpaidStudents = remember(students, payments, selectedMonth, selectedYear) {
        students.filter { student ->
            !viewModel.isFeePaid(student.id, selectedMonth, selectedYear, payments)
        }
    }

    val totalUnpaidAmount = unpaidStudents.sumOf { it.monthlyFee }
    val monthName = FeeUtils.getMonthName(selectedMonth, inHindi = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WhatsApp Fee Reminders", fontWeight = FontWeight.Bold)
                        Text(
                            "$monthName $selectedYear • ${unpaidStudents.size} students pending",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showMonthPicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(monthName, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = showMonthPicker,
                        onDismissRequest = { showMonthPicker = false }
                    ) {
                        FeeUtils.ACADEMIC_MONTHS.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.englishName) },
                                onClick = {
                                    viewModel.setSelectedMonth(m.monthNumber)
                                    showMonthPicker = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Total Unpaid Fees ($monthName $selectedYear)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = FeeUtils.formatCurrency(totalUnpaidAmount),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = "${unpaidStudents.size} Unpaid",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Tap 'Go to WhatsApp' next to any student to open WhatsApp with a pre-formatted fee reminder message.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }

            // Message Template Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sample Message Template:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dear [Father Name], fee for [Student Name] (Class: [Class]-[Section]) for $monthName $selectedYear of amount ₹[Amount] is pending. Kindly pay - ${schoolSettings.schoolName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Unpaid Students List (${unpaidStudents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (unpaidStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("All Fees Paid!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text("No pending fee dues for $monthName $selectedYear.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF047857))
                        }
                    }
                }
            } else {
                items(unpaidStudents, key = { it.id }) { student ->
                    UnpaidStudentCard(
                        student = student,
                        monthName = monthName,
                        year = selectedYear,
                        onStudentClick = { onStudentClick(student.id) },
                        onPayClick = { viewModel.openPayFeeDialog(student) },
                        onSendWhatsApp = {
                            viewModel.sendWhatsAppReminder(
                                context = context,
                                student = student,
                                month = selectedMonth,
                                year = selectedYear,
                                dueAmount = student.monthlyFee
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UnpaidStudentCard(
    student: Student,
    monthName: String,
    year: Int,
    onStudentClick: () -> Unit,
    onPayClick: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    photoUri = student.photoUri,
                    studentName = student.name,
                    avatarPreset = student.avatarPreset,
                    size = 50.dp,
                    borderColor = Color(0xFFEF4444)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Father: ${student.fatherName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Class: ${student.className}-${student.section} • Mobile: ${student.mobile}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${student.monthlyFee.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = "Due ($monthName)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB91C1C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPayClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Collect Fee", fontSize = 12.sp)
                }

                // Dedicated Go to WhatsApp Button
                Button(
                    onClick = onSendWhatsApp,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go to WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}
