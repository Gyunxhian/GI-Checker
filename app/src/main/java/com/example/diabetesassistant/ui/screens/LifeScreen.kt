package com.example.diabetesassistant.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground

private val ProfileBackground = Color(0xFFF3F8F4)
private val ProfileGreen = Color(0xFF007A35)
private val ProfileBrightGreen = Color(0xFF43FF82)
private val ProfileInk = Color(0xFF17251E)
private val ProfileMuted = Color(0xFF6B7770)
private val ProfileCell = Color.White.copy(alpha = 0.44f)
private val ProfileWarning = Color(0xFFFFC34D)
private val ProfileDanger = Color(0xFFFF6262)

@Composable
fun LifeScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onAnalysisPlanClick: () -> Unit = {},
    onMedicationClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onDarkModeClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ProfileTopBar()
            ProfileHeroCard(onClick = onProfileClick)
            HealthMetricGrid(onClick = onProfileClick)
            GoalSettingCard(onClick = onAnalysisPlanClick)
            ReminderCard(onClick = onMedicationClick)
            SettingsList(
                onNotificationClick = onNotificationClick,
                onDarkModeClick = onDarkModeClick,
                onSyncClick = onSyncClick,
                onPrivacyClick = onPrivacyClick,
                onSignOutClick = onSignOutClick,
            )
        }
    }
}

@Composable
fun ProfileDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "个人资料",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        ProfileHeroCard(onClick = {})
        SectionTitle("基础信息")
        ProfileInfoRow("姓名", "张晓明")
        ProfileInfoRow("年龄", "32 岁")
        ProfileInfoRow("身高", "178 cm")
        ProfileInfoRow("体重", "70 kg")
        ProfileInfoRow("糖尿病类型", "2 型")
        ProfileInfoRow("主要目标", "控制餐后糖负荷，减少高糖波动")

        SectionTitle("识别偏好")
        ProfileInfoRow("常用餐型", "中式正餐、轻食、咖啡饮品")
        ProfileInfoRow("运动习惯", "餐后步行，平均 25 分钟")
        ProfileInfoRow("过敏与忌口", "无已记录")
    }
}

@Composable
fun AnalysisPlanScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "分析方案",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "内置算法会结合食材重量、烹饪方式、吸收速度和个人方案，估算本餐糖摄入并生成运动与药量复核建议。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ProfileMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                PlanProgressRow("每餐糖摄入目标", "35g", 0.68f)
                PlanProgressRow("高糖负荷提醒线", "50g", 0.82f)
                PlanProgressRow("碳水换算系数", "10g 糖 / 1 单位", 0.72f)
                PlanProgressRow("餐后运动目标", "30 分钟", 0.64f)
            }
        }

        SectionTitle("算法偏好")
        PreferenceCard(
            icon = Icons.Filled.Restaurant,
            title = "烹饪方式权重",
            subtitle = "油炸、糖醋、勾芡等做法会提高糖负荷提醒等级。",
            badge = "已启用",
        )
        PreferenceCard(
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            title = "运动建议强度",
            subtitle = "根据本餐糖摄入和历史运动习惯给出步行时长。",
            badge = "适中",
        )
        PreferenceCard(
            icon = Icons.Filled.Medication,
            title = "药量复核规则",
            subtitle = "仅提示复核，不自动给出医疗处方。",
            badge = "谨慎",
        )
    }
}

@Composable
fun MedicationPlanScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "用药方案",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "当前方案",
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                MedicationRow("二甲双胍", "每日两次 · 早晚餐后", "固定")
                MedicationRow("胰岛素剂量核对", "高糖负荷餐前后提醒复核", "提醒")
            }
        }

        SectionTitle("复核触发条件")
        RuleCard("本餐可吸收糖超过 50g", "显示药量复核提醒，并建议查看医生方案。", ProfileDanger)
        RuleCard("识别到甜品、含糖饮品或勾芡菜", "提高提醒等级，建议减少下一餐主食。", ProfileWarning)
        RuleCard("餐后运动目标未完成", "推送轻运动建议，帮助平稳餐后曲线。", ProfileGreen)
    }
}

@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "建议提醒",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        ToggleCard("餐后运动提醒", "识别高糖负荷餐后，提醒步行或轻运动。", true)
        ToggleCard("用药复核提醒", "在需要时提醒查看医生方案，不直接替代医疗判断。", true)
        ToggleCard("晚餐主食提醒", "当天糖摄入偏高时，晚餐前提醒减少主食。", true)
        ToggleCard("每周分析报告", "汇总一周餐食识别、糖摄入和建议完成情况。", false)
    }
}

@Composable
fun DarkModeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "深色模式",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        ToggleCard("跟随系统", "根据手机系统设置自动切换浅色或深色界面。", true)
        ToggleCard("夜间降低亮度", "减少夜间查看建议时的屏幕刺激。", true)
        ToggleCard("保留绿色毛玻璃风格", "深色界面仍使用绿色强调色和半透明卡片。", true)
    }
}

@Composable
fun SyncArchiveScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "同步分析档案",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, tint = ProfileGreen)
                    Text(
                        text = "同步状态良好",
                        style = MaterialTheme.typography.titleMedium,
                        color = ProfileInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                ProfileInfoRow("最近同步", "今天 12:42")
                ProfileInfoRow("已同步餐食分析", "128 条")
                ProfileInfoRow("本地缓存", "开启")
            }
        }
        ToggleCard("仅在无线网络同步", "减少移动网络流量使用。", true)
        ToggleCard("同步识别图片", "用于回看餐食和校正识别结果。", false)
        ToggleCard("同步算法偏好", "在新设备上恢复分析方案和提醒设置。", true)
    }
}

