package com.example.diabetesassistant.ui.screens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.data.local.entity.User
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.repository.UserRepository
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun submitAuth_registersNumericAccountAsPhone() = runTest {
        val repository = FakeUserRepository(registerResult = 42L)
        val preferences = UserPreferencesDataSource(FakePreferencesDataStore())
        val viewModel = AppStateViewModel(repository, preferences)

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        val registeredUser = repository.registeredUsers.single()
        assertEquals("Tester", registeredUser.nickname)
        assertEquals("123456", registeredUser.phone)
        assertEquals(null, registeredUser.email)
        assertEquals("123456", registeredUser.password)
        assertTrue(preferences.signedInFlow.first())
        assertEquals(42L, preferences.signedInUserIdFlow.first())
        assertFalse(preferences.onboardingCompletedFlow.first())
        assertNull(viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
        assertEquals("", viewModel.authFormState.value.password)
    }

    @Test
    fun submitAuth_registerSuccess_whenSetSessionStateThrows_showsPersistenceFailure() = runTest {
        val repository = FakeUserRepository(registerResult = 42L)
        val preferences = ThrowingUserPreferencesDataSource()
        val viewModel = AppStateViewModel(repository, preferences)

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals(1, repository.registeredUsers.size)
        assertEquals(1, preferences.setSessionStateCalls)
        assertEquals("会话保存失败，请重试", viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
        assertEquals("123456", viewModel.authFormState.value.password)
    }

    @Test
    fun submitAuth_loginSuccess_whenSetSessionStateThrows_showsPersistenceFailure() = runTest {
        val repository = FakeUserRepository(
            loginResult = User(
                id = 7L,
                nickname = "Tester",
                phone = "123456",
                email = null,
                password = "123456",
            )
        )
        val preferences = ThrowingUserPreferencesDataSource()
        val viewModel = AppStateViewModel(repository, preferences)

        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals(1, repository.loginCalls)
        assertEquals(1, preferences.setSessionStateCalls)
        assertEquals("会话保存失败，请重试", viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
        assertEquals("123456", viewModel.authFormState.value.password)
    }

    @Test
    fun completeOnboarding_requiresPersistedSessionAfterAuthFailure() = runTest {
        val repository = FakeUserRepository(registerResult = 42L)
        val preferences = ThrowingUserPreferencesDataSource()
        val viewModel = AppStateViewModel(repository, preferences)

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        val result = viewModel.completeOnboarding(
            diabetesType = "1型",
            insulinType = "速效",
            insulinMin = 1f,
            insulinMax = 2f,
            weightKg = 60f,
            heightCm = 170f,
        )

        assertEquals("未找到登录用户", result)
    }

    @Test
    fun completeOnboarding_returnsFailureWhenSaveHealthProfileThrows() = runTest {
        val repository = FakeUserRepository(registerResult = 42L, saveHealthProfileFailure = IOException("save failed"))
        val preferences = UserPreferencesDataSource(FakePreferencesDataStore())
        val viewModel = AppStateViewModel(repository, preferences)

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        val result = viewModel.completeOnboarding(
            diabetesType = "1型",
            insulinType = "速效",
            insulinMin = 1f,
            insulinMax = 2f,
            weightKg = 60f,
            heightCm = 170f,
        )

        assertNull(result)

        advanceUntilIdle()

        assertEquals("健康档案保存失败，请重试", viewModel.onboardingSubmissionState.value.errorMessage)
        assertFalse(viewModel.onboardingSubmissionState.value.isSubmitting)
        assertTrue(repository.savedProfiles.isEmpty())
        assertFalse(preferences.onboardingCompletedFlow.first())
    }

    @Test
    fun submitAuth_register_whenRepositoryThrowsDatabaseError_showsLocalDataFailure() = runTest {
        val repository = FakeUserRepository(registerFailure = RuntimeException("database is locked"))
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertTrue(viewModel.authFormState.value.errorMessage?.startsWith("本地数据异常，请稍后重试") == true)
        assertTrue(viewModel.authFormState.value.errorMessage?.contains("database is locked") == true)
        assertFalse(viewModel.authFormState.value.isSubmitting)
    }

    @Test
    fun submitAuth_login_whenRepositoryThrowsDatabaseError_showsLocalDataFailure() = runTest {
        val repository = FakeUserRepository(loginFailure = RuntimeException("no such table: users"))
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertTrue(viewModel.authFormState.value.errorMessage?.startsWith("本地数据异常，请稍后重试") == true)
        assertTrue(viewModel.authFormState.value.errorMessage?.contains("no such table: users") == true)
        assertFalse(viewModel.authFormState.value.isSubmitting)
    }

    @Test
    fun submitAuth_register_whenConstraintErrorWrapped_showsDuplicateAccountMessage() = runTest {
        val repository = FakeUserRepository(registerFailure = RuntimeException("UNIQUE constraint failed: users.phone"))
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals("账号已存在，请直接登录", viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
    }

    @Test
    fun submitAuth_showsDuplicateMessageWhenRegisterAccountExists() = runTest {
        val repository = FakeUserRepository(accountExists = true)
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.setRegisterMode(true)
        viewModel.updateNickname("Tester")
        viewModel.updateAccount("123456")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals("账号已存在，请直接登录", viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
    }

    @Test
    fun submitAuth_showsInvalidCredentialsMessageWhenLoginFails() = runTest {
        val repository = FakeUserRepository(loginResult = null)
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.updateAccount("missing")
        viewModel.updatePassword("123456")
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals("账号不存在或密码错误", viewModel.authFormState.value.errorMessage)
        assertFalse(viewModel.authFormState.value.isSubmitting)
    }

    @Test
    fun submitAuth_rejectsShortPasswordBeforeRepositoryCall() = runTest {
        val repository = FakeUserRepository()
        val viewModel = AppStateViewModel(repository, UserPreferencesDataSource(FakePreferencesDataStore()))

        viewModel.updateAccount("123456")
        viewModel.updatePassword("12345")
        viewModel.submitAuth()

        assertEquals("密码至少需要 6 位", viewModel.authFormState.value.errorMessage)
        assertTrue(repository.registeredUsers.isEmpty())
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun completeOnboarding_requiresSignedInUser() {
        val viewModel = AppStateViewModel(FakeUserRepository(), UserPreferencesDataSource(FakePreferencesDataStore()))

        val result = viewModel.completeOnboarding(
            diabetesType = "1型",
            insulinType = "速效",
            insulinMin = 1f,
            insulinMax = 2f,
            weightKg = 60f,
            heightCm = 170f,
        )

        assertEquals("未找到登录用户", result)
    }
}

private class FakeUserRepository(
    private val registerResult: Long = 1L,
    private val loginResult: User? = null,
    private val accountExists: Boolean = false,
    private val saveHealthProfileFailure: Throwable? = null,
    private val registerFailure: Throwable? = null,
    private val loginFailure: Throwable? = null,
) : UserRepository {
    val registeredUsers = mutableListOf<User>()
    var loginCalls = 0
    var savedProfiles = mutableListOf<HealthProfile>()

    override suspend fun register(user: User): Long {
        registerFailure?.let { throw it }
        registeredUsers += user
        return registerResult
    }

    override suspend fun login(account: String, password: String): User? {
        loginFailure?.let { throw it }
        loginCalls += 1
        return loginResult
    }

    override suspend fun isAccountExists(account: String): Boolean = accountExists

    override suspend fun getUserById(userId: Long): User? = null

    override fun observeUsers(): Flow<List<User>> = flowOf(emptyList())

    override suspend fun saveHealthProfile(profile: HealthProfile): Long {
        saveHealthProfileFailure?.let { throw it }
        savedProfiles += profile
        return 1L
    }

    override suspend fun getHealthProfile(userId: Long): HealthProfile? = null

    override fun observeHealthProfile(userId: Long): Flow<HealthProfile?> = flowOf(null)
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

private class ThrowingUserPreferencesDataSource(
    private val failure: Throwable = IOException("persist failed"),
) : UserPreferencesDataSource(FakePreferencesDataStore()) {
    var setSessionStateCalls = 0

    override suspend fun setSessionState(
        signedIn: Boolean,
        userId: Long?,
        onboardingCompleted: Boolean,
    ) {
        setSessionStateCalls += 1
        throw failure
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
