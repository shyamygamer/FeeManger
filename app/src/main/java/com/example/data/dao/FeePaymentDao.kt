package com.example.data.dao

import androidx.room.*
import com.example.data.model.FeePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface FeePaymentDao {
    @Query("SELECT * FROM fee_payments")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY year DESC, month DESC")
    fun getPaymentsForStudent(studentId: Int): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId AND year = :year ORDER BY month ASC")
    fun getPaymentsForStudentYear(studentId: Int, year: Int): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE month = :month AND year = :year")
    fun getPaymentsForMonthAndYear(month: Int, year: Int): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId AND month = :month AND year = :year LIMIT 1")
    suspend fun getPaymentRecord(studentId: Int, month: Int, year: Int): FeePayment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePayment(payment: FeePayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<FeePayment>)

    @Query("DELETE FROM fee_payments WHERE studentId = :studentId")
    suspend fun deletePaymentsForStudent(studentId: Int)
}
