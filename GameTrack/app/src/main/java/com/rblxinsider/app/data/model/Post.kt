package com.rblxinsider.app.data.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val gameId: String = "",
    val gameName: String = "",
    val category: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val upvotes: Int = 0,
    val commentCount: Int = 0,
    val lang: String = "tr",
    val tags: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)