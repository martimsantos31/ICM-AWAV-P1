package pt.ua.deti.icm.awav.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.LightPurple
import pt.ua.deti.icm.awav.ui.theme.Purple

@Composable
fun Modifier.ChatMessage(
    message: String,
    isFromCurrentUser: Boolean,
    avatarResId: Int? = null
) {
    if (isFromCurrentUser) {
        Row(
            modifier = this,
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Purple)
                    .padding(AWAVStyles.chatMessagePadding)
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            avatarResId?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(AWAVStyles.chatAvatarSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    } else {
        Row(
            modifier = this,
            verticalAlignment = Alignment.Bottom
        ) {
            avatarResId?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(AWAVStyles.chatAvatarSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightPurple)
                    .padding(AWAVStyles.chatMessagePadding)
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}