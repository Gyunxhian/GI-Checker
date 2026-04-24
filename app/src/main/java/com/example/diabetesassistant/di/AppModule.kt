package com.example.diabetesassistant.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.diabetesassistant.data.local.DiabetesDatabase
import com.example.diabetesassistant.data.local.dao.CommentDao
import com.example.diabetesassistant.data.local.dao.CommunityPostDao
import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.local.dao.FavoriteDao
import com.example.diabetesassistant.data.local.dao.FoodRecordDao
import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.dao.UserDao
import com.example.diabetesassistant.data.remote.DashScopeApi
import com.example.diabetesassistant.data.remote.UsdaApi
import com.example.diabetesassistant.data.remote.interceptor.RetryInterceptor
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.data.repository.AiRepositoryImpl
import com.example.diabetesassistant.data.repository.CommunityRepository
import com.example.diabetesassistant.data.repository.CommunityRepositoryImpl
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.data.repository.FoodRecordRepositoryImpl
import com.example.diabetesassistant.data.repository.NutritionRepository
import com.example.diabetesassistant.data.repository.NutritionRepositoryImpl
import com.example.diabetesassistant.data.repository.UserRepository
import com.example.diabetesassistant.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Hilt DI 主模块。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * 提供 Room 数据库。
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DiabetesDatabase {
        return Room.databaseBuilder(
            context,
            DiabetesDatabase::class.java,
            DiabetesDatabase.DATABASE_NAME,
        ).addCallback(
            object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    DiabetesDatabase.prepopulateCookingMethods(db)
                }
            },
        ).build()
    }

    /**
     * 提供用户 DAO。
     */
    @Provides
    fun provideUserDao(database: DiabetesDatabase): UserDao = database.userDao()

    /**
     * 提供健康档案 DAO。
     */
    @Provides
    fun provideHealthProfileDao(database: DiabetesDatabase): HealthProfileDao = database.healthProfileDao()

    /**
     * 提供档案 DAO。
     */
    @Provides
    fun provideFoodRecordDao(database: DiabetesDatabase): FoodRecordDao = database.foodRecordDao()

    /**
     * 提供帖子 DAO。
     */
    @Provides
    fun provideCommunityPostDao(database: DiabetesDatabase): CommunityPostDao = database.communityPostDao()

    /**
     * 提供评论 DAO。
     */
    @Provides
    fun provideCommentDao(database: DiabetesDatabase): CommentDao = database.commentDao()

    /**
     * 提供收藏 DAO。
     */
    @Provides
    fun provideFavoriteDao(database: DiabetesDatabase): FavoriteDao = database.favoriteDao()

    /**
     * 提供烹饪方式 DAO。
     */
    @Provides
    fun provideCookingMethodDao(database: DiabetesDatabase): CookingMethodDao = database.cookingMethodDao()

    /**
     * 提供 USDA 缓存 DAO。
     */
    @Provides
    fun provideUsdaCacheDao(database: DiabetesDatabase): UsdaCacheDao = database.usdaCacheDao()

    /**
     * 提供 DataStore。
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences.preferences_pb") },
        )
    }

    /**
     * 提供 DashScope OkHttpClient。
     */
    @Provides
    @Singleton
    @DashScopeClient
    fun provideDashScopeOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .addInterceptor(RetryInterceptor())
            .build()
    }

    /**
     * 提供 USDA OkHttpClient。
     */
    @Provides
    @Singleton
    @UsdaClient
    fun provideUsdaOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .addInterceptor(RetryInterceptor())
            .build()
    }

    /**
     * 提供 DashScope Retrofit。
     */
    @Provides
    @Singleton
    @DashScopeRetrofit
    fun provideDashScopeRetrofit(@DashScopeClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dashscope.aliyuncs.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 提供 USDA Retrofit。
     */
    @Provides
    @Singleton
    @UsdaRetrofit
    fun provideUsdaRetrofit(@UsdaClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 提供 DashScope API。
     */
    @Provides
    @Singleton
    fun provideDashScopeApi(@DashScopeRetrofit retrofit: Retrofit): DashScopeApi {
        return retrofit.create(DashScopeApi::class.java)
    }

    /**
     * 提供 USDA API。
     */
    @Provides
    @Singleton
    fun provideUsdaApi(@UsdaRetrofit retrofit: Retrofit): UsdaApi {
        return retrofit.create(UsdaApi::class.java)
    }

    /**
     * 提供用户仓库。
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        healthProfileDao: HealthProfileDao,
    ): UserRepository = UserRepositoryImpl(userDao, healthProfileDao)

    /**
     * 提供档案仓库。
     */
    @Provides
    @Singleton
    fun provideFoodRecordRepository(foodRecordDao: FoodRecordDao): FoodRecordRepository {
        return FoodRecordRepositoryImpl(foodRecordDao)
    }

    /**
     * 提供社区仓库。
     */
    @Provides
    @Singleton
    fun provideCommunityRepository(
        communityPostDao: CommunityPostDao,
        commentDao: CommentDao,
        favoriteDao: FavoriteDao,
    ): CommunityRepository = CommunityRepositoryImpl(communityPostDao, commentDao, favoriteDao)

    /**
     * 提供 AI 仓库。
     */
    @Provides
    @Singleton
    fun provideAiRepository(
        dashScopeApi: DashScopeApi,
        apiKeyProvider: com.example.diabetesassistant.data.remote.ApiKeyProvider,
    ): AiRepository = AiRepositoryImpl(dashScopeApi, apiKeyProvider)

    /**
     * 提供营养仓库。
     */
    @Provides
    @Singleton
    fun provideNutritionRepository(
        usdaApi: UsdaApi,
        usdaCacheDao: UsdaCacheDao,
        apiKeyProvider: com.example.diabetesassistant.data.remote.ApiKeyProvider,
    ): NutritionRepository = NutritionRepositoryImpl(usdaApi, usdaCacheDao, apiKeyProvider)
}

/**
 * DashScope OkHttp 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DashScopeClient

/**
 * USDA OkHttp 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsdaClient

/**
 * DashScope Retrofit 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DashScopeRetrofit

/**
 * USDA Retrofit 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsdaRetrofit
