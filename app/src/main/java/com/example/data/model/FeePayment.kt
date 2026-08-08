package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val year: Int,
    val month: Int, // 1 = January, 2 = February, ..., 12 = December
    val isPaid: Boolean = false,
    val amount: Double,
    val paidDate: Long? = null,
    val paymentMode: String? = null, // "Cash", "UPI", "Bank Transfer", "Cheque"
    val receiptNo: String? = null,
    val remarks: String? = null
)
