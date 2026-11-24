package br.com.sentinel.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelBlueBg

@Composable
fun SentinelLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.sentinel_logo),
        contentDescription = stringResource(R.string.logo_do_sentinel),
        colorFilter = ColorFilter.tint(SentinelBlueBg),
        modifier = modifier.size(180.dp)
    )
}
