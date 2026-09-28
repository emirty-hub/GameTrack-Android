package com.rblxinsider.app.ui.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.rblxinsider.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Gonderiyi Sikayet Et", color = TextPrimary) },
            text = { Text("Bu gonderiyi uygunsuz icerik olarak bildirmek istiyor musun?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reportPost(postId)
                    showReportDialog = false
                }) { Text("Bildir", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gonderi", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = DarkSurface
                        ) {
                            if (state is PostDetailState.Success &&
                                (state as PostDetailState.Success).post.authorId == currentUser?.uid) {
                                DropdownMenuItem(
                                    text = { Text("Sil", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.deletePost(postId) { onBack() }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Sikayet Et", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        when (val s = state) {
            is PostDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            }
            is PostDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = Color.Red)
                }
            }
            is PostDetailState.Success -> {
                val isOwner = s.post.authorId == currentUser?.uid

                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(s.post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("@${s.post.authorName}", fontSize = 12.sp, color = TextHint)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(s.post.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (s.post.tags.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    s.post.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(DarkSurface)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(tag, fontSize = 11.sp, color = AccentPurple)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Upvote + Kaydet butonları
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.toggleUpvote(postId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (s.hasUpvoted) AccentPurple else DarkSurface
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "▲  ${s.post.upvotes}",
                                        color = if (s.hasUpvoted) Color.White else AccentPurple,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleSave(postId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSaved) AccentPurpleDark else DarkSurface
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = if (isSaved) "☆ Kaydedildi" else "☆ Kaydet",
                                        color = if (isSaved) Color.White else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = DarkSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Yorumlar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }

                        if (s.comments.isEmpty()) {
                            item {
                                Text("Henuz yorum yok", fontSize = 13.sp, color = TextHint)
                            }
                        } else {
                            items(s.comments) { comment ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurface)
                                        .padding(12.dp)
                                ) {
                                    Text("@${comment.authorName}", fontSize = 12.sp, color = AccentPurple, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comment.content, fontSize = 13.sp, color = TextSecondary)

                                    if (isOwner) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(
                                            onClick = { replyingTo = comment },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Cevap ver", fontSize = 11.sp, color = AccentPurple)
                                        }
                                    }

                                    comment.replies.forEach { reply ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkBg)
                                                .padding(10.dp)
                                        ) {
                                            Text("↳ @${reply.authorName}", fontSize = 11.sp, color = AccentPurple)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(reply.content, fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "@${replyingTo!!.authorName} yorumuna cevap veriyorsun",
                                fontSize = 11.sp,
                                color = TextHint,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { replyingTo = null }) {
                                Text("Iptal", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }

                    if (!isOwner) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Yorum yaz...", color = TextHint) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPurple,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = AccentPurple
                                ),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.addComment(postId, commentText)
                                    commentText = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Gonder", color = Color.White)
                            }
                        }
                    } else if (replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Cevabini yaz...", color = TextHint) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPurple,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = AccentPurple
                                ),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.addReply(postId, replyingTo!!.id, commentText)
                                    commentText = ""
                                    replyingTo = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Gonder", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}