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
import com.example.diabetesassistant.data.local.dao.AiChatMessageDao
import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.local.dao.FoodRecordDao
import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.dao.UserDao
import com.example.diabetesassistant.data.remote.DeepSeekApi
import com.example.diabetesassistant.data.remote.UsdaApi
import com.example.diabetesassistant.data.remote.interceptor.RetryInterceptor
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.data.repository.AiRepositoryImpl
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.data.repository.FoodRecordRepositoryImpl
import com.example.diabetesassistant.data.repository.NutritionRepository
import com.example.diabetesassistant.data.repository.NutritionRepositoryImpl
import com.example.diabetesassistant.data.repository.UserRepository
import com.example.diabetesassistant.data.repository.UserRepositoryImpl
import com.example.diabetesassistant.domain.algorithm.BloodSugarPredictor
import com.example.diabetesassistant.domain.algorithm.CarbAndGiCalculator
import com.example.diabetesassistant.domain.algorithm.FocusPeriodAnalyzer
import com.example.diabetesassistant.domain.algorithm.InsulinBandCalculator
import com.example.diabetesassistant.domain.usecase.AnalyzeFoodUseCase
import com.example.diabetesassistant.domain.usecase.CalculateBloodSugarUseCase
import com.example.diabetesassistant.domain.usecase.CalculateInsulinBandUseCase
import com.example.diabetesassistant.domain.usecase.GenerateAlertUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
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
        ).addMigrations(
            DiabetesDatabase.MIGRATION_1_2,
            DiabetesDatabase.MIGRATION_2_3,
            DiabetesDatabase.MIGRATION_3_4,
            DiabetesDatabase.MIGRATION_4_5,
            DiabetesDatabase.MIGRATION_5_6,
        ).fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addCallback(
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
     * 提供 AI 问答记录 DAO。
     */
    @Provides
    fun provideAiChatMessageDao(database: DiabetesDatabase): AiChatMessageDao = database.aiChatMessageDao()

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
     * 提供 USDA OkHttpClient。
     */
    @Provides
    @Singleton
    @UsdaClient
    fun provideUsdaOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(RetryInterceptor())
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

    @Provides
    @Singleton
    @DeepSeekClient
    fun provideDeepSeekOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(RetryInterceptor())
            .build()
    }

    @Provides
    @Singleton
    @DeepSeekRetrofit
    fun provideDeepSeekRetrofit(@DeepSeekClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDeepSeekApi(@DeepSeekRetrofit retrofit: Retrofit): DeepSeekApi {
        return retrofit.create(DeepSeekApi::class.java)
    }

    /**
     * 提供 AI 仓库。
     */
    @Provides
    @Singleton
    fun provideAiRepository(
        deepSeekApi: DeepSeekApi,
        apiKeyProvider: com.example.diabetesassistant.data.remote.ApiKeyProvider,
        @ApplicationContext context: Context,
    ): AiRepository = AiRepositoryImpl(deepSeekApi, apiKeyProvider, context)

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

    /**
     * 提供碳水与 GI 计算器。
     */
    @Provides
    @Singleton
    fun provideCarbAndGiCalculator(): CarbAndGiCalculator = CarbAndGiCalculator()

    /**
     * 提供血糖预测器。
     */
    @Provides
    @Singleton
    fun provideBloodSugarPredictor(carbAndGiCalculator: CarbAndGiCalculator): BloodSugarPredictor {
        return BloodSugarPredictor(carbAndGiCalculator)
    }

    /**
     * 提供胰岛素色带计算器。
     */
    @Provides
    @Singleton
    fun provideInsulinBandCalculator(): InsulinBandCalculator = InsulinBandCalculator()

    /**
     * 提供重点时段分析器。
     */
    @Provides
    @Singleton
    fun provideFocusPeriodAnalyzer(): FocusPeriodAnalyzer = FocusPeriodAnalyzer()

    /**
     * 提供完整餐食分析用例。
     */
    @Provides
    @Singleton
    fun provideAnalyzeFoodUseCase(
        nutritionRepository: NutritionRepository,
        aiRepository: AiRepository,
        cookingMethodDao: CookingMethodDao,
        carbAndGiCalculator: CarbAndGiCalculator,
        bloodSugarPredictor: BloodSugarPredictor,
        insulinBandCalculator: InsulinBandCalculator,
        focusPeriodAnalyzer: FocusPeriodAnalyzer,
    ): AnalyzeFoodUseCase {
        return AnalyzeFoodUseCase(
            nutritionRepository = nutritionRepository,
            aiRepository = aiRepository,
            cookingMethodDao = cookingMethodDao,
            carbAndGiCalculator = carbAndGiCalculator,
            bloodSugarPredictor = bloodSugarPredictor,
            insulinBandCalculator = insulinBandCalculator,
            focusPeriodAnalyzer = focusPeriodAnalyzer,
        )
    }

    /**
     * 提供血糖计算用例。
     */
    @Provides
    @Singleton
    fun provideCalculateBloodSugarUseCase(
        carbAndGiCalculator: CarbAndGiCalculator,
        bloodSugarPredictor: BloodSugarPredictor,
    ): CalculateBloodSugarUseCase {
        return CalculateBloodSugarUseCase(carbAndGiCalculator, bloodSugarPredictor)
    }

    /**
     * 提供胰岛素色带计算用例。
     */
    @Provides
    @Singleton
    fun provideCalculateInsulinBandUseCase(
        insulinBandCalculator: InsulinBandCalculator,
    ): CalculateInsulinBandUseCase {
        return CalculateInsulinBandUseCase(insulinBandCalculator)
    }

    /**
     * 提供提醒生成用例。
     */
    @Provides
    @Singleton
    fun provideGenerateAlertUseCase(
        focusPeriodAnalyzer: FocusPeriodAnalyzer,
        aiRepository: AiRepository,
    ): GenerateAlertUseCase {
        return GenerateAlertUseCase(focusPeriodAnalyzer, aiRepository)
    }
}

/**
 * USDA OkHttp 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsdaClient

/**
 * USDA Retrofit 依赖限定符。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UsdaRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeepSeekClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeepSeekRetrofit


