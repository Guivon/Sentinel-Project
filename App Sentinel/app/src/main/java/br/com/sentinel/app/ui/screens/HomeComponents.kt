package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.AttachedFile
import br.com.sentinel.app.ui.components.SentinelQuickCard
import br.com.sentinel.app.ui.theme.SentinelBorderLight
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelMint
import br.com.sentinel.app.ui.theme.SentinelPaleBlue
import br.com.sentinel.app.ui.theme.SentinelPeriwinkle
import br.com.sentinel.app.ui.theme.SentinelRiskPink
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTeal
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelYellow

private const val MESSAGE_LIMIT = 4000

@Composable
internal fun BoxScope.HomeBackgroundShapes(screenWidth: Dp, screenHeight: Dp) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = screenWidth * 0.2f, y = screenHeight * 0.1f)
            .size(screenWidth * 0.535f)
            .rotate(20f)
            .border(1.dp, SentinelYellow, RoundedCornerShape(36.dp))
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = screenWidth * 0.27f, y = screenHeight * 0.13f)
            .size(screenWidth * 0.535f)
            .rotate(20f)
            .background(SentinelYellow, RoundedCornerShape(36.dp))
    )
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = -(screenWidth * 0.36f), y = screenHeight * 0.1f)
            .size(screenWidth * 0.88f)
            .rotate(-55f)
            .border(1.dp, SentinelTeal, RoundedCornerShape(56.dp))
    )
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = -(screenWidth * 0.32f), y = screenHeight * 0.1f)
            .size(screenWidth * 0.8f)
            .rotate(-55f)
            .background(SentinelMint, RoundedCornerShape(48.dp))
    )
}

@Composable
internal fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.sentinel_logo),
            contentDescription = stringResource(R.string.sentinel),
            colorFilter = ColorFilter.tint(SentinelYellow),
            modifier = Modifier.size(44.dp)
        )
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = stringResource(R.string.notificacoes),
            tint = SentinelYellow,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
internal fun HomeGreeting(userName: String) {
    Text(
        text = if (userName.isNotBlank()) {
            stringResource(R.string.ola_1, userName)
        } else {
            stringResource(R.string.ola)
        },
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = SentinelTextLight
    )
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.conte_com_a_ia_para_te))
            withStyle(SpanStyle(color = SentinelYellow, fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.proteger))
            }
            append(stringResource(R.string.de_golpes))
        },
        fontSize = 30.sp,
        lineHeight = 34.sp,
        color = SentinelTextLight,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun AnalysisInputCard(
    message: String,
    onMessageChange: (String) -> Unit,
    attachment: AttachedFile?,
    onPickAttachment: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        AnalysisCardHeader()
        Spacer(modifier = Modifier.height(16.dp))
        MessageField(
            message = message,
            onMessageChange = onMessageChange,
            onPickAttachment = onPickAttachment
        )
        attachment?.let { file ->
            Spacer(modifier = Modifier.height(8.dp))
            AttachmentChip(file = file, onRemove = onRemoveAttachment)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAnalyze,
            colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = SentinelYellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (attachment != null) {
                    stringResource(R.string.analisar_anexo)
                } else {
                    stringResource(R.string.analisar_mensagem)
                },
                color = SentinelTextLight,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun AnalysisCardHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SentinelRiskRed, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.vector),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.recebeu_uma_mensagem_suspeita),
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.cole_o_texto_do_sms_ou_do_e_mail_ou_an),
                color = SentinelLightBlueCard,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MessageField(
    message: String,
    onMessageChange: (String) -> Unit,
    onPickAttachment: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = message,
            onValueChange = { if (it.length <= MESSAGE_LIMIT) onMessageChange(it) },
            placeholder = {
                Text(stringResource(R.string.cole_sua_mensagem_aqui), color = Color.LightGray)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = SentinelBorderLight,
                focusedBorderColor = SentinelLightBlueCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = 220.dp)
        )
        Text(
            text = "${message.length}/$MESSAGE_LIMIT",
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 12.dp)
        )
        IconButton(
            onClick = onPickAttachment,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = stringResource(R.string.anexar_imagem_ou_pdf),
                tint = SentinelLightBlueCard
            )
        }
    }
}

@Composable
private fun AttachmentChip(file: AttachedFile, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelPaleBlue, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AttachFile, contentDescription = null, tint = SentinelLightBlueCard)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = file.name,
            color = Color.DarkGray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.remover_anexo),
            tint = Color.Gray,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onRemove)
        )
    }
}

@Composable
internal fun HomeQuickActions(onHistoryClick: () -> Unit, onLearnClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SentinelQuickCard(
            title = stringResource(R.string.historico),
            subtitle = stringResource(R.string.veja_seu_historico),
            icon = Icons.Default.History,
            iconBackground = SentinelPeriwinkle,
            onClick = onHistoryClick,
            modifier = Modifier.weight(1f)
        )
        SentinelQuickCard(
            title = stringResource(R.string.aprender),
            subtitle = stringResource(R.string.veja_suas_analises),
            icon = Icons.Default.MenuBook,
            iconBackground = SentinelMint,
            onClick = onLearnClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun SecurityTipsCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelRiskPink, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = SentinelRiskRed,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dicas_de_seguranca),
                color = SentinelRiskRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.desconfie_de_mensagens_com_urgencia_pr),
                color = SentinelRiskRed,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SentinelRiskRed,
            modifier = Modifier.size(36.dp)
        )
    }
}
