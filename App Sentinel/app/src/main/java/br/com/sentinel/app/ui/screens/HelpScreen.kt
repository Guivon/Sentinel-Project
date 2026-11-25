package br.com.sentinel.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.icons.AccountBalance
import br.com.sentinel.app.ui.icons.ChevronLeft
import br.com.sentinel.app.ui.icons.GppMaybe
import br.com.sentinel.app.ui.icons.Security
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelTextHint
import br.com.sentinel.app.ui.theme.SentinelTextSecondary
import br.com.sentinel.app.ui.theme.SentinelYellow

private const val PANEL_CORNER_DP = 32
private const val TIP_ALPHA = 0.6f
private const val TIP_BORDER_DP = 3

private class HelpTopic(val icon: ImageVector, val onBlue: Boolean, val title: Int, val answer: Int)

private val helpTopics = listOf(
    HelpTopic(Icons.Default.Security, true, R.string.help_q_how_title, R.string.help_q_how_answer),
    HelpTopic(Icons.Default.AccountBalance, false, R.string.help_q_bank_title, R.string.help_q_bank_answer),
    HelpTopic(Icons.Default.GppMaybe, true, R.string.help_q_scam_title, R.string.help_q_scam_answer),
    HelpTopic(Icons.Default.Settings, false, R.string.help_q_protect_title, R.string.help_q_protect_answer),
    HelpTopic(Icons.Default.Lock, true, R.string.help_q_data_title, R.string.help_q_data_answer)
)

private class ResolvedTopic(val topic: HelpTopic, val title: String, val answer: String)

@Composable
fun SentinelHelpScreen(onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var openIndex by rememberSaveable { mutableStateOf(-1) }
    val resolved = helpTopics.map { ResolvedTopic(it, stringResource(it.title), stringResource(it.answer)) }
    val visible = resolved.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.answer.contains(query, ignoreCase = true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
    ) {
        HelpHeader(onBack = onBack, query = query, onQueryChange = { query = it })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(topStart = PANEL_CORNER_DP.dp, topEnd = PANEL_CORNER_DP.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            visible.forEach { item ->
                val index = resolved.indexOf(item)
                HelpRow(item, expanded = openIndex == index) { openIndex = if (openIndex == index) -1 else index }
                HorizontalDivider(color = SentinelDivider)
            }
            if (visible.isEmpty()) {
                Text(
                    text = stringResource(R.string.help_no_results),
                    color = SentinelTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            HelpTip()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpHeader(onBack: () -> Unit, query: String, onQueryChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = stringResource(R.string.voltar),
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .offset(x = (-6).dp)
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.central_de_ajuda),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.help_subtitle), color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.help_search_hint), color = SentinelTextHint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SentinelDarkBlueButton) },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HelpRow(item: ResolvedTopic, expanded: Boolean, onToggle: () -> Unit) {
    val circleColor = if (item.topic.onBlue) SentinelBlueBg else SentinelYellow
    val iconColor = if (item.topic.onBlue) SentinelYellow else SentinelDarkBlueButton
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.topic.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = SentinelDarkBlueButton,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = SentinelDarkBlueButton
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.answer,
                color = SentinelTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 64.dp)
            )
        }
    }
}

private class BankChannel(val label: Int, val display: String, val dial: String)

private val bankChannels = listOf(
    BankChannel(R.string.help_ch_central_capitals, "4004 0001", "40040001"),
    BankChannel(R.string.help_ch_central_other, "0800 729 0001", "08007290001"),
    BankChannel(R.string.help_ch_sac, "0800 729 0722", "08007290722"),
    BankChannel(R.string.help_ch_ombudsman, "0800 729 5678", "08007295678"),
    BankChannel(R.string.help_ch_hearing, "0800 729 0088", "08007290088")
)

@Composable
private fun HelpTip() {
    var open by rememberSaveable { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SentinelBlueBg.copy(alpha = TIP_ALPHA))
                .border(TIP_BORDER_DP.dp, SentinelBlueBg, RoundedCornerShape(20.dp))
                .clickable { open = !open }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.help_tip),
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White
            )
        }
        if (open) {
            Spacer(modifier = Modifier.height(12.dp))
            BankChannelsCard()
        }
    }
}

@Composable
private fun BankChannelsCard() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(TIP_BORDER_DP.dp, SentinelBlueBg, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.help_channels_title),
            color = SentinelDarkBlueButton,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        bankChannels.forEach { channel ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${channel.dial}"))) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = null,
                    tint = SentinelBlueBg,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(channel.label),
                        color = SentinelTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = channel.display,
                        color = SentinelDarkBlueButton,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider(color = SentinelDivider)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.help_channels_site),
            color = SentinelDarkBlueButton,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bb.com.br")))
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.help_channels_warning), color = SentinelTextSecondary, fontSize = 12.sp)
    }
}
