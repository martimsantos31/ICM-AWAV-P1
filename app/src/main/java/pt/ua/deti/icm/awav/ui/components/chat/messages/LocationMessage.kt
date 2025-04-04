package pt.ua.deti.icm.awav.ui.components.chat.messages

import pt.ua.deti.icm.awav.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.data.model.SimpleLocation
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.LightPurple
import pt.ua.deti.icm.awav.ui.theme.Purple

@Composable
fun LocationMessage(
    location: SimpleLocation?,
    isFromCurrentUser: Boolean,
    avatarResId: Int? = null,
    modifier: Modifier = Modifier
) {
    if (location == null) return

    if (isFromCurrentUser) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Purple)
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_placeholder),
                    contentDescription = "Location: ${location.latitude}, ${location.longitude}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
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
            modifier = modifier,
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
                    .width(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightPurple)
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_placeholder),
                    contentDescription = "Location: ${location.latitude}, ${location.longitude}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}