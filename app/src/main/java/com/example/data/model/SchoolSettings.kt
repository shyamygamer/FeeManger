package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_settings")
data class SchoolSettings(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "Saraswati Public School",
    val schoolPhone: String = "9876543210",
    val schoolAddress: String = "Main Road, School Campus",
    val academicSession: String = "2026-2027",
    val whatsappTemplate: String = "Dear {FATHER_NAME},\n\nThis is a friendly reminder that the fee for {STUDENT_NAME} (Class: {CLASS}-{SECTION}) for {MONTH} {YEAR} of amount ₹{AMOUNT} is pending.\n\nKindly clear the dues at your earliest convenience.\n\nThank you,\n{SCHOOL_NAME}"
)
