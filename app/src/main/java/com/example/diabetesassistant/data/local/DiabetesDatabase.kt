package com.example.diabetesassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.local.dao.FoodRecordDao
import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.dao.UserDao
import com.example.diabetesassistant.data.local.entity.CookingMethodTable
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.data.local.entity.UsdaCache
import com.example.diabetesassistant.data.local.entity.User

/**
 * 纯私有代谢健康 Room 主数据库。
 */
@Database(
    entities = [
        User::class,
        HealthProfile::class,
        FoodRecord::class,
        CookingMethodTable::class,
        UsdaCache::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DiabetesDatabase : RoomDatabase() {
    /**
     * 用户 DAO。
     */
    abstract fun userDao(): UserDao

    /**
     * 健康档案 DAO。
     */
    abstract fun healthProfileDao(): HealthProfileDao

    /**
     * 档案 DAO。
     */
    abstract fun foodRecordDao(): FoodRecordDao

    /**
     * 烹饪修正 DAO。
     */
    abstract fun cookingMethodDao(): CookingMethodDao

    /**
     * USDA 缓存 DAO。
     */
    abstract fun usdaCacheDao(): UsdaCacheDao

    companion object {
        /**
         * 数据库文件名。
         */
        const val DATABASE_NAME: String = "diabetes_assistant.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS community_posts")
                db.execSQL("DROP TABLE IF EXISTS comments")
                db.execSQL("DROP TABLE IF EXISTS favorites")
            }
        }

        /**
         * 预填充烹饪修正值（纯物理真实参数）。
         */
        fun prepopulateCookingMethods(db: SupportSQLiteDatabase) {
            val statements = listOf(
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('蒸', 0)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('煮', 0)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('白灼', 0)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('凉拌', 1)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('煲汤', 1)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('炒', 2)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('烧烤', 2)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('烤', 2)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('爆炒', 3)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('干煸', 3)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('红烧', 5)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('油炸', 6)",
                "INSERT OR REPLACE INTO cooking_method_table (cooking_method, correction_value) VALUES ('糖醋', 8)",
            )
            statements.forEach(db::execSQL)
        }
    }
}
