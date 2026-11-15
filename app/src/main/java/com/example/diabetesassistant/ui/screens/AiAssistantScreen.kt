package com.example.diabetesassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.diabetesassistant.camera.AnalysisResultJson
import com.example.diabetesassistant.data.local.dao.AiChatMessageDao
import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.entity.AiChatMessage
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.domain.privacy.PrivacyDataMasker
import com.example.diabetesassistant.domain.search.MedicalSearchProvider
import com.example.diabetesassistant.domain.search.SearchEvidence
import com.example.diabetesassistant.ui.components.AmbientGlowBackground
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.MarkdownText
import com.example.diabetesassistant.ui.theme.HealthPalette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StreamingMessageState(
    val reasoning: String = "",
    val content: String = "",
    val isThinking: Boolean = false,
    val searchSourcesJson: String = "",
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiChatMessageDao: AiChatMessageDao,
    private val aiRepository: AiRepository,
    private val medicalSearchProvider: MedicalSearchProvider,
    private val privacyDataMasker: PrivacyDataMasker,
    private val healthProfileDao: HealthProfileDao,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val foodRecordRepository: FoodRecordRepository,
) : ViewModel() {

    private val gson = Gson()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusText = MutableStateFlow<String?>(null)
    val statusText: StateFlow<String?> = _statusText.asStateFlow()

    private val _isThinkingEnabled = MutableStateFlow(true)
    val isThinkingEnabled: StateFlow<Boolean> = _isThinkingEnabled.asStateFlow()

    private val _streamingMessage = MutableStateFlow<StreamingMessageState?>(null)
    val streamingMessage: StateFlow<StreamingMessageState?> = _streamingMessage.asStateFlow()

    val chatMessages: StateFlow<List<AiChatMessage>> = aiChatMessageDao.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun toggleThinking(enabled: Boolean) {
        _isThinkingEnabled.value = enabled
    }

    fun sendQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            // 1. 存入用户提问
            aiChatMessageDao.insert(
                AiChatMessage(
                    role = "user",
                    content = trimmed,
                    timestamp = System.currentTimeMillis(),
                )
            )

            _isLoading.value = true
            _statusText.value = "正在检索权威临床指南..."

            try {
                // 2. 准备本地脱敏健康背景
                val userId = userPreferencesDataSource.signedInUserIdFlow.first()
                val healthProfile = userId?.let { healthProfileDao.getByUserId(it) }
                val carbTarget = userPreferencesDataSource.dailyCarbTargetFlow.first()
                val allRecords = foodRecordRepository.observeAll().first()

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                var todayCarbs = 0f
                allRecords.filter { it.recordDate >= cal.timeInMillis }.forEach { rec ->
                    try {
                        val parsed = gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
                        todayCarbs += (parsed?.totalCarbIntake ?: 0f)
                    } catch (_: Exception) {}
                }

                val privateContext = privacyDataMasker.maskToPrivateContext(
                    healthProfile = healthProfile,
                    dailyCarbTarget = carbTarget,
                    todayCarbs = todayCarbs,
                    recentRecords = allRecords,
                )

                // 3. 权威医学检索
                val searchEvidences = medicalSearchProvider.searchMedicalEvidence(trimmed)
                val evidenceJson = gson.toJson(searchEvidences)

                val thinkingMode = _isThinkingEnabled.value
                _statusText.value = if (thinkingMode) "GI 助手正在深度推理中..." else "GI 助手正在生成解答..."
                _streamingMessage.value = StreamingMessageState(
                    isThinking = thinkingMode,
                    searchSourcesJson = evidenceJson,
                )

                val fullReasoning = StringBuilder()
                val fullContent = StringBuilder()

                // 4. 流式接收 AI 生成回答
                aiRepository.askHealthAssistantStream(
                    userQuestion = trimmed,
                    privateContext = privateContext,
                    searchEvidences = searchEvidences,
                    enableThinking = thinkingMode,
                ).collect { chunk ->
                    if (chunk.reasoningDelta.isNotEmpty()) {
                        fullReasoning.append(chunk.reasoningDelta)
                        _streamingMessage.value = _streamingMessage.value?.copy(
                            reasoning = fullReasoning.toString(),
                            isThinking = true,
                        )
                        _statusText.value = "GI 助手正在深度推理中..."
                    }

                    if (chunk.contentDelta.isNotEmpty()) {
                        fullContent.append(chunk.contentDelta)
                        _streamingMessage.value = _streamingMessage.value?.copy(
                            content = fullContent.toString(),
                            isThinking = false,
                        )
                        _statusText.value = "GI 助手正在流式生成解答..."
                    }

                    if (chunk.errorMessage != null) {
                        fullContent.append("\n\n[网络提示] ").append(chunk.errorMessage)
                    }

                    if (chunk.isDone) {
                        var finalReasoning = fullReasoning.toString()
                        var rawContent = fullContent.toString()

                        if (rawContent.contains("</think>")) {
                            val parts = rawContent.split("</think>", limit = 2)
                            if (finalReasoning.isBlank()) {
                                finalReasoning = parts[0].replace("<think>", "").trim()
                            }
                            rawContent = parts.getOrElse(1) { "" }.trim()
                        }
                        rawContent = rawContent.replace("<think>", "").replace("</think>", "").trim()

                        val finalContent = rawContent.ifBlank {
                            if (finalReasoning.isNotBlank()) {
                                "已完成临床逻辑推演，请展开上方思维链查看详细推理。建议根据您的近期餐食碳水与血糖变化，遵循循序渐进的健康调理原则。"
                            } else {
                                "抱歉，暂时未能获取到有效解答，请稍后重试。"
                            }
                        }
                        aiChatMessageDao.insert(
                            AiChatMessage(
                                role = "assistant",
                                content = finalContent,
                                reasoningContent = finalReasoning,
                                searchSourcesJson = evidenceJson,
                                timestamp = System.currentTimeMillis(),
                            )
                        )
                        _streamingMessage.value = null
                    }
                }
            } catch (e: Exception) {
                aiChatMessageDao.insert(
                    AiChatMessage(
                        role = "assistant",
                        content = "处理提问时发生错误：${e.message}。请稍后重试。",
                        timestamp = System.currentTimeMillis(),
                    )
                )
            } finally {
                _isLoading.value = false
                _statusText.value = null
                _streamingMessage.value = null
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            aiChatMessageDao.deleteAll()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val isThinkingEnabled by viewModel.isThinkingEnabled.collectAsStateWithLifecycle()
    val streamingMessage by viewModel.streamingMessage.collectAsStateWithLifecycle()

    var inputQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val totalItems = messages.size + if (streamingMessage != null) 1 else 0

    LaunchedEffect(totalItems, streamingMessage?.content?.length, streamingMessage?.reasoning?.length) {
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val quickQuestions = listOf(
        "餐后头晕心慌出汗是怎么回事？",
        "今天这顿碳水吃超标了，该怎么补救？",
        "突发疑似低血糖，紧急处理步骤是什么？",
        "清晨空腹血糖总是偏高，原因有哪些？",
        "如何通过调整进餐顺序来平缓餐后血糖？",
    )

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空对话记录", fontWeight = FontWeight.Bold) },
            text = { Text("确认彻底清除当前设备上的所有 AI 助手历史对话？该操作将物理删除本地存储，不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("确认清空", color = HealthPalette.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    AmbientGlowBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(HealthPalette.pastelLavender()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = HealthPalette.Dark,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GI 专属健康顾问",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthPalette.textPrimary(),
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(HealthPalette.Secondary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "本地隐私脱敏 · 权威循证指南",
                                        fontSize = 10.sp,
                                        color = HealthPalette.textSecondary(),
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = HealthPalette.textPrimary(),
                            )
                        }
                    },
                    actions = {
                        // 思考开关药丸按钮 (切换深度思考模式)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isThinkingEnabled) HealthPalette.pastelLavender() else HealthPalette.chipSurface(),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isThinkingEnabled) HealthPalette.Secondary else HealthPalette.chipBorder()
                            ),
                            modifier = Modifier
                                .clickable { viewModel.toggleThinking(!isThinkingEnabled) }
                                .padding(end = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Psychology,
                                    contentDescription = "深度思考模式",
                                    tint = if (isThinkingEnabled) HealthPalette.Secondary else HealthPalette.textMuted(),
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isThinkingEnabled) "思考开启" else "思考关闭",
                                    fontSize = 11.sp,
                                    fontWeight = if (isThinkingEnabled) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isThinkingEnabled) HealthPalette.textPrimary() else HealthPalette.textMuted(),
                                )
                            }
                        }

                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "清空记录",
                                tint = HealthPalette.textSecondary(),
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
            ) {
                // 隐私脱敏与模式状态提示横幅
                Surface(
                    color = HealthPalette.pastelMint().copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = HealthPalette.Secondary,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isThinkingEnabled) {
                                "安全沙盒已启动 · 深度思考模式（输出多维临床鉴别思维链）"
                            } else {
                                "安全沙盒已启动 · 极速回答模式（已自动脱敏本地健康档案）"
                            },
                            fontSize = 10.5.sp,
                            color = HealthPalette.textPrimary(),
                            lineHeight = 14.sp,
                        )
                    }
                }

                // 消息列表与空状态占位 (占满剩余空间)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (messages.isEmpty() && streamingMessage == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                shape = RoundedCornerShape(24.dp),
                                elevation = 4.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(HealthPalette.pastelLavender()),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = HealthPalette.Secondary,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "欢迎使用 GI 智能顾问",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthPalette.textPrimary(),
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "我是您的专属代谢健康管家。支持流式 Markdown 排版与深度临床思维链分析。您可以向我咨询身体出现的任何不适症状或降糖控糖疑问。",
                                        fontSize = 13.sp,
                                        color = HealthPalette.textSecondary(),
                                        lineHeight = 19.sp,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "👇 点击下方快捷气泡快速发起咨询",
                                        fontSize = 12.sp,
                                        color = HealthPalette.Secondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                if (msg.role == "user") {
                                    UserMessageBubble(content = msg.content, timestamp = msg.timestamp)
                                } else {
                                    AssistantMessageBubble(
                                        content = msg.content,
                                        reasoningContent = msg.reasoningContent,
                                        sourcesJson = msg.searchSourcesJson,
                                        timestamp = msg.timestamp,
                                        isStreaming = false,
                                        isThinking = false,
                                    )
                                }
                            }

                            // 实时流式响应气泡
                            streamingMessage?.let { stream ->
                                item(key = "active_stream") {
                                    AssistantMessageBubble(
                                        content = stream.content,
                                        reasoningContent = stream.reasoning,
                                        sourcesJson = stream.searchSourcesJson,
                                        timestamp = System.currentTimeMillis(),
                                        isStreaming = true,
                                        isThinking = stream.isThinking,
                                    )
                                }
                            }

                            if (isLoading && streamingMessage == null && statusText != null) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(15.dp),
                                            strokeWidth = 2.dp,
                                            color = HealthPalette.Secondary,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = statusText ?: "",
                                            fontSize = 12.sp,
                                            color = HealthPalette.textSecondary(),
                                            fontStyle = FontStyle.Italic,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 底部输入区：紧贴软键盘上沿，键盘收起时紧贴系统导航栏
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // 快捷症状提问药丸滑动列表
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(quickQuestions) { q ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = HealthPalette.chipSurface(),
                                shadowElevation = 2.dp,
                                modifier = Modifier.clickable {
                                    viewModel.sendQuestion(q)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Psychology,
                                        contentDescription = null,
                                        tint = HealthPalette.Secondary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = q,
                                        fontSize = 12.sp,
                                        color = HealthPalette.textPrimary(),
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    // 输入框与发送按钮
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        elevation = 4.dp,
                        containerColor = HealthPalette.cardBackground(),
                        borderColor = HealthPalette.cardBorder(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = inputQuery,
                                onValueChange = { inputQuery = it },
                                placeholder = {
                                    Text(
                                        if (isThinkingEnabled) "向 GI 顾问咨询（已开启深度思考推理）..." else "向 GI 顾问咨询身体症状或饮食疑问...",
                                        fontSize = 13.sp,
                                        color = HealthPalette.textMuted(),
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 2.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HealthPalette.textPrimary(),
                                    unfocusedTextColor = HealthPalette.textPrimary(),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                ),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (inputQuery.isNotBlank() && !isLoading) {
                                            viewModel.sendQuestion(inputQuery)
                                            inputQuery = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                            )

                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = HealthPalette.Secondary,
                                    strokeWidth = 2.5.dp,
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        if (inputQuery.isNotBlank()) {
                                            viewModel.sendQuestion(inputQuery)
                                            inputQuery = ""
                                            focusManager.clearFocus()
                                        }
                                    },
                                    enabled = inputQuery.isNotBlank(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (inputQuery.isNotBlank()) HealthPalette.Secondary else Color.LightGray.copy(alpha = 0.5f)
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "发送",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(content: String, timestamp: Long) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(HealthPalette.Secondary, HealthPalette.Dark)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = content,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = timeFormat.format(Date(timestamp)),
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(end = 4.dp),
        )
    }
}

/**
 * 助手消息气泡：原生支持 Markdown 富文本、深度思考思维链（展开/收起）与权威循证文献引用
 */
@Composable
private fun AssistantMessageBubble(
    content: String,
    reasoningContent: String,
    sourcesJson: String,
    timestamp: Long,
    isStreaming: Boolean,
    isThinking: Boolean,
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val gson = remember { Gson() }

    val evidences = remember(sourcesJson) {
        if (sourcesJson.isNotBlank()) {
            try {
                val listType = object : TypeToken<List<SearchEvidence>>() {}.type
                gson.fromJson<List<SearchEvidence>>(sourcesJson, listType) ?: emptyList()
            } catch (_: Exception) {
                emptyList<SearchEvidence>()
            }
        } else {
            emptyList()
        }
    }

    val cleanReasoning = remember(reasoningContent, content) {
        if (reasoningContent.isNotBlank()) {
            reasoningContent.replace("<think>", "").replace("</think>", "").trim()
        } else if (content.contains("</think>")) {
            content.substringBefore("</think>").replace("<think>", "").trim()
        } else {
            ""
        }
    }

    val cleanContent = remember(content) {
        var c = content
        if (c.contains("</think>")) {
            c = c.substringAfter("</think>")
        }
        c.replace("<think>", "").replace("</think>", "").trim()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(HealthPalette.pastelLavender()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = HealthPalette.Dark,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxWidth(0.94f)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                    elevation = 3.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // 1. 深度思考思维链卡片（支持展开与收起，字体与排版与正文显著区分）
                        if (cleanReasoning.isNotBlank() || isThinking) {
                            ThinkingChainCard(
                                reasoning = cleanReasoning,
                                isThinking = isThinking,
                            )
                        }

                        // 2. 正文回答：采用现代轻量 Markdown 富文本渲染器
                        if (cleanContent.isNotBlank()) {
                            MarkdownText(
                                markdown = cleanContent,
                                color = HealthPalette.textPrimary(),
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                            )
                        } else if (isStreaming && !isThinking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = HealthPalette.Secondary,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "正在组织回答...",
                                    fontSize = 12.sp,
                                    color = HealthPalette.textMuted(),
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        }

                        // 3. 权威临床检索证据卡片
                        if (evidences.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HealthPalette.pastelMint().copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = HealthPalette.Secondary,
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "依据权威临床指南与文献：",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HealthPalette.textPrimary(),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    evidences.forEach { ev ->
                                        Text(
                                            text = "• [${ev.source}] ${ev.title}",
                                            fontSize = 11.sp,
                                            color = HealthPalette.textSecondary(),
                                            lineHeight = 15.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = timeFormat.format(Date(timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

/**
 * 深度思考思维链交互卡片：
 * 具备折叠与展开功能、微动效指示器，并且在字体字号（12.5sp）、字色（柔和次级色）及排版风格上与正文显著区分。
 */
@Composable
private fun ThinkingChainCard(
    reasoning: String,
    isThinking: Boolean,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(isThinking) }

    // 正在思考时保持展开以便用户实时查看思考脉络
    LaunchedEffect(isThinking) {
        if (isThinking) {
            isExpanded = true
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HealthPalette.chipSurface().copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthPalette.chipBorder()),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(HealthPalette.pastelLavender()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = HealthPalette.Secondary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isThinking) "正在进行临床思维推理..." else "已深度思考 · 临床推理链",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    if (isThinking) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            strokeWidth = 1.5.dp,
                            color = HealthPalette.Secondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "收起" else "展开详情",
                        fontSize = 11.sp,
                        color = HealthPalette.textMuted(),
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = HealthPalette.textMuted(),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(
                        color = HealthPalette.chipBorder(),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 左侧微光强调竖线 (强化思维备忘录视觉隐喻)
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(HealthPalette.Secondary.copy(alpha = 0.7f)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 思维链正文字体：与主回答明显区分 (12.5sp, 柔和次要色, 略带备忘录柔和质感)
                        Text(
                            text = reasoning.ifBlank { "正在分析摄入碳水结构与代谢调节机制..." },
                            fontSize = 12.5.sp,
                            lineHeight = 18.5.sp,
                            color = HealthPalette.textSecondary(),
                            fontStyle = FontStyle.Normal,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
