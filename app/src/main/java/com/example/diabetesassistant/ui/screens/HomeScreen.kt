package com.example.diabetesassistant.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.diabetesassistant.data.local.entity.Comment
import com.example.diabetesassistant.data.local.entity.CommunityPost
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.User
import com.example.diabetesassistant.data.repository.CommunityRepository
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class HomeFilterTab {
    RECOMMEND,
    FOLLOWING,
    MALL,
}

private data class SearchHistoryRecord(
    val query: String,
    val summary: String,
    val timeLabel: String,
)



@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val posts: StateFlow<List<CommunityPost>> = communityRepository.observePosts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val users: StateFlow<List<User>> = userRepository.observeUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun setLike(post: CommunityPost, isCurrentlyLoved: Boolean) {
        viewModelScope.launch {
            val nextLikeCount = if (isCurrentlyLoved) {
                (post.likeCount - 1).coerceAtLeast(0)
            } else {
                post.likeCount + 1
            }
            communityRepository.updatePost(post.copy(likeCount = nextLikeCount))
        }
    }
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    foodRecordRepository: FoodRecordRepository,
) : ViewModel() {
    val records: StateFlow<List<FoodRecord>> = foodRecordRepository.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun publish(
        userId: Long,
        title: String,
        content: String,
        postType: String,
        archiveRecordId: Long?,
        imagePaths: List<String>,
        onPosted: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val newId = communityRepository.createPost(
                CommunityPost(
                    userId = userId,
                    title = title.trim(),
                    content = content.trim(),
                    imagePath = imagePaths.joinToString("\n").ifBlank { null },
                    bloodSugarTag = if (postType == "ARCHIVE_SHARE") "档案分享" else null,
                    postType = postType,
                    archiveRecordId = archiveRecordId,
                )
            )
            onPosted(newId)
        }
    }
}

