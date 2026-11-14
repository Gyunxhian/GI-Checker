package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(60.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (authState.isRegisterMode) Icons.Default.Person else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }

                Text(
                    text = if (authState.isRegisterMode) "注册糖控账号" else "登录糖控助手",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // 切换标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        onClick = { onAuthModeChange(false) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!authState.isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "登录",
                            color = if (!authState.isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (!authState.isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp),
                            fontSize = 14.sp,
                        )
                    }
                    Surface(
                        onClick = { onAuthModeChange(true) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (authState.isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "注册",
                            color = if (authState.isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (authState.isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp),
                            fontSize = 14.sp,
                        )
                    }
                }

                if (authState.isRegisterMode) {
                    OutlinedTextField(
                        value = authState.nickname,
                        onValueChange = onNicknameChange,
                        label = { Text("用户昵称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                OutlinedTextField(
                    value = authState.account,
                    onValueChange = onAccountChange,
                    label = { Text("手机号或邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )

                OutlinedTextField(
                    value = authState.password,
                    onValueChange = onPasswordChange,
                    label = { Text("账户密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )

                authState.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = onAuthSubmit,
                    enabled = !authState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = if (authState.isRegisterMode) "注册并进入系统" else "登录并进入系统",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 顶部步骤指示器
                StepIndicator(currentPage = page)

                Spacer(modifier = Modifier.height(20.dp))

                when (page) {
                    OnboardingPage.Welcome -> {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(68.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "糖控助手 — 私有化健康守护",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "结合多餐盘识别与代谢动力学，为您量身预测餐后血糖波动，提供科学温馨的生活关怀。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3 大核心能力概述卡片
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OnboardFeatureRow(
                                    icon = Icons.Filled.CameraAlt,
                                    title = "多餐盘视觉识别",
                                    desc = "拍照或选图识别多道菜品，精准拆解食材与碳水",
                                )
                                OnboardFeatureRow(
                                    icon = Icons.Filled.ShowChart,
                                    title = "动力学曲线模拟",
                                    desc = "真实 Gamma 模型推演餐后 180 分钟波动轨迹",
                                )
                                OnboardFeatureRow(
                                    icon = Icons.Filled.Shield,
                                    title = "胰岛素安全区",
                                    desc = "评估药物作用重合时段，助力规避低血糖风险",
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { page = OnboardingPage.Highlights },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("开启健康之旅", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    OnboardingPage.Highlights -> {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "核心功能特色",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "专为日常控糖定制的极简体验",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HighlightCard(
                                title = "多图批次拍盘与食材拆解",
                                desc = "一餐多道菜逐一核验国家营养库，告别手算碳水烦恼。",
                            )
                            HighlightCard(
                                title = "荤素比与摄入量动态微调",
                                desc = "餐后滑动条即时联动，毫秒级重算吸收与达峰时间。",
                            )
                            HighlightCard(
                                title = "个性化温馨关怀与时段提醒",
                                desc = "智能生成温和不焦虑的餐后注意事项，全面贴合控糖节奏。",
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OnboardingNav(
                            canBack = true,
                            onBack = { page = OnboardingPage.Welcome },
                            onNext = { page = OnboardingPage.Disclaimer },
                        )
                    }

                    OnboardingPage.Disclaimer -> {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "医疗与合规免责声明",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "1. 本应用测算的血糖预测曲线、胰岛素色带及餐后建议均为算法模型推演，仅供个人生活作息与饮食管理参考。",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp,
                                )
                                Text(
                                    text = "2. 算法结果不能作为临床诊断依据，亦不能替代医生处方及专业用药调整方案。",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp,
                                )
                                Text(
                                    text = "3. 调整药物剂量或遇到低血糖严重不适时，请务必遵照执业医师医嘱并及时就医。",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 同意勾选交互
                        Surface(
                            onClick = { disclaimerAccepted = !disclaimerAccepted },
                            shape = RoundedCornerShape(12.dp),
                            color = if (disclaimerAccepted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (disclaimerAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = if (disclaimerAccepted) Icons.Default.CheckCircle else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (disclaimerAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = if (disclaimerAccepted) "我已完整阅读并同意免责声明" else "点击确认同意上述医疗免责声明",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (disclaimerAccepted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (disclaimerAccepted) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OnboardingNav(
                            canBack = true,
                            onBack = { page = OnboardingPage.Highlights },
                            onNext = { page = OnboardingPage.HealthProfile },
                            nextEnabled = disclaimerAccepted,
                        )
                    }

                    OnboardingPage.HealthProfile -> {
                        Text(
                            text = "完善个人健康档案",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "设置基础控糖参数，使代谢动力学预测更贴合您的身体体质",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            // 糖尿病类型选择：采用安全的双列自适应等宽布局，彻底杜绝 FlowRow NoSuchMethodError 与文字折行
                            Text(
                                text = "糖尿病类型（必填）",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            DIABETES_TYPE_OPTIONS.chunked(2).forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowOptions.forEach { option ->
                                        val isSelected = diabetesType == option
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                diabetesType = option
                                                errorMessage = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            label = {
                                                Text(
                                                    text = option,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            ),
                                        )
                                    }
                                    if (rowOptions.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // 胰岛素类型选择：采用安全的双列自适应等宽布局
                            Text(
                                text = "胰岛素类型（必填）",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            INSULIN_TYPE_OPTIONS.chunked(2).forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowOptions.forEach { option ->
                                        val isSelected = insulinType == option
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                insulinType = option
                                                errorMessage = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            label = {
                                                Text(
                                                    text = option,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            ),
                                        )
                                    }
                                }
                            }

                            // 剂量区间
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = insulinMinText,
                                    onValueChange = { insulinMinText = it },
                                    label = { Text("最小剂量 IU") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                                OutlinedTextField(
                                    value = insulinMaxText,
                                    onValueChange = { insulinMaxText = it },
                                    label = { Text("最大剂量 IU") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }

                            // 身高体重
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = heightText,
                                    onValueChange = { heightText = it },
                                    label = { Text("身高 cm（选填）") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = { weightText = it },
                                    label = { Text("体重 kg（选填）") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }

                        onboardingSubmissionState.errorMessage?.let {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OnboardingNav(
                            canBack = true,
                            onBack = { page = OnboardingPage.Disclaimer },
                            onNext = {
                                if (diabetesType.isBlank()) {
                                    errorMessage = "请选择您的糖尿病类型"
                                    return@OnboardingNav
                                }
                                if (insulinType.isBlank()) {
                                    errorMessage = "请选择您的胰岛素类型"
                                    return@OnboardingNav
                                }
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
                            nextLabel = if (onboardingSubmissionState.isSubmitting) "保存中..." else "完成并进入主页",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentPage: OnboardingPage) {
    val totalSteps = OnboardingPage.values().size
    val currentIndex = currentPage.ordinal

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until totalSteps) {
            val isActive = i == currentIndex
            val isDone = i < currentIndex
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isDone -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun OnboardFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun HighlightCard(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
            )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canBack) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "上一步",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        Button(
            onClick = onNext,
            enabled = nextEnabled,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            ),
            modifier = Modifier
                .weight(if (canBack) 1.3f else 1f)
                .height(46.dp),
        ) {
            Text(
                text = nextLabel,
                color = if (nextEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false,
            )
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
