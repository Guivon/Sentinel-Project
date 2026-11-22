package br.com.sentinel.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import br.com.sentinel.app.data.AvatarStore
import br.com.sentinel.app.ui.theme.SentinelDisabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UserAvatar(size: Dp, version: Int = 0, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photo by produceState<ImageBitmap?>(initialValue = null, version) {
        value = withContext(Dispatchers.IO) { AvatarStore.load(context)?.asImageBitmap() }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SentinelDisabled, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val current = photo
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.68f)
            )
        }
    }
}
