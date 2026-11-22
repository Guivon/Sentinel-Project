package br.com.sentinel.app.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelBackgroundLight
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelPrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.privacidade_e_lgpd),
                        fontWeight = FontWeight.Bold,
                        color = SentinelDarkBlueButton
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar),
                            tint = SentinelDarkBlueButton
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SentinelBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PolicyCard(
                title = R.string.tratamento_de_dados_pessoais,
                body = R.string.em_conformidade_com_a_lgpd_lei_geral_d,
                titleSize = 16,
                bodySize = 13,
                bodyColor = Color.DarkGray,
                titleColor = SentinelDarkBlueButton
            )
            PolicyCard(R.string.s_1_leitura_de_notificacoes, R.string.o_conteudo_de_notificacoes_sms_e_e_mai)
            PolicyCard(R.string.s_2_chamadas_telefonicas, R.string.a_identificacao_de_numeros_suspeitos_e)
            PolicyCard(R.string.s_3_seus_direitos_lgpd, R.string.voce_tem_total_autonomia_para_ativar_o)
        }
    }
}

@Composable
private fun PolicyCard(
    @StringRes title: Int,
    @StringRes body: Int,
    titleSize: Int = 14,
    bodySize: Int = 12,
    bodyColor: Color = Color.Gray,
    titleColor: Color = Color.Unspecified
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(title),
                fontSize = titleSize.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(if (titleSize > 14) 8.dp else 4.dp))
            Text(text = stringResource(body), fontSize = bodySize.sp, color = bodyColor)
        }
    }
}
