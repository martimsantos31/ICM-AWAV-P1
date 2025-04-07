package pt.ua.deti.icm.awav.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.model.FeedPost
import pt.ua.deti.icm.awav.data.model.FeedPostType
import pt.ua.deti.icm.awav.utils.StorageUtils
import java.io.File
import java.util.Date
import java.util.UUID

/**
 * Comment data class for feed posts
 */
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorPhotoUrl: String? = null,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository for managing feed posts in Firebase
 */
class FirebaseFeedRepository {
    private val TAG = "FirebaseFeedRepository"
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val postsCollection = db.collection("feed_posts")
    private val usersCollection = db.collection("users")
    private val likesCollection = db.collection("post_likes")
    private val commentsCollection = db.collection("post_comments")
    
    // Feed posts state
    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Set up real-time listener for feed posts
        setupFeedListener()
    }
    
    /**
     * Set up a real-time listener for feed posts
     */
    private fun setupFeedListener() {
        postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    _error.value = "Failed to load feed: ${e.message}"
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    _isLoading.value = true
                    try {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                
                                // Get likes count
                                val likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0
                                
                                // Get comments count
                                val commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0
                                
                                // Check if current user has liked this post
                                val currentUserId = auth.currentUser?.uid
                                val userLikes = data["likedBy"] as? List<String> ?: emptyList()
                                val isLikedByCurrentUser = currentUserId != null && userLikes.contains(currentUserId)
                                
                                // Get author details
                                val authorId = data["authorId"] as? String ?: ""
                                val authorName = data["authorName"] as? String ?: "Unknown User"
                                val authorPhotoUrl = data["authorPhotoUrl"] as? String
                                
                                // Get timestamp
                                val timestamp = (data["timestamp"] as? Timestamp)?.toDate() ?: Date()
                                
                                // Get content
                                val content = data["content"] as? String ?: ""
                                
                                // Get type
                                val typeString = data["type"] as? String ?: FeedPostType.TEXT.name
                                val type = try {
                                    FeedPostType.valueOf(typeString)
                                } catch (e: Exception) {
                                    FeedPostType.TEXT
                                }
                                
                                // Get image URL
                                val imageUrl = data["imageUrl"] as? String
                                
                                // Create FeedPost
                                FeedPost(
                                    id = doc.id,
                                    authorId = authorId,
                                    authorName = authorName,
                                    authorPhotoUrl = authorPhotoUrl,
                                    authorAvatarResId = 0, // We'll get this from Firestore or use a default
                                    content = content,
                                    imageUrl = imageUrl,
                                    type = type,
                                    timestamp = timestamp,
                                    likes = likesCount,
                                    comments = commentsCount,
                                    isLikedByCurrentUser = isLikedByCurrentUser
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing post: ${e.message}", e)
                                null
                            }
                        }
                        
                        _feedPosts.value = posts
                        Log.d(TAG, "Feed updated with ${posts.size} posts")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing feed data", e)
                        _error.value = "Error processing feed data: ${e.message}"
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
    }
    
    /**
     * Create a new text post
     */
    suspend fun createTextPost(content: String, type: FeedPostType): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        try {
            _isLoading.value = true
            
            // Get user display name from Firestore
            val userEmail = currentUser.email
            val userDoc = userEmail?.let { usersCollection.document(it).get().await() }
            val displayName = userDoc?.getString("displayName") ?: currentUser.displayName ?: "Anonymous"
            
            // Get user photo URL
            val photoUrl = userDoc?.getString("photoUrl") ?: currentUser.photoUrl?.toString()
            
            // Create post data
            val postId = UUID.randomUUID().toString()
            val postData = hashMapOf(
                "id" to postId,
                "authorId" to currentUser.uid,
                "authorEmail" to currentUser.email,
                "authorName" to displayName,
                "authorPhotoUrl" to photoUrl,
                "content" to content,
                "type" to type.name,
                "timestamp" to Timestamp.now(),
                "likesCount" to 0,
                "commentsCount" to 0,
                "likedBy" to listOf<String>()
            )
            
            // Add post to Firestore
            postsCollection.document(postId).set(postData).await()
            
            Log.d(TAG, "Post created successfully with ID: $postId")
            return Result.success(postId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post: ${e.message}", e)
            _error.value = "Failed to create post: ${e.message}"
            return Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Create a new post with an image
     */
    suspend fun createImagePost(content: String, imageFile: java.io.File, type: FeedPostType): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        try {
            _isLoading.value = true
            
            // Get user display name from Firestore
            val userEmail = currentUser.email
            val userDoc = userEmail?.let { usersCollection.document(it).get().await() }
            val displayName = userDoc?.getString("displayName") ?: currentUser.displayName ?: "Anonymous"
            
            // Get user photo URL
            val photoUrl = userDoc?.getString("photoUrl") ?: currentUser.photoUrl?.toString()
            
            // Create a unique filename - use a simpler path structure
            val postId = UUID.randomUUID().toString()
            val imageFileName = "feed_images/${postId}.jpg"
            
            // Upload image to Firebase Storage
            val imageUri = Uri.fromFile(imageFile)
            val storageRef = storage.reference.child(imageFileName)
            
            Log.d(TAG, "Starting image upload to path: $imageFileName")
            val uploadTask = storageRef.putFile(imageUri).await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Create post with image URL
            val post = FeedPost(
                id = postId,
                authorId = currentUser.uid,
                authorName = displayName,
                authorPhotoUrl = photoUrl,
                authorAvatarResId = 0, // We'll get this from Firestore or use a default
                content = content,
                imageUrl = downloadUrl,
                type = type,
                timestamp = Timestamp.now().toDate(),
                likes = 0,
                comments = 0,
                isLikedByCurrentUser = false
            )
            
            // Add post to Firestore
            postsCollection.document(postId).set(FeedPost.toFirestore(post)).await()
            
            Log.d(TAG, "Image post created successfully with ID: $postId")
            return Result.success(postId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image post: ${e.message}", e)
            _error.value = "Failed to create post: ${e.message}"
            return Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Toggle like on a post
     */
    suspend fun toggleLike(postId: String): Result<Boolean> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        try {
            // Get the post
            val postDoc = postsCollection.document(postId).get().await()
            if (!postDoc.exists()) {
                Log.e(TAG, "Post does not exist: $postId")
                return Result.failure(Exception("Post not found"))
            }
            
            // Get current likes data
            val likedBy = postDoc.get("likedBy") as? List<String> ?: listOf()
            val currentLikesCount = (postDoc.getLong("likesCount") ?: 0).toInt()
            
            // Check if user already liked this post
            val userId = currentUser.uid
            val isAlreadyLiked = likedBy.contains(userId)
            
            // Update the post with the new like status
            val updatedLikedBy = if (isAlreadyLiked) {
                likedBy.filter { it != userId }
            } else {
                likedBy + userId
            }
            
            // Update the post
            val updates = hashMapOf<String, Any>(
                "likedBy" to updatedLikedBy,
                "likesCount" to if (isAlreadyLiked) currentLikesCount - 1 else currentLikesCount + 1
            )
            
            postsCollection.document(postId).update(updates).await()
            
            Log.d(TAG, "Post like toggled successfully for post: $postId, liked: ${!isAlreadyLiked}")
            return Result.success(!isAlreadyLiked)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like: ${e.message}", e)
            _error.value = "Failed to like post: ${e.message}"
            return Result.failure(e)
        }
    }
    
    /**
     * Add a comment to a post
     */
    suspend fun addComment(postId: String, comment: String): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        try {
            // Get user display name
            val userEmail = currentUser.email
            val userDoc = userEmail?.let { usersCollection.document(it).get().await() }
            val displayName = userDoc?.getString("displayName") ?: currentUser.displayName ?: "Anonymous"
            
            // Create comment data
            val commentId = UUID.randomUUID().toString()
            val commentData = hashMapOf(
                "id" to commentId,
                "postId" to postId,
                "authorId" to currentUser.uid,
                "authorName" to displayName,
                "content" to comment,
                "timestamp" to Timestamp.now()
            )
            
            // Add comment to Firestore
            commentsCollection.document(commentId).set(commentData).await()
            
            // Update post's comment count
            val postDoc = postsCollection.document(postId).get().await()
            if (postDoc.exists()) {
                val currentCommentsCount = (postDoc.getLong("commentsCount") ?: 0).toInt()
                postsCollection.document(postId).update("commentsCount", currentCommentsCount + 1).await()
            }
            
            Log.d(TAG, "Comment added successfully to post: $postId")
            return Result.success(commentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment: ${e.message}", e)
            _error.value = "Failed to add comment: ${e.message}"
            return Result.failure(e)
        }
    }
    
    /**
     * Get comments for a post
     */
    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        // Start with empty list
        trySend(emptyList())
        
        // Set up listener for real-time updates
        val listenerRegistration = commentsCollection
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed for comments", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            
                            Comment(
                                id = doc.id,
                                postId = data["postId"] as? String ?: "",
                                authorId = data["authorId"] as? String ?: "",
                                authorName = data["authorName"] as? String ?: "Unknown User",
                                authorPhotoUrl = data["authorPhotoUrl"] as? String,
                                content = data["content"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing comment", e)
                            null
                        }
                    }
                    trySend(comments)
                }
            }
        
        // Clean up listener when flow is cancelled
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Create an announcement (for event organizers)
     */
    suspend fun createAnnouncement(content: String, imageFile: java.io.File?): Result<String> {
        // Check if user is admin
        if (!isUserAdmin()) {
            return Result.failure(Exception("Only administrators can create announcements"))
        }
        
        // Create announcement with or without image
        return if (imageFile != null) {
            createImagePost(content, imageFile, FeedPostType.ANNOUNCEMENT)
        } else {
            createTextPost(content, FeedPostType.ANNOUNCEMENT)
        }
    }
    
    /**
     * Create an event update (for event organizers)
     */
    suspend fun createEventUpdate(content: String, imageFile: java.io.File?): Result<String> {
        // Check if user is admin
        if (!isUserAdmin()) {
            return Result.failure(Exception("Only administrators can create event updates"))
        }
        
        // Create event update with or without image
        return if (imageFile != null) {
            createImagePost(content, imageFile, FeedPostType.EVENT_UPDATE)
        } else {
            createTextPost(content, FeedPostType.EVENT_UPDATE)
        }
    }
    
    /**
     * Delete a post (only if user is the author or an admin)
     */
    suspend fun deletePost(postId: String): Result<Boolean> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        try {
            // Get the post
            val postDoc = postsCollection.document(postId).get().await()
            if (!postDoc.exists()) {
                Log.e(TAG, "Post does not exist: $postId")
                return Result.failure(Exception("Post not found"))
            }
            
            // Check if user is the author
            val authorId = postDoc.getString("authorId")
            if (authorId != currentUser.uid) {
                // Check if user is an admin (would need to implement admin check logic)
                // For now, only allow author to delete
                Log.e(TAG, "User is not the author of this post")
                return Result.failure(Exception("You don't have permission to delete this post"))
            }
            
            // Delete post
            postsCollection.document(postId).delete().await()
            
            // Delete associated comments
            val comments = commentsCollection.whereEqualTo("postId", postId).get().await()
            for (comment in comments) {
                commentsCollection.document(comment.id).delete().await()
            }
            
            // Delete image if post has one
            val imageUrl = postDoc.getString("imageUrl")
            if (imageUrl != null) {
                try {
                    // Extract the path from the full URL
                    val path = imageUrl.substringAfter("feed_images/")
                    val storageRef = storage.reference.child("feed_images/$path")
                    storageRef.delete().await()
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting image: ${e.message}", e)
                    // Continue with post deletion even if image deletion fails
                }
            }
            
            Log.d(TAG, "Post deleted successfully: $postId")
            return Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post: ${e.message}", e)
            _error.value = "Failed to delete post: ${e.message}"
            return Result.failure(e)
        }
    }
    
    /**
     * Check if current user is an admin
     */
    private suspend fun isUserAdmin(): Boolean {
        val currentUser = auth.currentUser ?: return false
        
        try {
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            return userDoc.getBoolean("isAdmin") == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user is admin", e)
            return false
        }
    }
    
    companion object {
        @Volatile
        private var instance: FirebaseFeedRepository? = null

        fun getInstance(): FirebaseFeedRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseFeedRepository().also { instance = it }
            }
        }
    }
} 