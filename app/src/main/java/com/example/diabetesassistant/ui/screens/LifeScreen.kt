package com.example.diabetesassistant.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.model.InsulinType
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.data.repository.UserRepository
import com.example.diabetesassistant.ui.components.AmbientGlowBackground
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val healthProfile: HealthProfile = HealthProfile(
        userId = 1L,
        diabetesType = "2型糖尿病",
        insulinType = "速效",
        insulinMin = 2f,
        insulinMax = 10f,
        weightKg = 68f,
        heightCm = 172f,
    ),
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val userAvatarUri: String = "",
    val userNickname: String = "健康生活家",
    val dailyCarbTarget: Float = 130f,
    val isSaved: Boolean = false,
)

@HiltViewModel
class LifeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val foodRecordRepository: FoodRecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getHealthProfile(1L) ?: HealthProfile(
                userId = 1L,
                diabetesType = "2型糖尿病",
                insulinType = "速效",
                insulinMin = 2f,
                insulinMax = 10f,
                weightKg = 68f,
                heightCm = 172f,
            )
            _uiState.update { it.copy(healthProfile = profile) }
        }
        viewModelScope.launch {
            userPreferencesDataSource.appThemeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferencesDataSource.userAvatarUriFlow.collect { uri ->
                _uiState.update { it.copy(userAvatarUri = uri) }
            }
        }
        viewModelScope.launch {
            userPreferencesDataSource.userNicknameFlow.collect { name ->
                _uiState.update { it.copy(userNickname = name) }
            }
        }
        viewModelScope.launch {
            userPreferencesDataSource.dailyCarbTargetFlow.collect { target ->
                _uiState.update { it.copy(dailyCarbTarget = target) }
            }
        }
    }

    fun updateProfile(
        diabetesType: String,
        insulinType: String,
        height: Float,
        weight: Float,
    ) {
        viewModelScope.launch {
            val updated = _uiState.value.healthProfile.copy(
                diabetesType = diabetesType,
                insulinType = insulinType,
                heightCm = height,
                weightKg = weight,
            )
            userRepository.saveHealthProfile(updated)
            _uiState.update { it.copy(healthProfile = updated, isSaved = true) }
        }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            userPreferencesDataSource.setUserAvatarUri(uri)
        }
    }

    fun updateNickname(name: String) {
        viewModelScope.launch {
            userPreferencesDataSource.setUserNickname(name)
        }
    }

    fun updateDailyCarbTarget(target: Float) {
        viewModelScope.launch {
            userPreferencesDataSource.setDailyCarbTarget(target)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            userPreferencesDataSource.setThemeMode(mode)
        }
    }

    fun clearAllRecords(context: android.content.Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            foodRecordRepository.deleteAll()
            try {
                val dir = java.io.File(context.cacheDir, "camera_captures")
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
            } catch (_: Exception) {}
            onComplete()
        }
    }
}

/**
 * 现代极简个人代谢中心与设置（纯私有·免密）
 */
