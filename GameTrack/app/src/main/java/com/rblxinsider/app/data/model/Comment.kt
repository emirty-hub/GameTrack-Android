package com.rblxinsider.app.data.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val body: String = "",
    val createdAt: Timestamp? = null
)