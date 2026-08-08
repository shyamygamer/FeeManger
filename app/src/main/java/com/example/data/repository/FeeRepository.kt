package com.example.data.repository

import com.example.data.dao.FeePaymentDao
import com.example.data.dao.SchoolSettingsDao
import com.example.data.dao.StudentDao
import com.example.data.model.FeePayment
import com.example.data.model.SchoolSettings
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FeeRepository(
    private val studentDao: StudentDao,
    private val feePaymentDao: FeePaymentDao,
    private val schoolSettingsDao: SchoolSettingsDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allPayments: Flow<List<FeePayment>> = feePaymentDao.getAllPayments()
    val schoolSettings: Flow<SchoolSettings?> = schoolSettingsDao.getSettings()

    fun getStudentById(id: Int): Flow<Student?> = studentDao.getStudentById(id)

    fun getPaymentsForStudent(studentId: Int): Flow<List<FeePayment>> =
        feePaymentDao.getPaymentsForStudent(studentId)

    fun getPaymentsForStudentYear(studentId: Int, year: Int): Flow<List<FeePayment>> =
        feePaymentDao.getPaymentsForStudentYear(studentId, year)

    fun getPaymentsForMonthAndYear(month: Int, year: Int): Flow<List<FeePayment>> =
        feePaymentDao.getPaymentsForMonthAndYear(month, year)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) {
        feePaymentDao.deletePaymentsForStudent(student.id)
        studentDao.deleteStudent(student)
    }

    suspend fun insertOrUpdatePayment(payment: FeePayment) {
        feePaymentDao.insertOrUpdatePayment(payment)
    }

    suspend fun updateSchoolSettings(settings: SchoolSettings) {
        schoolSettingsDao.insertOrUpdate(settings)
    }

    /**
     * Check if database is empty and populate initial sample school students and payments if needed
     */
    suspend fun prepopulateSampleDataIfEmpty() {
        val existingStudents = studentDao.getAllStudents().firstOrNull()
        if (existingStudents.isNullOrEmpty()) {
            // Add default school settings
            schoolSettingsDao.insertOrUpdate(
                SchoolSettings(
                    id = 1,
                    schoolName = "Saraswati Public School",
                    schoolPhone = "9876543210",
                    schoolAddress = "Vidya Nagar, New Delhi",
                    academicSession = "2026-2027"
                )
            )

            // Add sample students
            val sampleStudents = listOf(
                Student(
                    name = "Rahul Kumar",
                    fatherName = "Rakesh Kumar",
                    className = "Class 6",
                    section = "A",
                    rollNo = "101",
                    mobile = "9876543210",
                    address = "House No. 45, Gandhi Nagar",
                    avatarPreset = 1,
                    monthlyFee = 1500.0
                ),
                Student(
                    name = "Priya Sharma",
                    fatherName = "Sunil Sharma",
                    className = "Class 6",
                    section = "A",
                    rollNo = "102",
                    mobile = "9812345678",
                    address = "Plot 12, Vikas Puri",
                    avatarPreset = 2,
                    monthlyFee = 1500.0
                ),
                Student(
                    name = "Amit Singh",
                    fatherName = "Vikram Singh",
                    className = "Class 6",
                    section = "B",
                    rollNo = "103",
                    mobile = "9765432109",
                    address = "Street No. 3, Ashok Vihar",
                    avatarPreset = 3,
                    monthlyFee = 1500.0
                ),
                Student(
                    name = "Ananya Verma",
                    fatherName = "Deepak Verma",
                    className = "Class 7",
                    section = "A",
                    rollNo = "201",
                    mobile = "9988776655",
                    address = "Sector 4, Model Town",
                    avatarPreset = 4,
                    monthlyFee = 1800.0
                ),
                Student(
                    name = "Sonu Yadav",
                    fatherName = "Ramsevak Yadav",
                    className = "Class 8",
                    section = "A",
                    rollNo = "301",
                    mobile = "9123456789",
                    address = "Raipur, Post Office Road",
                    avatarPreset = 5,
                    monthlyFee = 2000.0
                )
            )

            val currentYear = 2026
            val samplePayments = mutableListOf<FeePayment>()

            sampleStudents.forEach { student ->
                val id = studentDao.insertStudent(student).toInt()
                
                // For Rahul Kumar (paid Apr, May, Jun, Jul; unpaid Aug)
                if (student.name == "Rahul Kumar") {
                    listOf(4, 5, 6, 7).forEach { month ->
                        samplePayments.add(
                            FeePayment(
                                studentId = id,
                                year = currentYear,
                                month = month,
                                isPaid = true,
                                amount = student.monthlyFee,
                                paidDate = System.currentTimeMillis() - (8 - month) * 30L * 24 * 60 * 60 * 1000,
                                paymentMode = "Cash",
                                receiptNo = "REC-2026-0$month$id"
                            )
                        )
                    }
                    // Unpaid for Aug
                    samplePayments.add(
                        FeePayment(
                            studentId = id,
                            year = currentYear,
                            month = 8,
                            isPaid = false,
                            amount = student.monthlyFee
                        )
                    )
                } 
                // For Priya Sharma (paid all months up to current)
                else if (student.name == "Priya Sharma") {
                    listOf(4, 5, 6, 7, 8).forEach { month ->
                        samplePayments.add(
                            FeePayment(
                                studentId = id,
                                year = currentYear,
                                month = month,
                                isPaid = true,
                                amount = student.monthlyFee,
                                paidDate = System.currentTimeMillis() - (8 - month) * 30L * 24 * 60 * 60 * 1000,
                                paymentMode = "UPI",
                                receiptNo = "UPI-2026-0$month$id"
                            )
                        )
                    }
                }
                // For Amit Singh (unpaid Aug)
                else {
                    listOf(4, 5, 6).forEach { month ->
                        samplePayments.add(
                            FeePayment(
                                studentId = id,
                                year = currentYear,
                                month = month,
                                isPaid = true,
                                amount = student.monthlyFee,
                                paidDate = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000,
                                paymentMode = "Cash",
                                receiptNo = "REC-10$month"
                            )
                        )
                    }
                    samplePayments.add(
                        FeePayment(
                            studentId = id,
                            year = currentYear,
                            month = 8,
                            isPaid = false,
                            amount = student.monthlyFee
                        )
                    )
                }
            }

            feePaymentDao.insertPayments(samplePayments)
        }
    }
}
