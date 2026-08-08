package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FeePaymentDao
import com.example.data.dao.SchoolSettingsDao
import com.example.data.dao.StudentDao
import com.example.data.model.FeePayment
import com.example.data.model.SchoolSettings
import com.example.data.model.Student

@Database(
    entities = [Student::class, FeePayment::class, SchoolSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun feePaymentDao(): FeePaymentDao
    abstract fun schoolSettingsDao(): SchoolSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_fee_manager_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
