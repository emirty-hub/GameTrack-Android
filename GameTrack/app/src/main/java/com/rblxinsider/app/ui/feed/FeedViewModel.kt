package com.rblxinsider.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rblxinsider.app.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _allPosts = mutableListOf<Post>()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedTag = MutableStateFlow("Tumu")
    val selectedTag: StateFlow<String> = _selectedTag

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get().await()

                _allPosts.clear()
                _allPosts.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                })
                applyFilter()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setTag(tag: String) {
        _selectedTag.value = tag
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        var filtered = _allPosts.toList()

        if (_selectedTag.value != "Tumu") {
            filtered = filtered.filter { post ->
                post.tags.contains(_selectedTag.value)
            }
        }

        if (_searchQuery.value.isNotBlank()) {
            val q = _searchQuery.value.lowercase()
            filtered = filtered.filter { post ->
                post.title.lowercase().contains(q) ||
                        post.description.lowercase().contains(q) ||
                        post.authorName.lowercase().contains(q)
            }
        }

        _posts.value = filtered
    }
}