@Composable
fun LifeScreen(
    modifier: Modifier = Modifier,
    onResetComplete: () -> Unit = {},
    viewModel: LifeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateAvatar(uri.toString())
                Toast.makeText(context, "头像已更新", Toast.LENGTH_SHORT).show()
            }
        }
    )
    var showEditNicknameDialog by remember { mutableStateOf(false) }
    var showCarbTargetDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showClearRecordsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val heightM = (state.healthProfile.heightCm ?: 170f) / 100f
    val weightKg = state.healthProfile.weightKg ?: 65f
    val bmi = if (heightM > 0f) weightKg / (heightM * heightM) else 22f

    AmbientGlowBackground(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // 1. 顶部个人健康档案卡片
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            // 可点击更换头像
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                            ) {
                                if (state.userAvatarUri.isNotBlank()) {
                                    AsyncImage(
                                        model = state.userAvatarUri,
                                        contentDescription = "用户头像",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                }

                                // 头像右下角相机角标
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "更换头像",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(11.dp),
                                    )
                                }
                            }

                            // 可编辑昵称与隐私标签
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = state.userNickname,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    IconButton(
                                        onClick = { showEditNicknameDialog = true },
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "修改昵称",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = "数据 100% 仅保存在本机私有数据库中",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        // 核心参数展示条
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("类型", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.healthProfile.diabetesType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("胰岛素", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.healthProfile.insulinType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.1f".format(bmi), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                        ) {
                            Text(
                                text = "编辑控糖档案与参数",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }

            // 2. 偏好与系统设置项
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp,
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        SettingItemRow(
                            icon = Icons.Filled.TrackChanges,
                            title = "每日目标碳水限额",
                            subtitle = "${state.dailyCarbTarget.toInt()}g (点击自定义每日控糖配额)",
                            onClick = { showCarbTargetDialog = true },
                        )
                        SettingItemRow(
                            icon = Icons.Filled.DarkMode,
                            title = "外观显示模式",
                            subtitle = when (state.themeMode) {
                                AppThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                                AppThemeMode.LIGHT -> "浅色模式"
                                AppThemeMode.DARK -> "深色模式"
                            },
                            onClick = { showThemeDialog = true },
                        )
                        SettingItemRow(
                            icon = Icons.Filled.Description,
                            title = "医疗健康免责声明",
                            subtitle = "非医疗处方建议规范说明",
                            onClick = { showDisclaimerDialog = true },
                        )
                        SettingItemRow(
                            icon = Icons.Filled.Info,
                            title = "关于软件",
                            subtitle = "糖控助手 v1.0 (纯私有离线版)",
                            onClick = { showAboutDialog = true },
                        )
                        SettingItemRow(
                            icon = Icons.Filled.DeleteForever,
                            title = "重置本地餐食数据",
                            subtitle = "清空设备中所有历史拍照与测算分析",
                            tint = Color(0xFFD62828),
                            onClick = { showClearRecordsDialog = true },
                        )
                    }
                }
            }
        }
    }

    // 昵称编辑弹窗
    if (showEditNicknameDialog) {
        var tempName by remember { mutableStateOf(state.userNickname) }
        AlertDialog(
            onDismissRequest = { showEditNicknameDialog = false },
            title = { Text("修改个人昵称", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { if (it.length <= 16) tempName = it },
                    label = { Text("用户昵称 (最长16字)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.updateNickname(tempName.trim())
                            Toast.makeText(context, "昵称已保存", Toast.LENGTH_SHORT).show()
                        }
                        showEditNicknameDialog = false
                    }
                ) {
                    Text("保存", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNicknameDialog = false }) { Text("取消") }
            },
        )
    }

    // 每日碳水目标设置弹窗
    if (showCarbTargetDialog) {
        var tempTarget by remember { mutableStateOf(state.dailyCarbTarget) }
        val targetInt = tempTarget.toInt()

        val currentTierInfo = when {
            targetInt >= 240 -> "🟢 日常维持档：适合健康成年人与日常关注人群，推荐每餐约 80~90g"
            targetInt >= 190 -> "🟡 适度控糖档：适合轻度控糖与防峰人群，推荐每餐约 65~70g"
            targetInt >= 150 -> "🟠 稳健控糖档：适合常规糖尿病生活管理，推荐每餐约 50g"
            else -> "🔴 严格临床档：适合临床强化期或极低碳干预，推荐每餐约 35~40g"
        }

        AlertDialog(
            onDismissRequest = { showCarbTargetDialog = false },
            title = { Text("设置每日目标碳水限额", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "当前目标配额",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$targetInt 克/天",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentTierInfo,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp,
                            )
                        }
                    }

                    Slider(
                        value = tempTarget,
                        onValueChange = { tempTarget = it },
                        valueRange = 50f..300f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("50g 极低碳", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("180g 控糖均线", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("300g 常人标准", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = "科学预设档位参考：",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            130f to "130g 严格",
                            160f to "160g 稳健",
                            210f to "210g 适度",
                            260f to "260g 日常",
                        ).forEach { (v, lbl) ->
                            FilterChip(
                                selected = targetInt == v.toInt(),
                                onClick = { tempTarget = v },
                                label = { Text(lbl, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDailyCarbTarget(tempTarget)
                        Toast.makeText(context, "控糖配额已更新为 ${tempTarget.toInt()}g", Toast.LENGTH_SHORT).show()
                        showCarbTargetDialog = false
                    }
                ) {
                    Text("保存", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCarbTargetDialog = false }) { Text("取消") }
            },
        )
    }

    // 档案编辑弹窗
    if (showEditProfileDialog) {
        var diabetesType by remember { mutableStateOf(state.healthProfile.diabetesType) }
        var insulinType by remember { mutableStateOf(state.healthProfile.insulinType) }
        var heightText by remember { mutableStateOf((state.healthProfile.heightCm ?: 172f).toInt().toString()) }
        var weightText by remember { mutableStateOf((state.healthProfile.weightKg ?: 68f).toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("修改个人健康档案", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("糖尿病类型", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("2型糖尿病", "糖尿病前期", "1型糖尿病").chunked(2).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowTypes.forEach { type ->
                                val isSelected = diabetesType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { diabetesType = type },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                )
                            }
                            if (rowTypes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Text("胰岛素类型", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("速效", "短效", "长效", "未使用").chunked(2).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowTypes.forEach { type ->
                                val isSelected = insulinType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { insulinType = type },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = heightText.toFloatOrNull() ?: 172f
                        val w = weightText.toFloatOrNull() ?: 68f
                        viewModel.updateProfile(diabetesType, insulinType, h, w)
                        Toast.makeText(context, "健康档案已保存", Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    }
                ) {
                    Text("保存", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    // 主题切换弹窗
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("选择外观显示模式", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    listOf(
                        AppThemeMode.FOLLOW_SYSTEM to "跟随系统",
                        AppThemeMode.LIGHT to "浅色模式",
                        AppThemeMode.DARK to "深色模式",
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                        ) {
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("完成", color = MaterialTheme.colorScheme.primary) }
            },
        )
    }

    // 免责声明弹窗
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = { Text("医疗健康与合规免责声明", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    text = "1. 本应用旨在通过食物图像视觉识别、权威营养库对照与吸收动力学数学模型，为用户提供日常膳食碳水预估与血糖波动预测。\n\n" +
                           "2. 算法生成的预测曲线、峰值及胰岛素色带仅作为膳食搭配与健康自律的参考工具，绝不构成任何临床医疗诊断、胰岛素注射处方或医疗处置建议。\n\n" +
                           "3. 用户在调整用药剂量、胰岛素使用或治疗计划前，必须严格咨询执业医师或内分泌专科医生并遵照医嘱。",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("我已知晓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    // 清空数据确认弹窗
    if (showClearRecordsDialog) {
        AlertDialog(
            onDismissRequest = { showClearRecordsDialog = false },
            title = { Text("确认重置本地数据？", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("此操作将清空本地所有的餐食分析记录，数据无法恢复。", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords(context) {
                            Toast.makeText(context, "已成功清空所有本地餐食数据", Toast.LENGTH_SHORT).show()
                            onResetComplete()
                        }
                        showClearRecordsDialog = false
                    }
                ) {
                    Text("确认清空", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecordsDialog = false }) { Text("取消") }
            },
        )
    }

    // 关于软件弹窗
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于糖控助手", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "糖控助手 v1.0 (纯私有离线架构)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "1. 100% 本地隐私防护：所有测算结果与餐食图片仅持久化于设备私有沙盒 Room 数据库中，不设云端数据中心存储，保障完全私有与免密自主控制。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                    Text(
                        text = "2. 权威营养数据库：以 USDA FoodData Central 官方数据库为基准，辅以烹饪热力学与吸油吸水损耗校准真实可吸收碳水。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                    Text(
                        text = "3. 动态升糖预测：基于胃排空速率动力学方程与双室代谢模型，提供全天时段关注预警与个性化饮食微建议。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("了解", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    val iconColor = tint ?: MaterialTheme.colorScheme.primary
    val titleColor = tint ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LifeScreenPreview() {
    LifeScreen()
}
