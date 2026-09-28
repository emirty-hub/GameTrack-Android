package com.rblxinsider.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val postCount: Int = 0,
    val reputation: Int = 0
)

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: UserProfile, val posts: List<ProfilePost>) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

data class ProfilePost(
    val id: String = "",
    val title: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val tags: List<String> = emptyList()
)

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid).get().await()
                val profile = UserProfile(
                    uid = user.uid,
                    username = doc.getString("username") ?: user.displayName ?: "kullanici",
                    avatarUrl = doc.getString("avatarUrl") ?: user.photoUrl?.toString(),
                    bio = doc.getString("bio") ?: "",
                    postCount = (doc.getLong("postCount") ?: 0).toInt(),
                    reputation = (doc.getLong("reputation") ?: 0).toInt()
                )

                val postsSnapshot = db.collection("posts")
                    .whereEqualTo("authorId", user.uid)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get().await()

                val posts = postsSnapshot.documents.map { p ->
                    ProfilePost(
                        id = p.id,
                        title = p.getString("title") ?: "",
                        likeCount = (p.getLong("likeCount") ?: 0).toInt(),
                        commentCount = (p.getLong("commentCount") ?: 0).toInt(),
                        tags = (p.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                }

                _state.value = ProfileState.Success(profile, posts)
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Hata")
            }
        }
    }

    fun updateProfile(username: String, bio: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.uid).update(
                    mapOf("username" to username, "bio" to bio)
                ).await()
                loadProfile()
                _isEditing.value = false
            } catch (e: Exception) { }
        }
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    fun signOut() {
        auth.signOut()
    }
}