@HiltViewModel
class CommunityPostDetailViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val users: StateFlow<List<User>> = userRepository.observeUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun observePost(postId: Long) = communityRepository.observePostById(postId)

    fun observeComments(postId: Long) = communityRepository.observeComments(postId)

    fun setLike(post: CommunityPost, isCurrentlyLoved: Boolean) {
        viewModelScope.launch {
            val nextLikeCount = if (isCurrentlyLoved) {
                (post.likeCount - 1).coerceAtLeast(0)
            } else {
                post.likeCount + 1
            }
            communityRepository.updatePost(post.copy(likeCount = nextLikeCount))
        }
    }

    fun addComment(userId: Long, postId: Long, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            communityRepository.addComment(
                Comment(
                    content = content.trim(),
                    userId = userId,
                    postId = postId,
                )
            )
            val post = communityRepository.getPostById(postId) ?: return@launch
            communityRepository.updatePost(post.copy(commentCount = post.commentCount + 1))
        }
    }

    fun share(postId: Long) {
        viewModelScope.launch {
            communityRepository.getPostById(postId)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    welcomeTrigger: Int = 0,
    guardianDays: Int = 1,
    onScanClick: () -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    onAiAdviceClick: () -> Unit = {},
    onTodayRecordsClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    followedUserIds: List<String> = emptyList(),
    onAuthorClick: (String) -> Unit = {},
    onFollowClick: (String) -> Unit = {},
    viewModel: HomeFeedViewModel = hiltViewModel(),
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val userNameMap = remember(users) { users.associate { it.id to it.nickname } }
    val listState = rememberLazyListState()
    var tab by rememberSaveable { mutableStateOf(HomeFilterTab.RECOMMEND) }
    var searchMode by rememberSaveable { mutableStateOf(false) }
    var searchDraftText by rememberSaveable { mutableStateOf("") }
    var activeSearchQuery by rememberSaveable { mutableStateOf("") }
    var welcomeManualTrigger by rememberSaveable { mutableIntStateOf(0) }
    var welcomeDismissToken by rememberSaveable { mutableIntStateOf(0) }
    var hasDismissedWelcomeInSession by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    var pullDistance by remember { mutableStateOf(0f) }
    var pullTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(welcomeTrigger) {
        if (welcomeTrigger > welcomeDismissToken) {
            welcomeDismissToken = welcomeTrigger
            hasDismissedWelcomeInSession = false
        }
    }

    val pullDownConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (source == NestedScrollSource.UserInput && isAtTop && available.y > 0f) {
                    pullDistance += available.y
                    if (pullDistance >= 120f && !pullTriggered) {
                        pullTriggered = true
                        hasDismissedWelcomeInSession = false
                        welcomeManualTrigger += 1
                    }
                } else {
                    pullDistance = 0f
                    pullTriggered = false
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                pullDistance = 0f
                pullTriggered = false
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    val searchHistoryRecords = remember(posts) { buildSearchHistoryRecords(posts) }
    var historyRecords by remember { mutableStateOf<List<SearchHistoryRecord>>(emptyList()) }

    var historyInitialized by rememberSaveable { mutableStateOf(false) }

    fun submitSearch(rawQuery: String) {
        val query = rawQuery.trim()
        activeSearchQuery = query
        if (query.isBlank()) return
        historyRecords = listOf(
            SearchHistoryRecord(query = query, summary = "搜索记录", timeLabel = "刚刚"),
        ) + historyRecords.filterNot { it.query.equals(query, ignoreCase = true) }
    }

    LaunchedEffect(searchHistoryRecords) {
        if (!historyInitialized) {
            historyRecords = searchHistoryRecords
            historyInitialized = true
        }
    }

    val filteredPosts = remember(posts, tab, followedUserIds) {
        posts.filter { post ->
            when (tab) {
                HomeFilterTab.RECOMMEND -> post.postType != "MALL"
                HomeFilterTab.FOLLOWING -> {
                    followedUserIds.contains(post.userId.toString()) && post.postType != "MALL"
                }
                HomeFilterTab.MALL -> post.postType == "MALL"
            }
        }
    }

    val searchResultPosts = remember(posts, activeSearchQuery) {
        if (activeSearchQuery.isBlank()) {
            emptyList()
        } else {
            posts.filter {
                it.title.contains(activeSearchQuery, ignoreCase = true) ||
                    it.content.contains(activeSearchQuery, ignoreCase = true)
            }
        }
    }

    val displayPosts = filteredPosts

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullDownConnection),
        ) {
            stickyHeader {
                HomeTopBar(
                    searchMode = searchMode,
                    searchText = searchDraftText,
                    selectedTab = tab,
                    onCreatePostClick = onCreatePostClick,
                    onSearchModeChange = { enabled ->
                        searchMode = enabled
                        if (!enabled) {
                            activeSearchQuery = ""
                        }
                        if (enabled) keyboard?.show() else keyboard?.hide()
                    },
                    onSearchTextChange = { searchDraftText = it },
                    onSearchSubmit = { submitSearch(searchDraftText) },
                    onTabChange = { tab = it },
                )
            }

            item {
                HomeQuickActions(
                    onScanClick = onScanClick,
                    onAiAdviceClick = onAiAdviceClick,
                    onTodayRecordsClick = onTodayRecordsClick,
                )
            }

            item {
                if (searchMode) {
                    SearchHistorySection(
                        records = historyRecords,
                        searchText = searchDraftText,
                        activeQuery = activeSearchQuery,
                        searchResultCount = searchResultPosts.size,
                        onRecordClick = { query ->
                            searchDraftText = query
                            submitSearch(query)
                        },
                        onDeleteRecord = { query -> historyRecords = historyRecords.filterNot { it.query == query } },
                        onClearAll = { historyRecords = emptyList() },
                    )
                    if (activeSearchQuery.isNotBlank()) {
                        HomeFeedGrid(
                            posts = searchResultPosts,
                            onPostClick = onPostClick,
                            onAuthorClick = onAuthorClick,
                            onFollowClick = onFollowClick,
                            followedUserIds = followedUserIds,
                            authorNames = userNameMap,
                            onLoveToggle = { post, isLoved -> viewModel.setLike(post, isLoved) },
                        )
                    }
                } else {
                    HomeFeedGrid(
                        posts = displayPosts,
                        onPostClick = onPostClick,
                        onAuthorClick = onAuthorClick,
                        onFollowClick = onFollowClick,
                        followedUserIds = followedUserIds,
                        authorNames = userNameMap,
                        onLoveToggle = { post, isLoved -> viewModel.setLike(post, isLoved) },
                    )
                }
            }
        }

        WelcomeOverlay(
            trigger = welcomeTrigger + welcomeManualTrigger,
            guardianDays = guardianDays,
            shouldShowInitially = !hasDismissedWelcomeInSession,
            onDismissed = { hasDismissedWelcomeInSession = true },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun HomeTopBar(
    searchMode: Boolean,
    searchText: String,
    selectedTab: HomeFilterTab,
    onCreatePostClick: () -> Unit,
    onSearchModeChange: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onTabChange: (HomeFilterTab) -> Unit,
) {
    val searchWidth by animateDpAsState(targetValue = if (searchMode) 260.dp else 0.dp, label = "searchWidth")

    Surface(shadowElevation = 4.dp, tonalElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (searchMode) {
                IconButton(onClick = { onSearchModeChange(false) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier.width(searchWidth),
                    singleLine = true,
                    placeholder = { Text("搜索帖子") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                )
            } else {
                IconButton(onClick = onCreatePostClick) {
                    Icon(Icons.Default.Add, contentDescription = "发帖")
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    FilterChip(
                        selected = selectedTab == HomeFilterTab.RECOMMEND,
                        onClick = { onTabChange(HomeFilterTab.RECOMMEND) },
                        label = { Text("推荐") },
                    )
                    Box(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedTab == HomeFilterTab.FOLLOWING,
                        onClick = { onTabChange(HomeFilterTab.FOLLOWING) },
                        label = { Text("关注") },
                    )
                    Box(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedTab == HomeFilterTab.MALL,
                        onClick = { onTabChange(HomeFilterTab.MALL) },
                        label = { Text("商城") },
                    )
                }
                IconButton(onClick = { onSearchModeChange(true) }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActions(
    onScanClick: () -> Unit,
    onAiAdviceClick: () -> Unit,
    onTodayRecordsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(onClick = onScanClick, label = { Text("拍照识别") })
        AssistChip(onClick = onAiAdviceClick, label = { Text("AI 建议") })
        AssistChip(onClick = onTodayRecordsClick, label = { Text("今日档案") })
    }
}

@Composable
private fun SearchHistorySection(
    records: List<SearchHistoryRecord>,
    searchText: String,
    activeQuery: String,
    searchResultCount: Int,
    onRecordClick: (String) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }
    val visibleRecords = remember(records, searchText) {
        if (searchText.isBlank()) {
            records
        } else {
            records.filter {
                it.query.contains(searchText, ignoreCase = true) ||
                    it.summary.contains(searchText, ignoreCase = true)
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清空历史记录") },
            text = { Text("确认清空全部搜索历史？") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirm = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (visibleRecords.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text("暂无历史搜索")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (activeQuery.isNotBlank()) {
            Text(
                text = "“$activeQuery” 搜索结果 $searchResultCount 条",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4B9B6E),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "历史记录",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E7E5D),
            )
            TextButton(onClick = { showClearConfirm = true }) {
                Text("清空")
            }
        }
        visibleRecords.forEach { record ->
            Card(
                onClick = { onRecordClick(record.query) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(record.query, style = MaterialTheme.typography.titleSmall)
                        Text(record.summary, style = MaterialTheme.typography.bodySmall, color = Color(0xFF3E5D4F))
                        Text(record.timeLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B9B6E))
                    }
                    IconButton(onClick = { onDeleteRecord(record.query) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除历史")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HomeFeedGrid(
    posts: List<CommunityPost>,
    onPostClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    followedUserIds: List<String>,
    authorNames: Map<Long, String>,
    onLoveToggle: (CommunityPost, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (posts.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text("暂无帖子，点击左上角发帖")
        }
        return
    }

    val leftColumn = remember(posts) { posts.filterIndexed { index, _ -> index % 2 == 0 } }
    val rightColumn = remember(posts) { posts.filterIndexed { index, _ -> index % 2 == 1 } }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth - 24.dp - 10.dp) / 2

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(cardWidth),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            leftColumn.forEach { post ->
                CommunityPostCard(
                    post = post,
                    authorName = authorNames[post.userId] ?: "用户${post.userId}",
                    isFollowingAuthor = followedUserIds.contains(post.userId.toString()),
                    onPostClick = { onPostClick(post.id.toString()) },
                    onAuthorClick = { onAuthorClick(post.userId.toString()) },
                    onFollowClick = { onFollowClick(post.userId.toString()) },
                    onLoveToggle = { isLoved -> onLoveToggle(post, isLoved) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Column(
            modifier = Modifier.width(cardWidth),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rightColumn.forEach { post ->
                CommunityPostCard(
                    post = post,
                    authorName = authorNames[post.userId] ?: "用户${post.userId}",
                    isFollowingAuthor = followedUserIds.contains(post.userId.toString()),
                    onPostClick = { onPostClick(post.id.toString()) },
                    onAuthorClick = { onAuthorClick(post.userId.toString()) },
                    onFollowClick = { onFollowClick(post.userId.toString()) },
                    onLoveToggle = { isLoved -> onLoveToggle(post, isLoved) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    authorName: String,
    isFollowingAuthor: Boolean,
    onPostClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
    onLoveToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLoved by rememberSaveable(post.id) { mutableStateOf(false) }

    Card(
        onClick = onPostClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (post.postType == "MALL") Color(0xFFF8FAF9) else Color.White),
    ) {
        if (post.postType == "MALL") {
            ProductPostCardContent(
                post = post,
                authorName = authorName,
                isFollowingAuthor = isFollowingAuthor,
                onAuthorClick = onAuthorClick,
                onFollowClick = onFollowClick,
                onPostClick = onPostClick,
            )
        } else {
            UgcPostCardContent(
                post = post,
                authorName = authorName,
                isFollowingAuthor = isFollowingAuthor,
                onAuthorClick = onAuthorClick,
                onFollowClick = onFollowClick,
                onPostClick = onPostClick,
                onLoveToggle = onLoveToggle,
                isLoved = isLoved,
                onLovedStateChange = { isLoved = it },
            )
        }
    }
}

@Composable
private fun UgcPostCardContent(
    post: CommunityPost,
    authorName: String,
    isFollowingAuthor: Boolean,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
    onPostClick: () -> Unit,
    onLoveToggle: (Boolean) -> Unit,
    isLoved: Boolean,
    onLovedStateChange: (Boolean) -> Unit,
) {
    val imageEntries = remember(post.imagePath) {
        post.imagePath?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (post.id % 2L == 0L) 176.dp else 212.dp)
                .background(Color(0xFFE8F3EE), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (imageEntries.isEmpty()) "UGC 图片" else "UGC 图片 ${imageEntries.size} 张")
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(post.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                post.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF3E5D4F),
            )
            post.bloodSugarTag?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7E5D),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onAuthorClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                Text(authorName)
            }
            Button(onClick = onFollowClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                Text(if (isFollowingAuthor) "已关注" else "关注")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    onLoveToggle(isLoved)
                    onLovedStateChange(!isLoved)
                }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Love")
                }
                Text(post.likeCount.toString(), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onPostClick) {
                    Icon(Icons.AutoMirrored.Outlined.Comment, contentDescription = "Review")
                }
                Text(post.commentCount.toString(), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onPostClick) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                Text("0", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ProductPostCardContent(
    post: CommunityPost,
    authorName: String,
    isFollowingAuthor: Boolean,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
    onPostClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFEFF5F2), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("商品图")
        }
        Text(post.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
        Text(post.content, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        Text(post.bloodSugarTag ?: "", color = Color(0xFF1A5F3A), style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onAuthorClick) { Text(authorName) }
            TextButton(onClick = onFollowClick) { Text(if (isFollowingAuthor) "已关注店铺" else "关注店铺") }
        }
        Button(onClick = onPostClick, modifier = Modifier.fillMaxWidth()) {
            Text("查看商品详情")
        }
    }
}

@Composable
private fun WelcomeOverlay(
    trigger: Int,
    guardianDays: Int,
    shouldShowInitially: Boolean,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val offsetY = remember { Animatable(0f) }
    var visible by rememberSaveable(trigger) { mutableStateOf(shouldShowInitially) }

    LaunchedEffect(trigger, shouldShowInitially) {
        visible = shouldShowInitially
        if (shouldShowInitially) {
            offsetY.snapTo(0f)
        }
    }

    AnimatedVisibility(visible = visible) {
        Box(
            modifier = modifier
                .offset { IntOffset(x = 0, y = offsetY.value.roundToInt()) }
                .background(Color(0xFFEAF6EF))
                .pointerInput(trigger) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) {
                                scope.launch {
                                    offsetY.snapTo((offsetY.value + dragAmount).coerceAtMost(0f))
                                }
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetY.value < -120f) {
                                    offsetY.animateTo(
                                        targetValue = -screenHeightPx,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                    visible = false
                                    onDismissed()
                                } else {
                                    offsetY.animateTo(0f, spring())
                                }
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "血糖助手已守护您",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2E7E5D),
                )
                Text(
                    text = "$guardianDays 天了",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2E7E5D),
                )
                Text(
                    text = "↑ 上滑探索",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B9B6E),
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }
    }
}

@Composable
fun CreatePostScreen(
    onBackClick: () -> Unit,
    onPosted: (String) -> Unit,
    modifier: Modifier = Modifier,
    signedInUserId: Long? = null,
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var postType by rememberSaveable { mutableStateOf("FREE_TEXT") }
    var archiveRecordId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedImagePaths by rememberSaveable { mutableStateOf(listOf<String>()) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        val persistedUris = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                uri.toString()
            }.getOrNull()
        }
        selectedImagePaths = persistedUris
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("返回") }
                Text("发帖", style = MaterialTheme.typography.headlineSmall)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("正文") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            Text("帖子类型")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = postType == "FREE_TEXT",
                    onClick = { postType = "FREE_TEXT"; archiveRecordId = null },
                    label = { Text("自由图文") },
                )
                FilterChip(
                    selected = postType == "ARCHIVE_SHARE",
                    onClick = { postType = "ARCHIVE_SHARE" },
                    label = { Text("档案分享") },
                )
                FilterChip(
                    selected = postType == "MALL",
                    onClick = { postType = "MALL"; archiveRecordId = null },
                    label = { Text("商城") },
                )
            }

            Button(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }) {
                Text(if (selectedImagePaths.isEmpty()) "上传图片（可多选）" else "已选 ${selectedImagePaths.size} 张，重新选择")
            }
            if (selectedImagePaths.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImagePaths) { path ->
                        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF6EF))) {
                            Text(
                                text = path.substringAfterLast('/'),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (postType == "ARCHIVE_SHARE") {
                Text("关联档案")
                records.take(5).forEach { record ->
                    FilterChip(
                        selected = archiveRecordId == record.id,
                        onClick = { archiveRecordId = record.id },
                        label = { Text("${record.dishName} #${record.id}") },
                    )
                }
            }

            Button(
                onClick = {
                    val userId = signedInUserId ?: 1L
                    viewModel.publish(
                        userId = userId,
                        title = title,
                        content = content,
                        postType = postType,
                        archiveRecordId = archiveRecordId,
                        imagePaths = selectedImagePaths,
                    ) { newPostId ->
                        onPosted(newPostId.toString())
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("发布")
            }
        }
    }
}

@Composable
fun CommunityPostDetailScreen(
    onBackClick: () -> Unit,
    postId: String,
    modifier: Modifier = Modifier,
    followedUserIds: List<String> = emptyList(),
    onAuthorClick: (String) -> Unit = {},
    onFollowClick: (String) -> Unit = {},
    signedInUserId: Long? = null,
    viewModel: CommunityPostDetailViewModel = hiltViewModel(),
) {
    val postIdLong = postId.toLongOrNull()
    if (postIdLong == null) {
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("帖子不存在")
                Button(onClick = onBackClick) { Text("返回") }
            }
        }
        return
    }

    val post by viewModel.observePost(postIdLong).collectAsStateWithLifecycle(initialValue = null)
    val comments by viewModel.observeComments(postIdLong).collectAsStateWithLifecycle(initialValue = emptyList())
    val users by viewModel.users.collectAsStateWithLifecycle()
    val userNameMap = remember(users) { users.associate { it.id to it.nickname } }
    val userId = signedInUserId ?: 1L
    var isLoved by rememberSaveable(postIdLong) { mutableStateOf(false) }
    var shareCount by rememberSaveable(postIdLong) { mutableIntStateOf(0) }
    var commentInput by rememberSaveable { mutableStateOf("") }

    if (post == null) {
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onBackClick) { Text("返回") }
                    Text("详情", style = MaterialTheme.typography.headlineSmall)
                }
                Text("帖子加载中或已删除")
            }
        }
        return
    }

    val authorName = userNameMap[post!!.userId] ?: "用户${post!!.userId}"
    val detailTime = remember(post!!.createdAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(post!!.createdAt))
    }
    val detailImageEntries = remember(post!!.imagePath) {
        post!!.imagePath?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }

    val displayComments = remember(comments, post!!.commentCount) {
        val targetCount = post!!.commentCount.coerceAtLeast(0)
        val base = comments.take(targetCount)
        if (base.size >= targetCount) {
            base
        } else {
            val needed = targetCount - base.size
            val filler = (1..needed).map { index ->
                Comment(
                    content = "很有帮助，已收藏（示例评论 $index）",
                    userId = ((index % 6) + 11).toLong(),
                    postId = postIdLong,
                )
            }
            (base + filler).take(targetCount)
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onBackClick) { Text("返回") }
                    Text(
                        if (post!!.postType == "MALL") "商品详情" else "帖子详情",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            item {
                if (post!!.postType == "MALL") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(Color(0xFFEFF5F2), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (detailImageEntries.isEmpty()) "商品详情图" else "商品详情图 ${detailImageEntries.size} 张",
                                )
                            }
                            Text(post!!.title, style = MaterialTheme.typography.titleLarge)
                            Text(post!!.bloodSugarTag ?: "", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF1A5F3A))
                            Text(post!!.content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3E5D4F))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("店铺：$authorName", style = MaterialTheme.typography.titleSmall)
                                    Text("发货：当日/次日", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B9B6E))
                                }
                                TextButton(onClick = { onFollowClick(post!!.userId.toString()) }) {
                                    val isFollowing = followedUserIds.contains(post!!.userId.toString())
                                    Text(if (isFollowing) "已关注" else "关注店铺")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.weight(1f),
                                ) { Text("加入购物车") }
                                Button(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.weight(1f),
                                ) { Text("立即购买") }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFE8F3EE), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (detailImageEntries.isEmpty()) "帖子详情图" else "帖子详情图 ${detailImageEntries.size} 张",
                        )
                    }
                    if (detailImageEntries.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(detailImageEntries) { path ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = path.substringAfterLast('/'),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    Text(post!!.title, style = MaterialTheme.typography.titleLarge)
                    Text("作者：$authorName", style = MaterialTheme.typography.bodyMedium)
                    Text("发布时间：$detailTime", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B9B6E))
                    Text(post!!.content)
                    post!!.bloodSugarTag?.let { tag ->
                        Text(tag, color = Color(0xFF2E7E5D))
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onAuthorClick(post!!.userId.toString()) }) { Text(authorName) }
                    Button(onClick = { onFollowClick(post!!.userId.toString()) }) {
                        val isFollowing = followedUserIds.contains(post!!.userId.toString())
                        Text(if (isFollowing) "已关注" else "关注")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            viewModel.setLike(post!!, isLoved)
                            isLoved = !isLoved
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                        }
                        Text(post!!.likeCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Outlined.Comment, contentDescription = null)
                        }
                        Text(post!!.commentCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            viewModel.share(postIdLong)
                            shareCount += 1
                        }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                        Text(shareCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            item { Text("评论区", style = MaterialTheme.typography.titleMedium) }

            item {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入评论") },
                )
                Button(
                    onClick = {
                        viewModel.addComment(userId, postIdLong, commentInput)
                        commentInput = ""
                    },
                    enabled = commentInput.isNotBlank(),
                ) {
                    Text("发送")
                }
            }

            items(displayComments) { comment ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            userNameMap[comment.userId] ?: "用户${comment.userId}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(comment.content)
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityUserProfileScreen(
    onBackClick: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier,
    followedUserIds: List<String> = emptyList(),
    onFollowClick: (String) -> Unit = {},
    onPostClick: (String) -> Unit = {},
    feedViewModel: HomeFeedViewModel = hiltViewModel(),
) {
    val posts by feedViewModel.posts.collectAsStateWithLifecycle()
    val userPostId = remember(posts, userId) {
        posts.firstOrNull { it.userId.toString() == userId }?.id?.toString()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text("用户主页", style = MaterialTheme.typography.headlineSmall)
            Text("User ID: $userId")
            Text("Followed users: ${followedUserIds.size}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("返回") }
                Button(onClick = { onFollowClick(userId) }) { Text("关注") }
                Button(
                    onClick = { userPostId?.let(onPostClick) },
                    enabled = userPostId != null,
                ) { Text("打开帖子") }
            }
        }
    }
}

