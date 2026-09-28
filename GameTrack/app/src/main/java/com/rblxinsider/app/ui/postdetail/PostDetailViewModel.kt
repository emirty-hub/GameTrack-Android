package com.rblxinsider.app.ui.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Reply(
    val id: String = "",
    val authorName: String = "",
    val content: String = ""
)

data class Comment(
    val id: String = "",
    val authorName: String = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val replies: List<Reply> = emptyList()
)

data class PostDetail(
    val id: String = "",
    val authorName: String = "",
    val authorAvatar: String? = null,
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val commentCount: Int = 0,
    val authorId: String = ""
)

sealed class PostDetailState {
    object Loading : PostDetailState()
    data class Success(
        val post: PostDetail,
        val comments: List<Comment>,
        val hasUpvoted: Boolean = false
    ) : PostDetailState()
    data class Error(val message: String) : PostDetailState()
}

class PostDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<PostDetailState>(PostDetailState.Loading)
    val state: StateFlow<PostDetailState> = _state

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    fun loadPost(postId: String) {
        val user = auth.currentUser
        viewModelScope.launch {
            try {
                val doc = db.collection("posts").document(postId).get().await()
                val post = PostDetail(
                    id = doc.id,
                    authorName = doc.getString("authorName") ?: "",
                    authorAvatar = doc.getString("authorAvatar"),
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    upvotes = (doc.getLong("upvotes") ?: 0).toInt(),
                    commentCount = (doc.getLong("commentCount") ?: 0).toInt(),
                    authorId = doc.getString("authorId") ?: ""
                )

                val hasUpvoted = if (user != null) {
                    val upvoteDoc = db.collection("posts").document(postId)
                        .collection("upvotes").document(user.uid).get().await()
                    upvoteDoc.exists()
                } else false

                val commentsSnapshot = db.collection("posts").document(postId)
                    .collection("comments")
                    .orderBy("createdAt")
                    .get().await()

                val comments = commentsSnapshot.documents.map { c ->
                    val repliesSnapshot = db.collection("posts").document(postId)
                        .collection("comments").document(c.id)
                        .collection("replies")
                        .orderBy("createdAt")
                        .get().await()

                    val replies = repliesSnapshot.documents.map { r ->
                        Reply(
                            id = r.id,
                            authorName = r.getString("authorName") ?: "",
                            content = r.getString("content") ?: ""
                        )
                    }

                    Comment(
                        id = c.id,
                        authorName = c.getString("authorName") ?: "",
                        authorAvatar = c.getString("authorAvatar"),
                        content = c.getString("content") ?: "",
                        replies = replies
                    )
                }

                _state.value = PostDetailState.Success(post, comments, hasUpvoted)
                checkSaved(postId)
            } catch (e: Exception) {
                _state.value = PostDetailState.Error(e.message ?: "Hata")
            }
        }
    }

    fun checkSaved(postId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid)
                    .collection("saved").document(postId).get().await()
                _isSaved.value = doc.exists()
            } catch (e: Exception) { }
        }
    }

    fun toggleSave(postId: String) {
        val user = auth.currentUser ?: return
        val current = _state.value as? PostDetailState.Success ?: return
        viewModelScope.launch {
            try {
                val savedRef = db.collection("users").document(user.uid)
                    .collection("saved").document(postId)
                if (_isSaved.value) {
                    savedRef.delete().await()
                    _isSaved.value = false
                } else {
                    savedRef.set(mapOf(
                        "postId" to postId,
                        "title" to current.post.title,
                        "authorName" to current.post.authorName,
                        "tags" to current.post.tags,
                        "savedAt" to com.google.firebase.Timestamp.now()
                    )).await()
                    _isSaved.value = true
                }
            } catch (e: Exception) { }
        }
    }

    fun toggleUpvote(postId: String) {
        val user = auth.currentUser ?: return
        val current = _state.value as? PostDetailState.Success ?: return
        viewModelScope.launch {
            try {
                val upvoteRef = db.collection("posts").document(postId)
                    .collection("upvotes").document(user.uid)
                val postRef = db.collection("posts").document(postId)

                if (current.hasUpvoted) {
                    upvoteRef.delete().await()
                    postRef.update("upvotes", FieldValue.increment(-1)).await()
                    _state.value = current.copy(
                        post = current.post.copy(upvotes = current.post.upvotes - 1),
                        hasUpvoted = false
                    )
                } else {
                    upvoteRef.set(mapOf("uid" to user.uid)).await()
                    postRef.update("upvotes", FieldValue.increment(1)).await()
                    db.collection("users").document(current.post.authorId)
                        .update("reputation", FieldValue.increment(1)).await()
                    _state.value = current.copy(
                        post = current.post.copy(upvotes = current.post.upvotes + 1),
                        hasUpvoted = true
                    )
                }
            } catch (e: Exception) { }
        }
    }

    fun addComment(postId: String, content: String) {
        val user = auth.currentUser ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                val comment = hashMapOf(
                    "authorName" to (user.displayName ?: "kullanici"),
                    "authorAvatar" to user.photoUrl?.toString(),
                    "content" to content,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("posts").document(postId)
                    .collection("comments").add(comment).await()
                loadPost(postId)
            } catch (e: Exception) { }
        }
    }

    fun addReply(postId: String, commentId: String, content: String) {
        val user = auth.currentUser ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                val reply = hashMapOf(
                    "authorName" to (user.displayName ?: "kullanici"),
                    "content" to content,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("posts").document(postId)
                    .collection("comments").document(commentId)
                    .collection("replies").add(reply).await()
                loadPost(postId)
            } catch (e: Exception) { }
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("posts").document(postId).get().await()
                if (doc.getString("authorId") == user.uid) {
                    db.collection("posts").document(postId).delete().await()
                    onSuccess()
                }
            } catch (e: Exception) { }
        }
    }

    fun reportPost(postId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val report = hashMapOf(
                    "postId" to postId,
                    "reportedBy" to user.uid,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("reports").add(report).await()
            } catch (e: Exception) { }
        }
    }
}