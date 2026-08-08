package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.FeeRepository
import com.example.ui.FeeViewModel
import com.example.ui.FeeViewModelFactory
import com.example.ui.Screen
import com.example.ui.components.AddEditStudentSheet
import com.example.ui.components.PayFeeDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SchoolSettingsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsListScreen
import com.example.ui.screens.UnpaidRemindersScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: FeeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FeeRepository(
            studentDao = database.studentDao(),
            feePaymentDao = database.feePaymentDao(),
            schoolSettingsDao = database.schoolSettingsDao()
        )
        val factory = FeeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FeeViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: FeeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val studentToPay by viewModel.studentToPayFee.collectAsState()
    val showAddEditSheet by viewModel.showAddEditStudentSheet.collectAsState()
    val editingStudent by viewModel.editingStudent.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    // Calculate unpaid count for bottom nav badge
    val unpaidCount = remember(allStudents, allPayments, selectedMonth, selectedYear) {
        allStudents.count { student ->
            !viewModel.isFeePaid(student.id, selectedMonth, selectedYear, allPayments)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen !is Screen.StudentDetail && currentScreen !is Screen.Settings) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = FontWeight.SemiBold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.UnpaidReminders,
                        onClick = { viewModel.navigateTo(Screen.UnpaidReminders) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unpaidCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$unpaidCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                            }
                        },
                        label = { Text("Reminders", fontWeight = FontWeight.SemiBold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Students,
                        onClick = { viewModel.navigateTo(Screen.Students) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                        label = { Text("Students", fontWeight = FontWeight.SemiBold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        students = allStudents,
                        payments = allPayments,
                        onAddStudentClick = { viewModel.openAddStudentSheet() },
                        onStudentClick = { id -> viewModel.navigateTo(Screen.StudentDetail(id)) }
                    )
                }

                is Screen.UnpaidReminders -> {
                    UnpaidRemindersScreen(
                        viewModel = viewModel,
                        students = allStudents,
                        payments = allPayments,
                        onStudentClick = { id -> viewModel.navigateTo(Screen.StudentDetail(id)) }
                    )
                }

                is Screen.Students -> {
                    StudentsListScreen(
                        viewModel = viewModel,
                        students = allStudents,
                        payments = allPayments,
                        onAddStudentClick = { viewModel.openAddStudentSheet() },
                        onStudentClick = { id -> viewModel.navigateTo(Screen.StudentDetail(id)) }
                    )
                }

                is Screen.StudentDetail -> {
                    StudentDetailScreen(
                        studentId = screen.studentId,
                        viewModel = viewModel,
                        students = allStudents,
                        payments = allPayments,
                        onBackClick = { viewModel.navigateTo(Screen.Home) }
                    )
                }

                is Screen.Settings -> {
                    SchoolSettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateTo(Screen.Home) }
                    )
                }
            }
        }

        // Global Pay Fee Dialog
        studentToPay?.let { student ->
            val isPaid = viewModel.isFeePaid(student.id, selectedMonth, selectedYear, allPayments)
            PayFeeDialog(
                student = student,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                isCurrentlyPaid = isPaid,
                onDismiss = { viewModel.closePayFeeDialog() },
                onConfirm = { month, year, isPaidStatus, amount, mode, receipt, remarks ->
                    viewModel.markFeePayment(
                        studentId = student.id,
                        month = month,
                        year = year,
                        isPaid = isPaidStatus,
                        amount = amount,
                        paymentMode = mode,
                        receiptNo = receipt,
                        remarks = remarks
                    )
                }
            )
        }

        // Global Add / Edit Student Modal Sheet
        if (showAddEditSheet) {
            AddEditStudentSheet(
                student = editingStudent,
                onDismiss = { viewModel.closeAddEditStudentSheet() },
                onSave = { student -> viewModel.saveStudent(student) }
            )
        }
    }
}
