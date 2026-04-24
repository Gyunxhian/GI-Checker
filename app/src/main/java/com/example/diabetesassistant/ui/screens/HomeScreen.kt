package com.example.diabetesassistant.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground
import com.example.diabetesassistant.ui.theme.DiabetesAssistantTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private val PrimaryGreen = Color(0xFF008A3D)
private val BrightGreen = Color(0xFF78FF9D)
private val Ink = Color(0xFF17251E)
private val MutedInk = Color(0xFF6B7770)
private val TrackGray = Color(0xFFE5EBE7)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit = {},
    onAiAdviceClick: () -> Unit = {},
    onTodayRecordsClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    followedUserIds: List<String> = emptyList(),
    onAuthorClick: (String) -> Unit = {},
    onFollowClick: (String) -> Unit = {},
) {
    var selectedFeed by remember { mutableStateOf("推荐") }
    val visiblePosts = when (selectedFeed) {
        "关注" -> communityPosts.filter { followedUserIds.contains(it.authorId) }
        "最新" -> communityPosts.sortedByDescending { it.id }
        else -> communityPosts
    }

    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 14.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CommunitySearchBar()
            CommunityFilterTabs(selectedFeed = selectedFeed, onSelect = { selectedFeed = it })
            if (visiblePosts.isEmpty()) {
                EmptyFollowingCard()
            } else {
                CommunityWaterfall(
                    posts = visiblePosts,
                    followedUserIds = followedUserIds,
                    onPostClick = onPostClick,
                    onAuthorClick = onAuthorClick,
                    onFollowClick = onFollowClick,
                )
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
) {
    val post = communityPosts.find { it.id == postId } ?: communityPosts.first()
    val user = communityUsers.first { it.id == post.authorId }
    val isFollowing = followedUserIds.contains(user.id)
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(onClick = onBackClick, shape = CircleShape, color = Color.Transparent) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(22.dp),
                    )
                }
                Text(
                    text = "帖子详情",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = PrimaryGreen)
            }

            PostVisual(
                imageUrl = post.imageUrl,
                color = post.color,
                height = 250,
                title = post.mealName,
                modifier = Modifier.fillMaxWidth(),
            )

            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CommunityAuthorRow(
                        post = post,
                        onClick = { onAuthorClick(user.id) },
                    )
                    FollowButton(
                        followed = isFollowing,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFollowClick(user.id) },
                    )
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = post.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF405149),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusChip(post.glucoseTag, PrimaryGreen)
                        StatusChip(post.postType, Color(0xFF3E8BFF))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SocialMetric(Icons.Filled.Favorite, post.likes)
                        SocialMetric(Icons.Filled.ChatBubble, post.comments)
                        SocialMetric(Icons.Filled.Bookmark, post.saves)
                    }
                }
            }

            Text(
                text = "评论",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp),
            )
            CommentCard("李楠", "这个搭配看起来很稳，我也准备把米饭换成半份。")
            CommentCard("糖友小周", "餐后散步 20 分钟真的有帮助，感谢分享。")
        }
    }
}

@Composable
private fun CommunitySearchBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.74f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MutedInk, modifier = Modifier.size(21.dp))
            Text(
                text = "搜索餐食经验、控糖记录",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(Icons.Filled.Tune, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun CommunityFilterTabs(
    selectedFeed: String,
    onSelect: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("推荐", "最新", "关注").forEach { title ->
            Surface(
                onClick = { onSelect(title) },
                shape = RoundedCornerShape(50),
                color = if (selectedFeed == title) BrightGreen else Color.White.copy(alpha = 0.62f),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedFeed == title) PrimaryGreen else MutedInk,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun CommunityWaterfall(
    posts: List<CommunityPost>,
    followedUserIds: List<String>,
    onPostClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            posts.filterIndexed { index, _ -> index % 2 == 0 }.forEach { post ->
                CommunityPostCard(
                    post = post,
                    followed = followedUserIds.contains(post.authorId),
                    onClick = { onPostClick(post.id) },
                    onAuthorClick = { onAuthorClick(post.authorId) },
                    onFollowClick = { onFollowClick(post.authorId) },
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            posts.filterIndexed { index, _ -> index % 2 == 1 }.forEach { post ->
                CommunityPostCard(
                    post = post,
                    followed = followedUserIds.contains(post.authorId),
                    onClick = { onPostClick(post.id) },
                    onAuthorClick = { onAuthorClick(post.authorId) },
                    onFollowClick = { onFollowClick(post.authorId) },
                )
            }
        }
    }
}

@Composable
private fun EmptyFollowingCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.70f),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = "还没有关注内容",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "关注其他用户后，他们的新帖子会出现在这里。",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    followed: Boolean,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.72f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PostVisual(
                imageUrl = post.imageUrl,
                color = post.color,
                height = post.imageHeight,
                title = post.mealName,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold,
                )
                StatusChip(post.glucoseTag, PrimaryGreen)
                CommunityAuthorRow(post = post, compact = true, onClick = onAuthorClick)
                FollowButton(followed = followed, compact = true, onClick = onFollowClick)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SocialMetric(Icons.Filled.Favorite, post.likes)
                    SocialMetric(Icons.Filled.ChatBubble, post.comments)
                }
            }
        }
    }
}

