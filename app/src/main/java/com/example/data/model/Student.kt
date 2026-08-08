package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fatherName: String,
    val className: String,
    val section: String,
    val rollNo: String = "",
    val mobile: String,
    val address: String,
    val photoUri: String? = null,
    val avatarPreset: Int = 0, // Preset avatar index if photoUri is empty
    val monthlyFee: Double = 1500.0,
    val createdAt: Long = System.currentTimeMillis()
)
