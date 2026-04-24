package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 健康档案实体。
 */
@Entity(
    tableName = "health_profiles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("user_id", unique = true)],
)
data class HealthProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "diabetes_type")
    val diabetesType: String,
    @ColumnInfo(name = "insulin_type")
    val insulinType: String,
    @ColumnInfo(name = "insulin_min")
    val insulinMin: Float?,
    @ColumnInfo(name = "insulin_max")
    val insulinMax: Float?,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float?,
    @ColumnInfo(name = "height_cm")
    val heightCm: Float?,
)
