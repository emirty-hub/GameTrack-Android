package com.rblxinsider.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SavedPost(
    val postId: String = "",
    val title: String = "",
    val authorName: String = "",
    val tags: List<String> = emptyList()
)

class SavedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableStateFlow<List<SavedPost>>(emptyList())
    val posts: StateFlow<List<SavedPost>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadSaved()
    }

    fun loadSaved() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("users").document(user.uid)
                    .collection("saved")
                    .orderBy("savedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get().await()

                _posts.value = snapshot.documents.mapNotNull { doc ->
                    SavedPost(
                        postId = doc.getString("postId") ?: return@mapNotNull null,
                        title = doc.getString("title") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeSaved(postId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.uid)
                    .collection("saved").document(postId).delete().await()
                _posts.value = _posts.value.filter { it.postId != postId }
            } catch (e: Exception) { }
        }
    }
}