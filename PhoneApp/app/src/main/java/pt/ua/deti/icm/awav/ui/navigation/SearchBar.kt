package pt.ua.deti.icm.awav.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.SearchBarGray

@Composable
fun AWAVSearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search",
    onValueChange: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(AWAVStyles.searchBarHeight)
            .clip(RoundedCornerShape(AWAVStyles.searchBarCornerRadius)),
        placeholder = { Text(hint) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SearchBarGray,
            unfocusedContainerColor = SearchBarGray,
            disabledContainerColor = SearchBarGray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}