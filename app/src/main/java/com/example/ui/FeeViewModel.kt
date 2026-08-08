package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FeePayment
import com.example.data.model.SchoolSettings
import com.example.data.model.Student
import com.example.data.repository.FeeRepository
import com.example.util.FeeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class Screen {
    object Home : Screen()
    object Students : Screen()
    object UnpaidReminders : Screen()
    data class StudentDetail(val studentId: Int) : Screen()
    object Settings : Screen()
}

class FeeViewModel(private val repository: FeeRepository) : ViewModel() {

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Filters
    private val calendar = Calendar.getInstance()
    val currentSystemMonth = calendar.get(Calendar.MONTH) + 1 // 1..12
    val currentSystemYear = calendar.get(Calendar.YEAR)

    private val _selectedMonth = MutableStateFlow(
        if (currentSystemMonth in 1..12) currentSystemMonth else 8
    )
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(
        if (currentSystemYear >= 2024) currentSystemYear else 2026
    )
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow("All")
    val selectedClassFilter: StateFlow<String> = _selectedClassFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("All") // "All", "Paid", "Unpaid"
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    // Database flows
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<FeePayment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schoolSettings: StateFlow<SchoolSettings> = repository.schoolSettings
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SchoolSettings()
        )

    // Dialog state for paying fees
    private val _studentToPayFee = MutableStateFlow<Student?>(null)
    val studentToPayFee: StateFlow<Student?> = _studentToPayFee.asStateFlow()

    // Dialog state for adding/editing student
    private val _showAddEditStudentSheet = MutableStateFlow(false)
    val showAddEditStudentSheet: StateFlow<Boolean> = _showAddEditStudentSheet.asStateFlow()

    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateSampleDataIfEmpty()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSelectedMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setClassFilter(className: String) {
        _selectedClassFilter.value = className
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun openPayFeeDialog(student: Student) {
        _studentToPayFee.value = student
    }

    fun closePayFeeDialog() {
        _studentToPayFee.value = null
    }

    fun openAddStudentSheet() {
        _editingStudent.value = null
        _showAddEditStudentSheet.value = true
    }

    fun openEditStudentSheet(student: Student) {
        _editingStudent.value = student
        _showAddEditStudentSheet.value = true
    }

    fun closeAddEditStudentSheet() {
        _showAddEditStudentSheet.value = false
        _editingStudent.value = null
    }

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
            closeAddEditStudentSheet()
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (_currentScreen.value is Screen.StudentDetail) {
                _currentScreen.value = Screen.Students
            }
        }
    }

    fun markFeePayment(
        studentId: Int,
        month: Int,
        year: Int,
        isPaid: Boolean,
        amount: Double,
        paymentMode: String? = "Cash",
        receiptNo: String? = null,
        remarks: String? = null
    ) {
        viewModelScope.launch {
            val payment = FeePayment(
                studentId = studentId,
                year = year,
                month = month,
                isPaid = isPaid,
                amount = amount,
                paidDate = if (isPaid) System.currentTimeMillis() else null,
                paymentMode = if (isPaid) paymentMode else null,
                receiptNo = if (isPaid && receiptNo.isNullOrBlank()) "REC-${System.currentTimeMillis().toString().takeLast(6)}" else receiptNo,
                remarks = remarks
            )
            repository.insertOrUpdatePayment(payment)
            closePayFeeDialog()
        }
    }

    fun updateSettings(settings: SchoolSettings) {
        viewModelScope.launch {
            repository.updateSchoolSettings(settings)
        }
    }

    fun sendWhatsAppReminder(
        context: Context,
        student: Student,
        month: Int,
        year: Int,
        dueAmount: Double
    ) {
        val settings = schoolSettings.value
        val monthName = FeeUtils.getMonthName(month, inHindi = false)
        
        var message = settings.whatsappTemplate
            .replace("{FATHER_NAME}", student.fatherName)
            .replace("{STUDENT_NAME}", student.name)
            .replace("{CLASS}", student.className)
            .replace("{SECTION}", student.section)
            .replace("{MONTH}", monthName)
            .replace("{YEAR}", year.toString())
            .replace("{AMOUNT}", String.format("%.0f", dueAmount))
            .replace("{SCHOOL_NAME}", settings.schoolName)

        FeeUtils.sendWhatsAppReminder(context, student.mobile, message)
    }

    /**
     * Check if a specific student has paid for given month & year
     */
    fun isFeePaid(studentId: Int, month: Int, year: Int, paymentsList: List<FeePayment>): Boolean {
        return paymentsList.any { it.studentId == studentId && it.month == month && it.year == year && it.isPaid }
    }

    /**
     * Get payment record for student for a specific month
     */
    fun getPaymentRecord(studentId: Int, month: Int, year: Int, paymentsList: List<FeePayment>): FeePayment? {
        return paymentsList.firstOrNull { it.studentId == studentId && it.month == month && it.year == year }
    }
}

class FeeViewModelFactory(private val repository: FeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
