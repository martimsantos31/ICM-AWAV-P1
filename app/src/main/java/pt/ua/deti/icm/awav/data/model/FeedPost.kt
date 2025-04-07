package pt.ua.deti.icm.awav.data.model

import java.util.Date

enum class FeedPostType {
    TEXT,
    IMAGE,
    ANNOUNCEMENT,
    EVENT_UPDATE
}

data class FeedPost(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarResId: Int,
    val content: String,
    val imageUrl: String? = null,
    val type: FeedPostType,
    val timestamp: Date = Date(),
    val likes: Int = 0,
    val comments: Int = 0,
    val isLikedByCurrentUser: Boolean = false
) 