@Composable
private fun PostVisual(
    imageUrl: String,
    color: Color,
    height: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    ) {
        RemoteImage(
            url = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            fallbackColor = color,
        )
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun CommunityAuthorRow(
    post: CommunityPost,
    compact: Boolean = false,
    onClick: () -> Unit = {},
) {
    val user = communityUsers.first { it.id == post.authorId }
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 24.dp else 34.dp)
                .clip(CircleShape)
                .background(BrightGreen.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            RemoteImage(
                url = user.avatarUrl,
                contentDescription = user.name,
                modifier = Modifier.fillMaxSize(),
                fallbackColor = BrightGreen.copy(alpha = 0.55f),
            )
        }
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
            )
            if (!compact) {
                Text(
                    text = post.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun SocialMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MutedInk, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CommentCard(author: String, body: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White.copy(alpha = 0.68f),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFFE6F8EC), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(author.take(1), color = PrimaryGreen, fontWeight = FontWeight.ExtraBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(author, style = MaterialTheme.typography.labelLarge, color = Ink, fontWeight = FontWeight.ExtraBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MutedInk, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun CommunityUserProfileScreen(
    onBackClick: () -> Unit,
    userId: String,
    followedUserIds: List<String>,
    onFollowClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = communityUsers.find { it.id == userId } ?: communityUsers.first()
    val posts = communityPosts.filter { it.authorId == user.id }
    val isFollowing = followedUserIds.contains(user.id)

    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(onClick = onBackClick, shape = CircleShape, color = Color.Transparent) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(22.dp),
                    )
                }
                Text(
                    text = "用户主页",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.width(28.dp))
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BrightGreen.copy(alpha = 0.45f)),
                        ) {
                            RemoteImage(
                                url = user.avatarUrl,
                                contentDescription = user.name,
                                modifier = Modifier.fillMaxSize(),
                                fallbackColor = BrightGreen.copy(alpha = 0.45f),
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(user.name, style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.ExtraBold)
                            Text(user.bio, style = MaterialTheme.typography.bodySmall, color = MutedInk, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileStat("帖子", posts.size.toString(), Modifier.weight(1f))
                        ProfileStat("关注者", user.followers, Modifier.weight(1f))
                        ProfileStat("控糖天数", user.days, Modifier.weight(1f))
                    }
                    FollowButton(
                        followed = isFollowing,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onFollowClick(user.id) },
                    )
                }
            }

            Text(
                text = "${user.name} 的帖子",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp),
            )
            posts.forEach { post ->
                UserPostListItem(post = post, onClick = { onPostClick(post.id) })
            }
        }
    }
}

