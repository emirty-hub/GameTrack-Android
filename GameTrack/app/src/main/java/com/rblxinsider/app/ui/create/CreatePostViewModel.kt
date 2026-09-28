package com.rblxinsider.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class CreatePostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val state: StateFlow<CreatePostState> = _state

    val availableTags = listOf("Glitch", "Exploit", "Hidden", "Update", "Tip", "Discussion")

    fun submitPost(
        title: String,
        description: String,
        selectedTags: List<String>
    ) {
        val user = auth.currentUser ?: return
        if (title.isBlank() || description.isBlank()) {
            _state.value = CreatePostState.Error("Baslik ve aciklama zorunlu")
            return
        }

        viewModelScope.launch {
            _state.value = CreatePostState.Loading
            try {
                val post = hashMapOf(
                    "id" to UUID.randomUUID().toString(),
                    "authorId" to user.uid,
                    "authorName" to (user.displayName ?: "kullanici"),
                    "authorAvatar" to user.photoUrl?.toString(),
                    "title" to title,
                    "description" to description,
                    "tags" to selectedTags,
                    "imageUrl" to null,
                    "likeCount" to 0,
                    "commentCount" to 0,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                db.collection("posts").add(post).await()
                _state.value = CreatePostState.Success
            } catch (e: Exception) {
                _state.value = CreatePostState.Error(e.message ?: "Hata olustu")
            }
        }
    }

    fun resetState() {
        _state.value = CreatePostState.Idle
    }
}