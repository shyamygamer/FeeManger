package com.example.data.dao

import androidx.room.*
import com.example.data.model.SchoolSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolSettingsDao {
    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SchoolSettings?>

    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SchoolSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SchoolSettings)
}