@Composable
private fun UserPostListItem(
    post: CommunityPost,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White.copy(alpha = 0.70f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PostVisual(
                imageUrl = post.imageUrl,
                color = post.color,
                height = 84,
                title = post.mealName,
                modifier = Modifier.size(width = 92.dp, height = 84.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(post.title, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.ExtraBold)
                StatusChip(post.glucoseTag, PrimaryGreen)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialMetric(Icons.Filled.Favorite, post.likes)
                    SocialMetric(Icons.Filled.ChatBubble, post.comments)
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEAF8EF),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = PrimaryGreen, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FollowButton(
    followed: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (followed) Color(0xFFEAF8EF) else PrimaryGreen,
    ) {
        Text(
            text = if (followed) "已关注" else "关注",
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 18.dp,
                vertical = if (compact) 6.dp else 10.dp,
            ),
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            color = if (followed) PrimaryGreen else Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackColor: Color = TrackGray,
) {
    var image by androidx.compose.runtime.remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        image = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    val loadedImage = image
    if (loadedImage != null) {
        Image(
            bitmap = loadedImage,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(modifier = modifier.background(fallbackColor), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.LocalDining,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun MealAnalysisCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "最近餐食糖负荷",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    color = Color(0xFFD9FFE6).copy(alpha = 0.82f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = "已生成分析",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF066230),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            CarbGauge(
                grams = 45,
                goalGrams = 60,
                progress = 45f / 60f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp),
            )
        }
    }
}

@Composable
private fun CarbGauge(
    grams: Int,
    goalGrams: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                val horizontalInset = 26.dp.toPx()
                val arcWidth = (size.width - horizontalInset * 2).coerceAtLeast(0f)
                val arcHeight = arcWidth / 2f
                val top = (size.height - arcHeight - strokeWidth / 2f).coerceAtLeast(0f)
                val topLeft = Offset(horizontalInset, top)
                val arcSize = Size(arcWidth, arcWidth)

                drawArc(
                    color = TrackGray,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    color = PrimaryGreen,
                    startAngle = 180f,
                    sweepAngle = 180f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = grams.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "g",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 5.dp),
                    )
                }
                Text(
                    text = "建议上限 ${goalGrams}g · 仍可摄入 ${(goalGrams - grams).coerceAtLeast(0)}g",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedInk,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    ) { measurables, constraints ->
        val canvas = measurables[0].measure(constraints)
        val label = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val labelX = (width - label.width) / 2
        val labelY = (height * 0.48f).toInt().coerceAtMost(height - label.height)

        layout(width, height) {
            canvas.place(0, 0)
            label.place(labelX, labelY)
        }
    }
}

@Composable
private fun AiSuggestionCard(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalDining,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "运动与药量建议",
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                )
            }
            SuggestionLine("根据午餐识别结果，建议餐后步行 30 分钟。")
            SuggestionLine("估算可吸收糖分 45g，药量建议请结合医嘱确认。")
        }
    }
}

@Composable
private fun SuggestionLine(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .background(PrimaryGreen, CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF36463D),
            fontWeight = FontWeight.SemiBold,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
        )
    }
}

@Composable
private fun ScanMealButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = PrimaryGreen.copy(alpha = 0.20f),
                spotColor = PrimaryGreen.copy(alpha = 0.20f),
            ),
        shape = RoundedCornerShape(28.dp),
        color = PrimaryGreen,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "拍照分析本餐",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "智能识别分析",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.76f),
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp),
                )
            }
        }
    }
}

@Composable
private fun TodayRecordsCard(onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "今日分析",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp),
        )
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                RecordRow(
                    completed = true,
                    title = "早餐",
                    description = "燕麦牛奶 · 建议步行 15 分钟",
                    time = "08:30",
                )
                RecordRow(
                    completed = true,
                    title = "午餐",
                    description = "米饭鸡胸肉 · 估算糖负荷 45g",
                    time = "12:45",
                )
                RecordRow(
                    completed = false,
                    title = "晚餐",
                    description = "待拍照分析",
                    time = "--:--",
                )
            }
        }
    }
}

