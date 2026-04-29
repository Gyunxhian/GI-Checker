package com.example.diabetesassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
    version = 3,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE community_posts ADD COLUMN post_type TEXT NOT NULL DEFAULT 'FREE_TEXT'"
                )
                db.execSQL(
                    "ALTER TABLE community_posts ADD COLUMN archive_record_id INTEGER"
                )
            }
        }

        /**
         * 预填充示例用户与帖子。
         */
        fun prepopulateDefaultUsersAndPosts(db: SupportSQLiteDatabase) {
            val userStatements = listOf(
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (11, '控糖小青', NULL, 'seed11@demo.local', 'seed_pwd', 1710000000000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (12, '晨光燕麦', NULL, 'seed12@demo.local', 'seed_pwd', 1710000100000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (13, '番茄牛肉饭', NULL, 'seed13@demo.local', 'seed_pwd', 1710000200000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (14, '晚餐慢慢来', NULL, 'seed14@demo.local', 'seed_pwd', 1710000300000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (15, '坚果补给站', NULL, 'seed15@demo.local', 'seed_pwd', 1710000400000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (16, '轻食备餐君', NULL, 'seed16@demo.local', 'seed_pwd', 1710000500000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (21, '营养优选铺', NULL, 'seed21@demo.local', 'seed_pwd', 1710000600000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (22, '控量工具屋', NULL, 'seed22@demo.local', 'seed_pwd', 1710000700000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (23, '记录手册集', NULL, 'seed23@demo.local', 'seed_pwd', 1710000800000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (24, '全麦工坊', NULL, 'seed24@demo.local', 'seed_pwd', 1710000900000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (25, '厨房精量', NULL, 'seed25@demo.local', 'seed_pwd', 1710001000000)",
                "INSERT OR IGNORE INTO users (id, nickname, phone, email, password, created_at) VALUES (26, '低糖饮品集', NULL, 'seed26@demo.local', 'seed_pwd', 1710001100000)",
            )
            val postStatements = listOf(
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9001, 11, '低GI午餐：鸡胸藜麦碗', '鸡胸肉120g + 藜麦80g + 西兰花100g，清炒少油。', NULL, '餐后平稳', 'FREE_TEXT', NULL, 126, 18, 1711000000000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9002, 12, '早餐替换：燕麦酸奶杯', '无糖酸奶+即食燕麦+蓝莓，控制总碳水约32g。', NULL, '推荐早餐', 'FREE_TEXT', NULL, 89, 13, 1711000100000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9003, 13, '番茄牛肉意面（控量版）', '全麦意面70g，搭配番茄牛肉酱，控制油盐。', NULL, '碳水可控', 'FREE_TEXT', NULL, 77, 9, 1711000200000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9004, 14, '晚餐示例：清蒸鱼+杂粮饭', '杂粮饭半碗，搭配清蒸鱼和凉拌菠菜。', NULL, '晚餐参考', 'FREE_TEXT', NULL, 93, 15, 1711000300000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9005, 15, '加餐建议：坚果+黄瓜', '下午加餐少量原味坚果，避免血糖大起伏。', NULL, '加餐指南', 'FREE_TEXT', NULL, 58, 7, 1711000400000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9006, 16, '一周轻食备餐分享', '提前分装主食和蛋白质，减少外卖频率。', NULL, '备餐', 'FREE_TEXT', NULL, 110, 21, 1711000500000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9101, 21, '无糖高纤燕麦片', '每份30g，膳食纤维丰富，适合早餐替换。', NULL, '¥29.9', 'MALL', NULL, 45, 6, 1711000600000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9102, 22, '分隔控量餐盒（3件装）', '按荤素比例备餐，方便记录每餐摄入。', NULL, '¥39.0', 'MALL', NULL, 51, 8, 1711000700000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9103, 23, '便携血糖记录手册', '纸质随手记，含餐前餐后与症状追踪页。', NULL, '¥12.8', 'MALL', NULL, 32, 4, 1711000800000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9104, 24, '低GI全麦面包', '每片独立包装，适合加班或运动后补给。', NULL, '¥24.5', 'MALL', NULL, 39, 5, 1711000900000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9105, 25, '厨房电子食物秤', '支持克重精准测量，做碳水估算更稳定。', NULL, '¥59.0', 'MALL', NULL, 67, 11, 1711001000000)",
                "INSERT OR IGNORE INTO community_posts (id, user_id, title, content, image_path, blood_sugar_tag, post_type, archive_record_id, like_count, comment_count, created_at) VALUES (9106, 26, '代糖冲饮组合', '0蔗糖冲饮，控制热量与负担。', NULL, '¥33.6', 'MALL', NULL, 40, 6, 1711001100000)",
            )
            val commentStatements = listOf(
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50001, '这个搭配我试了三天，餐后血糖确实平稳。', 12, 9001, 1711200000000)",
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50002, '藜麦可以换成糙米吗？口感会不会更好？', 14, 9001, 1711200100000)",
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50003, '早餐杯建议提前一晚冷藏，早上更省事。', 16, 9002, 1711200200000)",
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50004, '全麦意面这个份量对我刚好，感谢分享。', 11, 9003, 1711200300000)",
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50005, '这款餐盒分区很实用，备餐效率高。', 25, 9102, 1711200400000)",
                "INSERT OR IGNORE INTO comments (id, content, user_id, post_id, created_at) VALUES (50006, '电子秤称重很准，做碳水估算方便。', 23, 9105, 1711200500000)",
            )
            userStatements.forEach(db::execSQL)
            postStatements.forEach(db::execSQL)
            commentStatements.forEach(db::execSQL)
        }

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
