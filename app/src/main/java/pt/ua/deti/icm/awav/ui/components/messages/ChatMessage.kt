package pt.ua.deti.icm.awav.ui.components.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.ua.deti.icm.awav.data.model.Message
import pt.ua.deti.icm.awav.data.model.MessageType

@Composable
fun ChatMessage(
    message: Message,
    isFromCurrentUser: Boolean,
    avatarResId: Int? = null,
    modifier: Modifier = Modifier
) {
    when (message.type) {
        MessageType.TEXT -> TextMessage(
            message = message.content,
            isFromCurrentUser = isFromCurrentUser,
            avatarResId = avatarResId,
            modifier = modifier
        )
        MessageType.IMAGE -> ImageMessage(
            imageUrl = message.mediaUrl ?: "",
            isFromCurrentUser = isFromCurrentUser,
            avatarResId = avatarResId,
            modifier = modifier
        )
        MessageType.LOCATION -> LocationMessage(
            location = message.location,
            isFromCurrentUser = isFromCurrentUser,
            avatarResId = avatarResId,
            modifier = modifier
        )
    }
}
