package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 认证与引导模式。
 */
enum class AuthOnboardingMode {
    AuthOnly,
    OnboardingOnly,
}

private enum class OnboardingPage {
    Welcome,
    Highlights,
    Disclaimer,
    HealthProfile,
}

private val DIABETES_TYPE_OPTIONS = listOf("1型糖尿病", "2型糖尿病", "妊娠期糖尿病", "其他")
private val INSULIN_TYPE_OPTIONS = listOf("速效", "短效", "中效", "长效")

/**
 * 认证与引导入口。
 */
@Composable
fun AuthOnboardingScreen(
    mode: AuthOnboardingMode,
    authState: AuthFormState,
    onboardingSubmissionState: OnboardingSubmissionState,
    onAuthModeChange: (Boolean) -> Unit,
    onNicknameChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAuthSubmit: () -> Unit,
    onCompleteOnboarding: (
        diabetesType: String,
        insulinType: String,
        insulinMin: Float?,
        insulinMax: Float?,
        weightKg: Float?,
        heightCm: Float?,
    ) -> String?,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        AuthOnboardingMode.AuthOnly -> {
            AuthEntryContent(
                authState = authState,
                onAuthModeChange = onAuthModeChange,
                onNicknameChange = onNicknameChange,
                onAccountChange = onAccountChange,
                onPasswordChange = onPasswordChange,
                onAuthSubmit = onAuthSubmit,
                modifier = modifier,
            )
        }

        AuthOnboardingMode.OnboardingOnly -> {
            OnboardingContent(
                onboardingSubmissionState = onboardingSubmissionState,
                onCompleteOnboarding = onCompleteOnboarding,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AuthEntryContent(
    authState: AuthFormState,
    onAuthModeChange: (Boolean) -> Unit,
    onNicknameChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAuthSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = if (authState.isRegisterMode) "注册" else "登录", style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onAuthModeChange(false) }, modifier = Modifier.weight(1f)) {
                    Text("登录")
                }
                OutlinedButton(onClick = { onAuthModeChange(true) }, modifier = Modifier.weight(1f)) {
                    Text("注册")
                }
            }

            if (authState.isRegisterMode) {
                OutlinedTextField(
                    value = authState.nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = authState.account,
                onValueChange = onAccountChange,
                label = { Text("手机号或邮箱") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = authState.password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            authState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onAuthSubmit,
                enabled = !authState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (authState.isRegisterMode) "注册并继续" else "登录并继续")
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    onboardingSubmissionState: OnboardingSubmissionState,
    onCompleteOnboarding: (
        diabetesType: String,
        insulinType: String,
        insulinMin: Float?,
        insulinMax: Float?,
        weightKg: Float?,
        heightCm: Float?,
    ) -> String?,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableStateOf(OnboardingPage.Welcome) }
    var disclaimerAccepted by remember { mutableStateOf(false) }

    var diabetesType by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf("") }
    var insulinMinText by remember { mutableStateOf("") }
    var insulinMaxText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (page) {
                    OnboardingPage.Welcome -> "欢迎"
                    OnboardingPage.Highlights -> "功能亮点"
                    OnboardingPage.Disclaimer -> "免责声明"
                    OnboardingPage.HealthProfile -> "必填健康档案"
                },
                style = MaterialTheme.typography.headlineMedium,
            )

            when (page) {
                OnboardingPage.Welcome -> {
                    Text("拍照识别菜品，预测血糖波动，提供温馨提醒。")
                    OnboardingNav(
                        canBack = false,
                        onBack = {},
                        onNext = { page = OnboardingPage.Highlights },
                    )
                }

                OnboardingPage.Highlights -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• 拍照识别最多 5 道菜")
                            Text("• 滑块联动实时重算")
                            Text("• 曲线预测 + 胰岛素色带 + 重点时段")
                        }
                    }
                    OnboardingNav(
                        canBack = true,
                        onBack = { page = OnboardingPage.Welcome },
                        onNext = { page = OnboardingPage.Disclaimer },
                    )
                }

                OnboardingPage.Disclaimer -> {
                    Text("本应用仅提供健康管理参考，不构成医疗建议，请在医生指导下治疗与用药。")
                    OutlinedButton(onClick = { disclaimerAccepted = !disclaimerAccepted }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (disclaimerAccepted) "已同意免责声明" else "点击同意免责声明")
                    }
                    OnboardingNav(
                        canBack = true,
                        onBack = { page = OnboardingPage.Highlights },
                        onNext = { page = OnboardingPage.HealthProfile },
                        nextEnabled = disclaimerAccepted,
                    )
                }

                OnboardingPage.HealthProfile -> {
                    OptionSelectorField(
                        label = "糖尿病类型（必填）",
                        options = DIABETES_TYPE_OPTIONS,
                        selected = diabetesType,
                        onSelect = {
                            diabetesType = it
                            errorMessage = null
                        },
                    )
                    OptionSelectorField(
                        label = "胰岛素类型（必填）",
                        options = INSULIN_TYPE_OPTIONS,
                        selected = insulinType,
                        onSelect = {
                            insulinType = it
                            errorMessage = null
                        },
                    )
                    OutlinedTextField(
                        value = insulinMinText,
                        onValueChange = { insulinMinText = it },
                        label = { Text("最小剂量 IU（选填）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = insulinMaxText,
                        onValueChange = { insulinMaxText = it },
                        label = { Text("最大剂量 IU（选填）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("体重 kg（选填）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("身高 cm（选填）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )

                    errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    onboardingSubmissionState.errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    OnboardingNav(
                        canBack = true,
                        onBack = { page = OnboardingPage.Disclaimer },
                        onNext = {
                            errorMessage = onCompleteOnboarding(
                                diabetesType.trim(),
                                insulinType.trim(),
                                insulinMinText.toFloatOrNull(),
                                insulinMaxText.toFloatOrNull(),
                                weightText.toFloatOrNull(),
                                heightText.toFloatOrNull(),
                            )
                        },
                        nextEnabled = !onboardingSubmissionState.isSubmitting,
                        nextLabel = if (onboardingSubmissionState.isSubmitting) "保存中..." else "完成引导",
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingNav(
    canBack: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    nextLabel: String = "下一步",
) {
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onBack, enabled = canBack, modifier = Modifier.weight(1f)) {
            Text("上一步")
        }
        Button(onClick = onNext, enabled = nextEnabled, modifier = Modifier.weight(1f)) {
            Text(nextLabel)
        }
    }
}

@Composable
private fun OptionSelectorField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    options.chunked(2).forEach { rowOptions ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rowOptions.forEach { option ->
                val isSelected = selected == option
                if (isSelected) {
                    Button(
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(option)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(option)
                    }
                }
            }
            if (rowOptions.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthOnboardingAuthPreview() {
    AuthOnboardingScreen(
        mode = AuthOnboardingMode.AuthOnly,
        authState = AuthFormState(),
        onboardingSubmissionState = OnboardingSubmissionState(),
        onAuthModeChange = {},
        onNicknameChange = {},
        onAccountChange = {},
        onPasswordChange = {},
        onAuthSubmit = {},
        onCompleteOnboarding = { _, _, _, _, _, _ -> null },
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthOnboardingFlowPreview() {
    AuthOnboardingScreen(
        mode = AuthOnboardingMode.OnboardingOnly,
        authState = AuthFormState(),
        onboardingSubmissionState = OnboardingSubmissionState(),
        onAuthModeChange = {},
        onNicknameChange = {},
        onAccountChange = {},
        onPasswordChange = {},
        onAuthSubmit = {},
        onCompleteOnboarding = { _, _, _, _, _, _ -> null },
    )
}
