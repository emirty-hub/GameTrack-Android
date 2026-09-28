package com.rblxinsider.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rblxinsider.app.data.model.Post
import com.rblxinsider.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPostClick: (String) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val tags = listOf("Tumu", "Glitch", "Exploit", "Hidden", "Update", "Tip", "Discussion")

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RblxInsider",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentPurple
            )
        }

        // Arama çubuğu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (searchQuery.isEmpty()) {
                Text("Ara...", color = TextHint, fontSize = 13.sp)
            }
            BasicTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                cursorBrush = SolidColor(AccentPurple),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Tag filtreleri
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags) { tag ->
                val isSelected = selectedTag == tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentPurple else DarkSurface)
                        .clickable { viewModel.setTag(tag) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        // Post listesi
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadPosts() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isLoading && posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gonderi bulunamadi",
                        color = TextHint,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentPurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(2).uppercase(),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = post.authorName, fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TagSecretBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(text = post.gameName, fontSize = 9.sp, color = AccentPurple)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Taglar
            if (post.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(tag, fontSize = 9.sp, color = AccentPurple)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "▲ ${post.upvotes}", fontSize = 11.sp, color = AccentPurple)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "💬 ${post.commentCount}", fontSize = 11.sp, color = TextHint)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = post.createdAt?.toDate()?.let {
                        val diff = (System.currentTimeMillis() - it.time) / 1000
                        when {
                            diff < 3600 -> "${diff / 60} dk"
                            diff < 86400 -> "${diff / 3600} sa"
                            else -> "${diff / 86400} gun"
                        }
                    } ?: "",
                    fontSize = 10.sp,
                    color = TextHint
                )
            }
        }
    }
}

@Composable
fun CategoryTag(category: String) {
    val (bg, fg, label) = when (category) {
        "secret" -> Triple(TagSecretBg, TagSecret, "Gizli Yol")
        "trick" -> Triple(TagTrickBg, TagTrick, "Taktik")
        "glitch" -> Triple(TagGlitchBg, TagGlitch, "Glitch")
        else -> Triple(DarkCard, TextHint, category)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 9.sp, color = fg)
    }
}