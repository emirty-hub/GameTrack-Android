package com.rblxinsider.app.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val postCount: Int = 0,
    val reputation: Int = 0,
    val createdAt: Timestamp? = null
)