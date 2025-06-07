package com.example.oralenglishgpt.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val settingKey: String,
    val value: String
)