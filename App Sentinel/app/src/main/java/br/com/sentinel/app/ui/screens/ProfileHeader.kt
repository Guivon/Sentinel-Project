package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AvatarStore
import br.com.sentinel.app.ui.components.UserAvatar
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.launch

@Composable
internal fun ProfileHeader(userName: String, userEmail: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photoVersion by remember { mutableIntStateOf(0) }
    val hasPhoto = remember(photoVersion) { AvatarStore.hasPhoto(context) }
    val saveFailed = stringResource(R.string.photo_save_failed)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            if (AvatarStore.saveFromUri(context, uri)) {
                photoVersion += 1
            } else {
                Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val pickPhoto = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.clickable { pickPhoto() }) {
            UserAvatar(
                size = 88.dp,
                version = photoVersion,
                modifier = Modifier.border(3.dp, SentinelYellow, CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .background(SentinelYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = stringResource(R.string.photo_change),
                    tint = SentinelBlueBg,
                    modifier = Modifier.size(16.dp).padding(0.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName.ifBlank { stringResource(R.string.usuario) },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = userEmail, color = Color.White, fontSize = 13.sp)
            if (hasPhoto) {
                TextButton(
                    onClick = {
                        AvatarStore.remove(context)
                        photoVersion += 1
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(stringResource(R.string.photo_remove), color = SentinelYellow, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = stringResource(R.string.photo_hint),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
