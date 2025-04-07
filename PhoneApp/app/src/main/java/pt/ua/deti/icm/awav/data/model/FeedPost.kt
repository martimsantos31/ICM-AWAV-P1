package pt.ua.deti.icm.awav.data.model

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
    val authorPhotoUrl: String? = null,
    val authorAvatarResId: Int = 0,
    val content: String,
    val imageUrl: String? = null,
    val type: FeedPostType,
    val timestamp: Date = Date(),
    val likes: Int = 0,
    val comments: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val likedBy: List<String> = emptyList()
) {
    companion object {
        fun fromFirestore(id: String, data: Map<String, Any>): FeedPost? {
            return try {
                val likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0
                val commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0
                val authorId = data["authorId"] as? String ?: ""
                val authorName = data["authorName"] as? String ?: "Unknown User"
                val authorPhotoUrl = data["authorPhotoUrl"] as? String
                val timestamp = when (val ts = data["timestamp"]) {
                    is Timestamp -> ts.toDate()
                    is Date -> ts
                    else -> Date()
                }
                val content = data["content"] as? String ?: ""
                val typeString = data["type"] as? String ?: FeedPostType.TEXT.name
                val type = try {
                    FeedPostType.valueOf(typeString)
                } catch (e: Exception) {
                    FeedPostType.TEXT
                }
                val imageUrl = data["imageUrl"] as? String
                val likedBy = data["likedBy"] as? List<String> ?: emptyList()
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val isLikedByCurrentUser = currentUserId != null && likedBy.contains(currentUserId)
                FeedPost(
                    id = id,
                    authorId = authorId,
                    authorName = authorName,
                    authorPhotoUrl = authorPhotoUrl,
                    content = content,
                    imageUrl = imageUrl,
                    type = type,
                    timestamp = timestamp,
                    likes = likesCount,
                    comments = commentsCount,
                    isLikedByCurrentUser = isLikedByCurrentUser,
                    likedBy = likedBy
                )
            } catch (e: Exception) {
                android.util.Log.e("FeedPost", "Error creating FeedPost from Firestore data: ${e.message}", e)
                null
            }
        }
        
        fun toFirestore(post: FeedPost): Map<String, Any> {
            return mapOf(
                "id" to post.id,
                "authorId" to post.authorId,
                "authorName" to post.authorName,
                "authorPhotoUrl" to (post.authorPhotoUrl ?: ""),
                "content" to post.content,
                "imageUrl" to (post.imageUrl ?: ""),
                "type" to post.type.name,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likesCount" to post.likes,
                "commentsCount" to post.comments,
                "likedBy" to post.likedBy
            )
        }
    }
} 