@Composable
fun PrivacyDataScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSecondaryScaffold(
        title = "隐私与数据",
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        PreferenceCard(
            icon = Icons.Filled.Lock,
            title = "本地优先处理",
            subtitle = "餐食图片和识别结果优先保存在本机，减少不必要上传。",
            badge = "推荐",
        )
        PreferenceCard(
            icon = Icons.Filled.PrivacyTip,
            title = "敏感信息保护",
            subtitle = "用药方案、身体指标和餐食档案会作为敏感数据管理。",
            badge = "已开启",
        )
        PreferenceCard(
            icon = Icons.Filled.Sync,
            title = "数据导出",
            subtitle = "可将分析档案导出给医生或营养师参考。",
            badge = "可用",
        )
        SectionTitle("数据管理")
        ProfileInfoRow("清理识别缓存", "保留分析结果")
        ProfileInfoRow("删除全部档案", "需要二次确认")
        ProfileInfoRow("隐私政策", "查看")
    }
}

@Composable
private fun ProfileSecondaryScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ProfileSecondaryTopBar(title = title, onBackClick = onBackClick)
            content()
        }
    }
}

@Composable
private fun ProfileSecondaryTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onBackClick),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.36f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = ProfileGreen,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ProfileInk,
            fontWeight = FontWeight.ExtraBold,
        )
        Box(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = Color(0xFF2D6B61),
                modifier = Modifier.size(34.dp),
            )
            Text(
                text = "糖控助手",
                style = MaterialTheme.typography.titleMedium,
                color = ProfileInk,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = ProfileGreen,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ProfileHeroCard(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ShoeAvatar()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "张晓明",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ProfileBrightGreen.copy(alpha = 0.26f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = ProfileGreen,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "已完成 128 次餐食分析",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfileGreen,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ProfileMuted.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ShoeAvatar() {
    Box(
        modifier = Modifier
            .size(74.dp)
            .background(Color.White.copy(alpha = 0.68f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 23.dp)
                .background(Color(0xFF2D302B), RoundedCornerShape(50))
                .border(1.dp, Color(0xFF5B5F55), RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun HealthMetricGrid(onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            MetricTile(Icons.Filled.Height, "178", "cm", Modifier.weight(1f))
            MetricTile(Icons.Filled.FitnessCenter, "70", "kg", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            MetricTile(Icons.Filled.CalendarMonth, "32", "岁", Modifier.weight(1f))
            MetricTile(Icons.Filled.Bloodtype, "2 型", "", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(92.dp)
            .background(ProfileCell),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfileMuted.copy(alpha = 0.52f),
                modifier = Modifier.size(22.dp),
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = ProfileMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalSettingCard(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.TrackChanges,
                    contentDescription = null,
                    tint = ProfileGreen,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "分析方案",
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            GoalProgressRow("碳水换算系数", "10g 糖 / 1 单位", 0.72f)
            GoalProgressRow("餐后运动目标", "30 分钟", 0.64f)
        }
    }
}

@Composable
private fun ReminderCard(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(ProfileBrightGreen.copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = ProfileGreen,
                    modifier = Modifier.size(21.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "用药方案",
                    style = MaterialTheme.typography.titleSmall,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "二甲双胍 · 胰岛素剂量核对",
                    style = MaterialTheme.typography.labelMedium,
                    color = ProfileMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = ProfileBrightGreen.copy(alpha = 0.22f),
            ) {
                Text(
                    text = "餐后",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ProfileGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun SettingsList(
    onNotificationClick: () -> Unit,
    onDarkModeClick: () -> Unit,
    onSyncClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val items = listOf(
        SettingItem(Icons.Filled.Notifications, "建议提醒", onNotificationClick),
        SettingItem(Icons.Filled.DarkMode, "深色模式", onDarkModeClick),
        SettingItem(Icons.Filled.Sync, "同步分析档案", onSyncClick),
        SettingItem(Icons.Filled.PrivacyTip, "隐私与数据", onPrivacyClick),
        SettingItem(Icons.Filled.Logout, "退出登录", onSignOutClick),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            SettingRow(item)
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = ProfileGreen.copy(alpha = 0.08f),
                spotColor = ProfileGreen.copy(alpha = 0.08f),
            )
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.White.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = ProfileMuted.copy(alpha = 0.54f),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = ProfileInk,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ProfileMuted.copy(alpha = 0.42f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = ProfileInk,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ProfileMuted,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ProfileInk,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun PlanProgressRow(
    label: String,
    value: String,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ProfileMuted,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = ProfileGreen,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = ProfileGreen,
            trackColor = Color(0xFFE1ECE6),
        )
    }
}

@Composable
private fun GoalProgressRow(
    label: String,
    value: String,
    progress: Float,
) = PlanProgressRow(label = label, value = value, progress = progress)

@Composable
private fun PreferenceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.64f),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ProfileBrightGreen.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = ProfileGreen, modifier = Modifier.size(23.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ProfileInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfileMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Surface(shape = RoundedCornerShape(50), color = ProfileBrightGreen.copy(alpha = 0.20f)) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfileGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun MedicationRow(
    name: String,
    description: String,
    badge: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.44f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Medication,
            contentDescription = null,
            tint = ProfileGreen,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.titleSmall, color = ProfileInk, fontWeight = FontWeight.ExtraBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = ProfileMuted, fontWeight = FontWeight.SemiBold)
        }
        Text(badge, style = MaterialTheme.typography.labelMedium, color = ProfileGreen, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun RuleCard(
    title: String,
    subtitle: String,
    color: Color,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.64f),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape),
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = ProfileInk, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ProfileMuted, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    checkedByDefault: Boolean,
) {
    var checked by remember { mutableStateOf(checkedByDefault) }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.64f),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ProfileInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfileMuted,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.36f))
        }
    }
}

@Immutable
private data class SettingItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)
