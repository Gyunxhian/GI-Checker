package com.example.diabetesassistant.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.diabetesassistant.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore 偏好读取与写入。
 */
@Singleton
open class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * 登录状态流。
     */
    open val signedInFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SIGNED_IN] ?: false
    }

    /**
     * 当前登录用户 ID 流。
     */
    open val signedInUserIdFlow: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SIGNED_IN_USER_ID]
    }

    /**
     * Onboarding 完成状态流。
     */
    open val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    /**
     * 外观模式流。
     */
    val appThemeModeFlow: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        val value = prefs[PreferencesKeys.THEME_MODE] ?: AppThemeMode.FOLLOW_SYSTEM.name
        AppThemeMode.entries.firstOrNull { it.name == value } ?: AppThemeMode.FOLLOW_SYSTEM
    }

    /**
     * 用户头像 URI 流。
     */
    val userAvatarUriFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_AVATAR_URI] ?: ""
    }

    /**
     * 用户昵称流。
     */
    val userNicknameFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_NICKNAME] ?: "健康生活家"
    }

    /**
     * 每日目标碳水限额流 (默认 180g 稳健控糖)。
     */
    val dailyCarbTargetFlow: Flow<Float> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DAILY_CARB_TARGET] ?: 180f
    }

    /**
     * 原子更新会话状态。
     */
    open suspend fun setSessionState(
        signedIn: Boolean,
        userId: Long?,
        onboardingCompleted: Boolean,
    ) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SIGNED_IN] = signedIn
            if (signedIn && userId != null) {
                prefs[PreferencesKeys.SIGNED_IN_USER_ID] = userId
                prefs[PreferencesKeys.ONBOARDING_COMPLETED] = onboardingCompleted
            } else {
                prefs.remove(PreferencesKeys.SIGNED_IN_USER_ID)
                prefs[PreferencesKeys.ONBOARDING_COMPLETED] = false
            }
        }
    }

    /**
     * 更新登录状态。
     */
    suspend fun setSignedIn(signedIn: Boolean, userId: Long? = null) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SIGNED_IN] = signedIn
            if (signedIn && userId != null) {
                prefs[PreferencesKeys.SIGNED_IN_USER_ID] = userId
            } else {
                prefs.remove(PreferencesKeys.SIGNED_IN_USER_ID)
            }
        }
    }

    /**
     * 更新 Onboarding 完成状态。
     */
    open suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    /**
     * 更新外观模式。
     */
    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_MODE] = mode.name }
    }

    /**
     * 更新用户头像 URI。
     */
    suspend fun setUserAvatarUri(uri: String) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.USER_AVATAR_URI] = uri }
    }

    /**
     * 更新用户昵称。
     */
    suspend fun setUserNickname(nickname: String) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.USER_NICKNAME] = nickname }
    }

    /**
     * 更新每日目标碳水限额。
     */
    suspend fun setDailyCarbTarget(target: Float) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.DAILY_CARB_TARGET] = target }
    }
}

/**
 * 偏好键定义。
 */
object PreferencesKeys {
    /**
     * 登录状态键。
     */
    val SIGNED_IN = booleanPreferencesKey("signed_in")

    /**
     * 登录用户 ID 键。
     */
    val SIGNED_IN_USER_ID = longPreferencesKey("signed_in_user_id")

    /**
     * Onboarding 完成键。
     */
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    /**
     * 外观模式键。
     */
    val THEME_MODE = stringPreferencesKey("theme_mode")

    /**
     * 用户头像 URI 键。
     */
    val USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")

    /**
     * 用户昵称键。
     */
    val USER_NICKNAME = stringPreferencesKey("user_nickname")

    /**
     * 每日目标碳水限额键。
     */
    val DAILY_CARB_TARGET = floatPreferencesKey("daily_carb_target")
}