private fun buildSearchHistoryRecords(posts: List<CommunityPost>): List<SearchHistoryRecord> {
    val fromPosts = posts
        .asReversed()
        .take(10)
        .map {
            SearchHistoryRecord(
                query = it.title,
                summary = it.content,
                timeLabel = "最近浏览",
            )
        }
    return if (fromPosts.isNotEmpty()) fromPosts else DEFAULT_SEARCH_HISTORY_RECORDS
}

private val DEFAULT_SEARCH_HISTORY_RECORDS = listOf(
    SearchHistoryRecord("低GI早餐", "燕麦、鸡蛋、无糖酸奶搭配方案", "今天 08:12"),
    SearchHistoryRecord("控糖晚餐", "清蒸鱼+杂粮饭+凉拌蔬菜", "昨天 19:43"),
    SearchHistoryRecord("高蛋白加餐", "原味坚果与无糖豆浆", "昨天 16:20"),
    SearchHistoryRecord("胰岛素注射时间", "餐前注射与峰值对齐建议", "周一 12:05"),
    SearchHistoryRecord("外卖怎么选", "优先蒸煮、避免糖醋和油炸", "周一 09:36"),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(welcomeTrigger = 1, guardianDays = 7)
}

@Preview(showBackground = true)
@Composable
private fun SearchHistoryEmptyPreview() {
    SearchHistorySection(
        records = emptyList(),
        searchText = "",
        activeQuery = "",
        searchResultCount = 0,
        onRecordClick = {},
        onDeleteRecord = {},
        onClearAll = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchHistoryWithDataPreview() {
    SearchHistorySection(
        records = DEFAULT_SEARCH_HISTORY_RECORDS,
        searchText = "",
        activeQuery = "低GI早餐",
        searchResultCount = 12,
        onRecordClick = {},
        onDeleteRecord = {},
        onClearAll = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CreatePostScreenPreview() {
    Surface {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CreatePostScreen Preview")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityPostDetailScreenPreview() {
    Surface {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CommunityPostDetailScreen Preview")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityUserProfileScreenPreview() {
    CommunityUserProfileScreen(onBackClick = {}, userId = "1")
}
