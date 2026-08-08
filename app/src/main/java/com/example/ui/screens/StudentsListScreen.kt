package com.example.ui.screens

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
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.FeeViewModel
import com.example.ui.components.StudentItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
    viewModel: FeeViewModel,
    students: List<Student>,
    payments: List<FeePayment>,
    onAddStudentClick: () -> Unit,
    onStudentClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedClassFilter by viewModel.selectedClassFilter.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val filteredList = remember(students, searchQuery, selectedClassFilter) {
        students.filter { student ->
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.fatherName.contains(searchQuery, ignoreCase = true) ||
                    student.rollNo.contains(searchQuery, ignoreCase = true) ||
                    student.mobile.contains(searchQuery)

            val matchesClass = selectedClassFilter == "All" || student.className.equals(selectedClassFilter, ignoreCase = true)

            matchesSearch && matchesClass
        }
    }

    val classList = remember(students) {
        listOf("All") + students.map { it.className }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students Directory (${students.size})", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Class filter
            item {
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
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    classList.forEach { cls ->
                        FilterChip(
                            selected = selectedClassFilter == cls,
                            onClick = { viewModel.setClassFilter(cls) },
                            label = { Text(cls) }
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No students found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { student ->
                    val isPaid = viewModel.isFeePaid(student.id, selectedMonth, selectedYear, payments)
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
