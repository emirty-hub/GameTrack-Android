package com.rblxinsider.app.data.model

import com.google.firebase.Timestamp

data class GameUpdate(
    val id: String = "",
    val gameId: String = "",
    val gameName: String = "",
    val description: String = "",
    val source: String = "", // "devforum" | "twitter" | "auto"
    val status: String = "", // "live" | "upcoming"
    val scheduledAt: Timestamp? = null,
    val detectedAt: Timestamp? = null,
    val lang: String = "tr"
)