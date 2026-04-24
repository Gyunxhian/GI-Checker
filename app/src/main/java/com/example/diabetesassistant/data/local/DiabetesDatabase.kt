package com.example.diabetesassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.diabetesassistant.data.local.dao.CommentDao
import com.example.diabetesassistant.data.local.dao.CommunityPostDao
import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.local.dao.FavoriteDao
import com.example.diabetesassistant.data.local.dao.FoodRecordDao
import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.dao.UserDao
import com.example.diabetesassistant.data.local.entity.Comment
import com.example.diabetesassistant.data.local.entity.CommunityPost
import com.example.diabetesassistant.data.local.entity.CookingMethodTable
import com.example.diabetesassistant.data.local.entity.Favorite
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.data.local.entity.UsdaCache
import com.example.diabetesassistant.data.local.entity.User

/**
 * Room 主数据库。
 */
@Database(
    entities = [
        User::class,
        HealthProfile::class,
        FoodRecord::class,
        CommunityPost::class,
        Comment::class,
        Favorite::class,
        CookingMethodTable::class,
        UsdaCache::class,
    ],
    version = 1,
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
     * 帖子 DAO。
     */
    abstract fun communityPostDao(): CommunityPostDao

    /**
     * 评论 DAO。
     */
    abstract fun commentDao(): CommentDao

    /**
     * 收藏 DAO。
     */
    abstract fun favoriteDao(): FavoriteDao

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

        /**
         * 预填充烹饪修正值。
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
