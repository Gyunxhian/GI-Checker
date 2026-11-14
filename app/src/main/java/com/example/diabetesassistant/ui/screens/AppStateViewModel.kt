package com.example.diabetesassistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.database.sqlite.SQLiteConstraintException
import com.example.diabetesassistant.BuildConfig
import com.example.diabetesassistant.data.local.entity.User
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.repository.UserRepository
import com.example.diabetesassistant.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
private const val MIN_PASSWORD_LENGTH = 6
private const val DUPLICATE_ACCOUNT_ERROR = "账号已存在"
private const val INVALID_CREDENTIALS_ERROR = "账号或密码错误"
private const val SESSION_PERSISTENCE_ERROR = "会话保存失败"
private const val LOCAL_DATA_ERROR = "本地数据异常"

/**
 * 登录表单状态。
 */
data class AuthFormState(
    val isRegisterMode: Boolean = false,
    val nickname: String = "",
    val account: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

data class OnboardingSubmissionState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * 全局会话与引导流程状态管理。
 */
@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : ViewModel() {

    val signedIn: StateFlow<Boolean> = userPreferencesDataSource.signedInFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val onboardingCompleted: StateFlow<Boolean> = userPreferencesDataSource.onboardingCompletedFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val signedInUserId: StateFlow<Long?> = userPreferencesDataSource.signedInUserIdFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    val appThemeMode: StateFlow<AppThemeMode> = userPreferencesDataSource.appThemeModeFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AppThemeMode.FOLLOW_SYSTEM,
    )

    private val _sessionUserId = MutableStateFlow<Long?>(null)

    private val _guardianDays = MutableStateFlow(1)
    val guardianDays: StateFlow<Int> = _guardianDays.asStateFlow()

    private val _authFormState = MutableStateFlow(AuthFormState())
    val authFormState: StateFlow<AuthFormState> = _authFormState.asStateFlow()

    private val _onboardingSubmissionState = MutableStateFlow(OnboardingSubmissionState())
    val onboardingSubmissionState: StateFlow<OnboardingSubmissionState> = _onboardingSubmissionState.asStateFlow()

    init {
        viewModelScope.launch {
            signedInUserId.collect { userId ->
                val id = userId ?: 1L
                val createdAt = userRepository.getUserById(id)?.createdAt ?: System.currentTimeMillis()
                val days = ((System.currentTimeMillis() - createdAt) / DAY_MILLIS) + 1
                _guardianDays.value = days.coerceAtLeast(1L).toInt()
            }
        }
    }

    /**
     * 切换登录/注册模式。
     */
    fun setRegisterMode(enabled: Boolean) {
        _authFormState.value = _authFormState.value.copy(
            isRegisterMode = enabled,
            errorMessage = null,
        )
    }

    /**
     * 更新昵称输入。
     */
    fun updateNickname(value: String) {
        _authFormState.value = _authFormState.value.copy(
            nickname = value,
            errorMessage = null,
        )
    }

    /**
     * 更新账号输入。
     */
    fun updateAccount(value: String) {
        _authFormState.value = _authFormState.value.copy(
            account = value,
            errorMessage = null,
        )
    }

    /**
     * 更新密码输入。
     */
    fun updatePassword(value: String) {
        _authFormState.value = _authFormState.value.copy(
            password = value,
            errorMessage = null,
        )
    }

    /**
     * 提交登录或注册。
     */
    fun submitAuth() {
        val snapshot = _authFormState.value
        if (snapshot.isSubmitting) return

        val rawAccount = snapshot.account.trim()
        val password = snapshot.password.trim()
        val nickname = snapshot.nickname.trim()
        val isEmailAccount = rawAccount.contains("@")
        val account = if (isEmailAccount) rawAccount.lowercase() else rawAccount

        if (account.isBlank() || password.isBlank()) {
            _authFormState.value = snapshot.copy(errorMessage = "账号和密码不能为空")
            return
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            _authFormState.value = snapshot.copy(errorMessage = "密码至少需要 6 位")
            return
        }

        if (snapshot.isRegisterMode && nickname.isBlank()) {
            _authFormState.value = snapshot.copy(errorMessage = "注册时昵称为必填")
            return
        }

        _authFormState.value = snapshot.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                if (snapshot.isRegisterMode) {
                    if (userRepository.isAccountExists(account)) {
                        error(DUPLICATE_ACCOUNT_ERROR)
                    }
                    val registeredUserId = userRepository.register(
                        User(
                            nickname = nickname,
                            phone = if (isEmailAccount) null else account,
                            email = if (isEmailAccount) account else null,
                            password = password,
                        )
                    )
                    persistSession(
                        userId = registeredUserId,
                        onboardingCompleted = false,
                    )
                } else {
                    val user = userRepository.login(account, password)
                        ?: error(INVALID_CREDENTIALS_ERROR)
                    val hasHealthProfile = userRepository.getHealthProfile(user.id) != null
                    persistSession(
                        userId = user.id,
                        onboardingCompleted = hasHealthProfile,
                    )
                }
            }.onSuccess {
                _authFormState.value = _authFormState.value.copy(
                    password = "",
                    isSubmitting = false,
                    errorMessage = null,
                )
            }.onFailure { throwable ->
                _sessionUserId.value = null
                val resolvedMessage = resolveAuthErrorMessage(
                    throwable = throwable,
                    isRegisterMode = snapshot.isRegisterMode,
                )
                _authFormState.value = _authFormState.value.copy(
                    isSubmitting = false,
                    errorMessage = resolvedMessage,
                )
            }
        }
    }

    private suspend fun persistSession(
        userId: Long,
        onboardingCompleted: Boolean,
    ) {
        runCatching {
            userPreferencesDataSource.setSessionState(
                signedIn = true,
                userId = userId,
                onboardingCompleted = onboardingCompleted,
            )
        }.onSuccess {
            _sessionUserId.value = userId
        }.onFailure {
            _sessionUserId.value = null
            error(SESSION_PERSISTENCE_ERROR)
        }
    }

    private fun resolveAuthErrorMessage(
        throwable: Throwable,
        isRegisterMode: Boolean,
    ): String {
        val throwableChain = generateSequence(throwable) { it.cause }.toList()
        val messages = throwableChain.mapNotNull { it.message?.lowercase() }
        val hasLocalDataError = throwableChain.any { it is java.io.IOException } || messages.any {
            "database" in it || "sqlite" in it || "no such table" in it || "locked" in it || "disk i/o" in it
        }

        val isDuplicateAccount = isRegisterMode && (
            messages.any { DUPLICATE_ACCOUNT_ERROR.lowercase() in it } ||
            throwableChain.any { it is SQLiteConstraintException } ||
            messages.any { "unique constraint" in it || "constraint failed" in it || "duplicate" in it }
        )

        return when {
            messages.any { SESSION_PERSISTENCE_ERROR.lowercase() in it } -> "会话保存失败，请重试"
            isDuplicateAccount -> "账号已存在，请直接登录"
            messages.any { INVALID_CREDENTIALS_ERROR.lowercase() in it } -> "账号不存在或密码错误"
            messages.any { LOCAL_DATA_ERROR.lowercase() in it } || hasLocalDataError -> {
                if (BuildConfig.DEBUG) {
                    val debugReason = throwableChain.firstNotNullOfOrNull { it.message?.trim()?.takeIf(String::isNotEmpty) }
                        ?: throwable::class.simpleName
                        ?: "Unknown"
                    "本地数据异常，请稍后重试（debug: $debugReason）"
                } else {
                    "本地数据异常，请稍后重试"
                }
            }
            isRegisterMode -> "注册失败，请检查账号信息"
            else -> "登录失败，请检查账号或密码"
        }
    }

    fun ensureSingleUserSignedIn() {
        viewModelScope.launch {
            if (userRepository.getUserById(1L) == null) {
                userRepository.register(
                    User(
                        id = 1L,
                        nickname = "我",
                        phone = null,
                        email = "local@user.local",
                        password = "local_password",
                    )
                )
            }
            _sessionUserId.value = 1L
            userPreferencesDataSource.setSessionState(
                signedIn = true,
                userId = 1L,
                onboardingCompleted = userPreferencesDataSource.onboardingCompletedFlow.first(),
            )
        }
    }

    /**
     * 完成引导并写入必填健康档案。
     */
    fun completeOnboarding(
        diabetesType: String,
        insulinType: String,
        insulinMin: Float?,
        insulinMax: Float?,
        weightKg: Float?,
        heightCm: Float?,
    ): String? {
        val userId = _sessionUserId.value ?: signedInUserId.value
        if (userId == null) {
            return "未找到登录用户"
        }

        val diabetes = diabetesType.trim()
        val insulin = insulinType.trim()
        if (diabetes.isBlank() || insulin.isBlank()) {
            return "糖尿病类型和胰岛素类型为必填"
        }

        if (_onboardingSubmissionState.value.isSubmitting) {
            return null
        }

        _onboardingSubmissionState.value = OnboardingSubmissionState(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                if (userRepository.getUserById(userId) == null) {
                    userRepository.register(
                        User(
                            id = userId,
                            nickname = "我",
                            phone = null,
                            email = "local@user.local",
                            password = "local_password",
                        )
                    )
                }
                val existing = userRepository.getHealthProfile(userId)
                userRepository.saveHealthProfile(
                    com.example.diabetesassistant.data.local.entity.HealthProfile(
                        id = existing?.id ?: 0L,
                        userId = userId,
                        diabetesType = diabetes,
                        insulinType = insulin,
                        insulinMin = insulinMin,
                        insulinMax = insulinMax,
                        weightKg = weightKg,
                        heightCm = heightCm,
                    )
                )
                _sessionUserId.value = userId
                userPreferencesDataSource.setSessionState(
                    signedIn = true,
                    userId = userId,
                    onboardingCompleted = true,
                )
            }.onSuccess {
                _onboardingSubmissionState.value = OnboardingSubmissionState()
            }.onFailure { e ->
                android.util.Log.e("AppStateViewModel", "completeOnboarding failed", e)
                _onboardingSubmissionState.value = OnboardingSubmissionState(
                    isSubmitting = false,
                    errorMessage = "健康档案保存失败，请重试",
                )
            }
        }
        return null
    }

    /**
     * 退出登录。
     */
    fun signOut() {
        viewModelScope.launch {
            _sessionUserId.value = null
            _guardianDays.value = 1
            userPreferencesDataSource.setSessionState(
                signedIn = false,
                userId = null,
                onboardingCompleted = false,
            )
        }
    }
}
