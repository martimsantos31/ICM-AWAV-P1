package pt.ua.deti.icm.awav.ui.components.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.data.model.FeedPost
import pt.ua.deti.icm.awav.data.model.FeedPostType
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedPostItem(
    post: FeedPost,
    onLikeClick: (FeedPost) -> Unit,
    onCommentClick: (FeedPost) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(post.isLikedByCurrentUser) }
    var likeCount by remember { mutableStateOf(post.likes) }

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())

    val formattedTime = remember(post.timestamp) {
        val today = Calendar.getInstance()
        val postDate = Calendar.getInstance().apply { time = post.timestamp }

        if (today.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR) &&
            today.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)) {
            "Today at ${timeFormatter.format(post.timestamp)}"
        } else {
            "${dateFormatter.format(post.timestamp)} at ${timeFormatter.format(post.timestamp)}"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post header with author info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author avatar
                Image(
                    painter = painterResource(id = post.authorAvatarResId),
                    contentDescription = "Author Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Author name and timestamp
                Column {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (post.type == FeedPostType.ANNOUNCEMENT || post.type == FeedPostType.EVENT_UPDATE) {
                    Spacer(modifier = Modifier.weight(1f))

                    AssistChip(
                        onClick = {},
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (post.type == FeedPostType.ANNOUNCEMENT)
                                Purple.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        label = {
                            Text(
                                text = if (post.type == FeedPostType.ANNOUNCEMENT) "Announcement" else "Event Update",
                                color = if (post.type == FeedPostType.ANNOUNCEMENT)
                                    Purple else MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Post image if available
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // In a real app, you'd load the image from a URL
                    // For now, we'll use a placeholder if imageUrl is not null
                    Image(
                        painter = painterResource(id = post.authorAvatarResId), // Using avatar as placeholder
                        contentDescription = "Post Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons (like, comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like count and button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = {
                        isLiked = !isLiked
                        likeCount += if (isLiked) 1 else -1
                        onLikeClick(post)
                    })
                ) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likeCount += if (isLiked) 1 else -1
                            onLikeClick(post)
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Purple else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = likeCount.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                // Comment count and button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = { onCommentClick(post) })
                ) {
                    IconButton(
                        onClick = { onCommentClick(post) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment"
                        )
                    }
                    Text(
                        text = post.comments.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
} 