@Composable
private fun RecordRow(
    completed: Boolean,
    title: String,
    description: String,
    time: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(if (completed) BrightGreen else TrackGray, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (completed) Icons.Filled.Check else Icons.Filled.Circle,
                contentDescription = null,
                tint = if (completed) PrimaryGreen else TrackGray,
                modifier = Modifier.size(if (completed) 14.dp else 8.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Ink,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (completed) Color(0xFF405149) else MutedInk,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MutedInk,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}

private data class CommunityPost(
    val id: String,
    val authorId: String,
    val author: String,
    val time: String,
    val title: String,
    val body: String,
    val mealName: String,
    val imageUrl: String,
    val glucoseTag: String,
    val postType: String,
    val likes: String,
    val comments: String,
    val saves: String,
    val imageHeight: Int,
    val color: Color,
)

private data class CommunityUser(
    val id: String,
    val name: String,
    val bio: String,
    val avatarUrl: String,
    val followers: String,
    val days: String,
)

private val communityUsers = listOf(
    CommunityUser(
        id = "user-zhang",
        name = "张晓明",
        bio = "记录中式正餐控糖经验，偏爱餐后步行。",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80",
        followers = "1.2k",
        days = "186",
    ),
    CommunityUser(
        id = "user-lin",
        name = "林医生的餐盘",
        bio = "分享低 GI 搭配和温和控糖知识。",
        avatarUrl = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&w=240&q=80",
        followers = "8.4k",
        days = "420",
    ),
    CommunityUser(
        id = "user-zhou",
        name = "糖友小周",
        bio = "外食控糖实践派，正在减少精制主食。",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80",
        followers = "726",
        days = "92",
    ),
    CommunityUser(
        id = "user-light",
        name = "轻食记录者",
        bio = "关注外卖、沙拉和轻食里的隐藏糖。",
        avatarUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=240&q=80",
        followers = "542",
        days = "138",
    ),
)

private val communityPosts = listOf(
    CommunityPost(
        id = "post-1",
        authorId = "user-zhang",
        author = "张晓明",
        time = "今天 12:45",
        title = "半份糙米饭 + 鸡胸肉，餐后曲线更平稳",
        body = "午餐把白米饭换成半份糙米饭，搭配烤鸡胸肉和西兰花。系统估算可吸收糖约 45g，餐后步行 30 分钟，整体感觉比较轻松。",
        mealName = "糙米鸡胸餐",
        imageUrl = "https://source.unsplash.com/900x700/?brown-rice,chicken,broccoli,bowl",
        glucoseTag = "餐后较平稳",
        postType = "档案分享",
        likes = "128",
        comments = "24",
        saves = "36",
        imageHeight = 168,
        color = Color(0xFF2F6F4E),
    ),
    CommunityPost(
        id = "post-2",
        authorId = "user-lin",
        author = "林医生的餐盘",
        time = "今天 09:18",
        title = "早餐燕麦怎么搭配更耐饿",
        body = "燕麦 40g 加无糖酸奶和一小把坚果，比单独喝粥更有饱腹感。注意水果不要一次放太多，可以先从半份开始观察。",
        mealName = "燕麦酸奶碗",
        imageUrl = "https://source.unsplash.com/900x900/?oatmeal,yogurt,nuts,bowl",
        glucoseTag = "低 GI",
        postType = "自由图文",
        likes = "96",
        comments = "18",
        saves = "41",
        imageHeight = 212,
        color = Color(0xFF5D7F44),
    ),
    CommunityPost(
        id = "post-3",
        authorId = "user-zhou",
        author = "糖友小周",
        time = "昨天 18:32",
        title = "红烧菜少汁会舒服很多",
        body = "今天红烧肉只吃瘦肉部分，酱汁没有拌饭。系统提醒糖负荷中等，晚饭主食减少到半碗，餐后没有明显犯困。",
        mealName = "少汁红烧肉",
        imageUrl = "https://source.unsplash.com/900x900/?braised-pork,chinese-food,rice",
        glucoseTag = "需复核药量",
        postType = "档案分享",
        likes = "84",
        comments = "12",
        saves = "19",
        imageHeight = 228,
        color = Color(0xFF8A5943),
    ),
    CommunityPost(
        id = "post-4",
        authorId = "user-light",
        author = "轻食记录者",
        time = "昨天 13:06",
        title = "外卖沙拉也要留意隐藏糖",
        body = "酱汁单独放，玉米和南瓜都减半，蛋白质选鸡蛋和虾仁。整体糖负荷低一些，但饱腹感还不错。",
        mealName = "虾仁鸡蛋沙拉",
        imageUrl = "https://source.unsplash.com/900x700/?shrimp,egg,salad,bowl",
        glucoseTag = "清淡搭配",
        postType = "自由图文",
        likes = "67",
        comments = "9",
        saves = "22",
        imageHeight = 178,
        color = Color(0xFF3F8A6A),
    ),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DiabetesAssistantTheme {
        HomeScreen()
    }
}
