package com.rblxinsider.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rblxinsider.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var editUsername by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is ProfileState.Success && editUsername.isEmpty()) {
            editUsername = (state as ProfileState.Success).user.username
            editBio = (state as ProfileState.Success).user.bio
        }
    }

    when (val s = state) {
        is ProfileState.Loading -> {
            Box(Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
        }
        is ProfileState.Error -> {
            Box(Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
                Text(s.message, color = Color.Red)
            }
        }
        is ProfileState.Success -> {
            if (isEditing) {
                Column(
                    modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profili Duzenle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = { viewModel.toggleEditing() }) {
                            Text("Iptal", color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Kullanici Adi", color = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = DarkSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentPurple
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = TextHint) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = DarkSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentPurple
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.updateProfile(editUsername, editBio) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Kaydet", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().background(DarkBg)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(130.dp).background(
                                    Brush.verticalGradient(colors = listOf(AccentPurpleDark, DarkBg))
                                )
                            )
                            Box(
                                modifier = Modifier.size(80.dp).align(Alignment.BottomStart)
                                    .padding(start = 16.dp).clip(CircleShape)
                                    .border(3.dp, AccentPurple, CircleShape).background(DarkSurface)
                            ) {
                                if (s.user.avatarUrl != null) {
                                    AsyncImage(
                                        model = s.user.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(s.user.username.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { editUsername = s.user.username; editBio = s.user.bio; viewModel.toggleEditing() },
                                    shape = RoundedCornerShape(20.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(AccentPurple, AccentPurple)))
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Duzenle", color = AccentPurple, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.signOut(); onSignOut() },
                                    shape = RoundedCornerShape(20.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.Red, Color.Red)))
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cikis", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(s.user.username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (s.user.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(s.user.bio, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(label = "Gonderi", value = s.posts.size.toString(), modifier = Modifier.weight(1f))
                            StatCard(label = "Beğeni", value = s.user.reputation.toString(), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Gonderiler", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (s.posts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Henuz gonderi yok", color = TextHint, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(s.posts) { post ->
                            ProfilePostCard(post = post, onClick = { onPostClick(post.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(DarkSurface).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun ProfilePostCard(post: ProfilePost, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(post.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.tags.take(3).forEach { tag ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(DarkBg).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(tag, fontSize = 10.sp, color = AccentPurple)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                Text("▲ ${post.likeCount}", fontSize = 11.sp, color = AccentPurple)
                Spacer(modifier = Modifier.width(12.dp))
                Text("💬 ${post.commentCount}", fontSize = 11.sp, color = TextHint)
            }
        }
    }
}