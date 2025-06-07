package com.example.oralenglishgpt.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oralenglishgpt.database.entity.SettingsEntity

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE settingKey = 'auto_play'")
    suspend fun getAutoPlaySetting(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAutoPlaySetting(settings: SettingsEntity)
}