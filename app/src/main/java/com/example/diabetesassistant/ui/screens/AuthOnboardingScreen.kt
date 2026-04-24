package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val AuthGreen = Color(0xFF087A3D)
private val AuthLime = Color(0xFFC2E05E)
private val AuthInk = Color(0xFF15241D)
private val AuthMuted = Color(0xFF6D7B72)
private val AuthSurface = Color(0xFFF8FBF7)

@Composable
fun AuthOnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableIntStateOf(0) }
    var agreed by remember { mutableStateOf(false) }
    var diabetesType by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val canContinue = when (step) {
        0 -> account.isNotBlank() && password.length >= 6
        1 -> agreed
        else -> diabetesType.isNotBlank() && insulinType.isNotBlank()
    }

    AuthBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 34.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            AuthHeader(step = step)

            AuthPanel {
                when (step) {
                    0 -> LoginStep(
                        account = account,
                        onAccountChange = { account = it },
                        password = password,
                        onPasswordChange = { password = it },
                    )
                    1 -> DisclaimerStep(agreed = agreed, onAgreeChange = { agreed = it })
                    else -> HealthProfileStep(
                        diabetesType = diabetesType,
                        onDiabetesTypeChange = { diabetesType = it },
                        insulinType = insulinType,
                        onInsulinTypeChange = { insulinType = it },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (step > 0) {
                    TextButton(
                        onClick = { step -= 1 },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("返回", color = AuthMuted, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Button(
                    onClick = {
                        if (step == 2) onFinished() else step += 1
                    },
                    enabled = canContinue,
                    modifier = Modifier
                        .weight(if (step > 0) 1.8f else 1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthGreen,
                        disabledContainerColor = Color(0xFFD5E0D8),
                    ),
                ) {
                    Text(
                        text = when (step) {
                            0 -> "登录并继续"
                            1 -> "同意并继续"
                            else -> "完成设置"
                        },
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthBackground(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF9FCF8),
                        Color(0xFFEFF6EF),
                        Color(0xFFE1EEE4),
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .background(AuthLime.copy(alpha = 0.18f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(220.dp)
                .background(AuthGreen.copy(alpha = 0.08f), CircleShape)
        )
        content()
    }
}

@Composable
private fun AuthHeader(step: Int) {
    val titles = listOf("登录账号", "安全确认", "个人资料")
    val subtitles = listOf(
        "先建立你的本地账号，之后再补充健康档案。",
        "确认使用边界，应用只提供日常记录与提醒。",
        "填写必填信息，让提醒更贴合你的方案。",
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = titles[step],
                    style = MaterialTheme.typography.headlineMedium,
                    color = AuthInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitles[step],
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (step > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
                ) {
                    Text(
                        text = "$step/2",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = AuthGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
        if (step > 0) {
            LinearProgressIndicator(
                progress = { step / 2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = AuthGreen,
                trackColor = Color.White.copy(alpha = 0.70f),
            )
        }
    }
}

@Composable
private fun AuthPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = AuthGreen.copy(alpha = 0.10f),
                spotColor = AuthGreen.copy(alpha = 0.14f),
            ),
        shape = RoundedCornerShape(28.dp),
        color = AuthSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f)),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}

@Composable
private fun LoginStep(
    account: String,
    onAccountChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
) {
    Text(
        text = "欢迎回来",
        style = MaterialTheme.typography.headlineSmall,
        color = AuthInk,
        fontWeight = FontWeight.ExtraBold,
    )
    Text(
        text = "使用手机号或邮箱登录。当前为本地模拟账号，信息仅保存在本机。",
        style = MaterialTheme.typography.bodyMedium,
        color = AuthMuted,
        fontWeight = FontWeight.SemiBold,
    )
    CleanTextField(
        value = account,
        onValueChange = onAccountChange,
        label = "手机号或邮箱",
        keyboardType = KeyboardType.Email,
    )
    CleanTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = "密码，至少 6 位",
        keyboardType = KeyboardType.Password,
        password = true,
    )
}

@Composable
private fun DisclaimerStep(
    agreed: Boolean,
    onAgreeChange: (Boolean) -> Unit,
) {
    StepIcon(Icons.Filled.VerifiedUser)
    Text(
        text = "免责声明",
        style = MaterialTheme.typography.headlineSmall,
        color = AuthInk,
        fontWeight = FontWeight.ExtraBold,
    )
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, Color(0xFFE4EEE7)),
    ) {
        Text(
            text = "本应用提供餐食识别、糖负荷估算和温馨提醒，结果仅用于日常记录与沟通参考，不构成医疗诊断、处方或胰岛素剂量建议。请始终以医生方案为准。",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AuthMuted,
            fontWeight = FontWeight.SemiBold,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAgreeChange(!agreed) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = agreed, onCheckedChange = onAgreeChange)
        Text(
            text = "我已阅读并同意",
            style = MaterialTheme.typography.bodyMedium,
            color = AuthInk,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HealthProfileStep(
    diabetesType: String,
    onDiabetesTypeChange: (String) -> Unit,
    insulinType: String,
    onInsulinTypeChange: (String) -> Unit,
) {
    StepIcon(Icons.Filled.Medication)
    Text(
        text = "必填健康档案",
        style = MaterialTheme.typography.headlineSmall,
        color = AuthInk,
        fontWeight = FontWeight.ExtraBold,
    )
    Text(
        text = "这些信息会用于调整提醒语气与药量复核区间，之后可在“我的”页面修改。",
        style = MaterialTheme.typography.bodyMedium,
        color = AuthMuted,
        fontWeight = FontWeight.SemiBold,
    )
    OptionGroup(
        title = "糖尿病类型",
        options = listOf("1 型", "2 型", "妊娠期", "其他"),
        selected = diabetesType,
        onSelected = onDiabetesTypeChange,
    )
    OptionGroup(
        title = "胰岛素类型",
        options = listOf("速效", "短效", "中效", "长效"),
        selected = insulinType,
        onSelected = onInsulinTypeChange,
    )
}

@Composable
private fun CleanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    password: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthGreen,
            unfocusedBorderColor = Color(0xFFDCE7DF),
            focusedContainerColor = Color.White.copy(alpha = 0.82f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.68f),
        ),
    )
}

@Composable
private fun StepIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(AuthGreen.copy(alpha = 0.10f), CircleShape)
            .border(1.dp, AuthGreen.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AuthGreen, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun OptionGroup(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = AuthInk,
            fontWeight = FontWeight.ExtraBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                ChoiceRow(
                    text = option,
                    selected = selected == option,
                    onClick = { onSelected(option) },
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFE8F6EC) else Color.White.copy(alpha = 0.66f),
        border = BorderStroke(1.dp, if (selected) AuthGreen.copy(alpha = 0.38f) else Color(0xFFE5EEE8)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                color = if (selected) AuthGreen else AuthInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(if (selected) AuthGreen else Color.Transparent, CircleShape)
                    .border(1.dp, if (selected) AuthGreen else Color(0xFFCBD8D0), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
