package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.FeeViewModel
import com.example.ui.Screen
import com.example.ui.components.StudentItemCard
import com.example.util.FeeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FeeViewModel,
    students: List<Student>,
    payments: List<FeePayment>,
    onAddStudentClick: () -> Unit,
    onStudentClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val schoolSettings by viewModel.schoolSettings.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedClassFilter by viewModel.selectedClassFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    var showMonthMenu by remember { mutableStateOf(false) }

    // Filter students
    val filteredStudents = remember(students, payments, selectedMonth, selectedYear, searchQuery, selectedClassFilter, selectedStatusFilter) {
        students.filter { student ->
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.fatherName.contains(searchQuery, ignoreCase = true) ||
                    student.rollNo.contains(searchQuery, ignoreCase = true) ||
                    student.mobile.contains(searchQuery)

            val matchesClass = selectedClassFilter == "All" || student.className.equals(selectedClassFilter, ignoreCase = true)

            val isPaid = viewModel.isFeePaid(student.id, selectedMonth, selectedYear, payments)
            val matchesStatus = when (selectedStatusFilter) {
                "Paid" -> isPaid
                "Unpaid" -> !isPaid
                else -> true
            }

            matchesSearch && matchesClass && matchesStatus
        }
    }

    // Stats calculations for selected month
    val totalStudents = students.size
    val paidCountForMonth = students.count { viewModel.isFeePaid(it.id, selectedMonth, selectedYear, payments) }
    val unpaidCountForMonth = totalStudents - paidCountForMonth

    val totalCollected = students.filter { viewModel.isFeePaid(it.id, selectedMonth, selectedYear, payments) }
        .sumOf { it.monthlyFee }

    val totalPendingAmount = students.filter { !viewModel.isFeePaid(it.id, selectedMonth, selectedYear, payments) }
        .sumOf { it.monthlyFee }

    // Available unique classes
    val classOptions = remember(students) {
        listOf("All") + students.map { it.className }.distinct().sorted()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Student", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // School Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = schoolSettings.schoolName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Session: ${schoolSettings.academicSession} • School Fee Management",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.navigateTo(Screen.Settings) },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Month Selector Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Month: ${FeeUtils.getMonthName(selectedMonth, false)} ($selectedYear)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            TextButton(onClick = { showMonthMenu = true }) {
                                Text("Change", fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false }
                            ) {
                                FeeUtils.ACADEMIC_MONTHS.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.englishName) },
                                        onClick = {
                                            viewModel.setSelectedMonth(m.monthNumber)
                                            showMonthMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBadgeCard(
                        title = "Total Students",
                        value = "$totalStudents",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF3B82F6)
                    )
                    StatBadgeCard(
                        title = "Fee Paid",
                        value = "$paidCountForMonth",
                        subtitle = FeeUtils.formatCurrency(totalCollected),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF10B981)
                    )
                    StatBadgeCard(
                        title = "Fee Pending",
                        value = "$unpaidCountForMonth",
                        subtitle = FeeUtils.formatCurrency(totalPendingAmount),
                        icon = Icons.Default.PendingActions,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search by student, father, or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Class Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Class: ",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        classOptions.forEach { cls ->
                            FilterChip(
                                selected = selectedClassFilter == cls,
                                onClick = { viewModel.setClassFilter(cls) },
                                label = { Text(cls) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Payment Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status: ",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf("All" to "All", "Paid" to "Paid", "Unpaid" to "Unpaid").forEach { (key, label) ->
                            FilterChip(
                                selected = selectedStatusFilter == key,
                                onClick = { viewModel.setStatusFilter(key) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when(key) {
                                        "Paid" -> Color(0xFFD1FAE5)
                                        "Unpaid" -> Color(0xFFFEE2E2)
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // Unpaid Warning Alert Banner
            if (unpaidCountForMonth > 0 && selectedStatusFilter != "Paid") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "$unpaidCountForMonth students have pending fees",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Send 1-click WhatsApp reminders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.navigateTo(Screen.UnpaidReminders) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reminders", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Student List Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Students List (${filteredStudents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Student Cards
            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No students found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    val isPaid = viewModel.isFeePaid(student.id, selectedMonth, selectedYear, payments)
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        StudentItemCard(
                            student = student,
                            isPaid = isPaid,
                            selectedMonth = selectedMonth,
                            selectedYear = selectedYear,
                            onCardClick = { onStudentClick(student.id) },
                            onPayFeeClick = { viewModel.openPayFeeDialog(student) },
                            onWhatsAppClick = {
                                viewModel.sendWhatsAppReminder(
                                    context = context,
                                    student = student,
                                    month = selectedMonth,
                                    year = selectedYear,
                                    dueAmount = student.monthlyFee
                                )
                            },
                            onEditClick = { viewModel.openEditStudentSheet(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadgeCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(135.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = color)
            }
        }
    }
}
