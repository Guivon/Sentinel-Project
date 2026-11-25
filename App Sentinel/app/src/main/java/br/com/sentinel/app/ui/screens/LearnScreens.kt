package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.icons.Cancel
import br.com.sentinel.app.ui.icons.ChevronLeft
import br.com.sentinel.app.ui.icons.ChevronRight
import br.com.sentinel.app.ui.icons.Fingerprint
import br.com.sentinel.app.ui.icons.StarBorder
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelPaleBlueAlt
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTeal
import br.com.sentinel.app.ui.theme.SentinelTealDark
import br.com.sentinel.app.ui.theme.SentinelTealDarker
import br.com.sentinel.app.ui.theme.SentinelTextBody
import br.com.sentinel.app.ui.theme.SentinelTextDark
import br.com.sentinel.app.ui.theme.SentinelTextSecondary
import br.com.sentinel.app.ui.theme.SentinelWarningDarkest
import br.com.sentinel.app.ui.theme.SentinelWarningYellow
import br.com.sentinel.app.ui.theme.SentinelYellow

internal data class LearnTopic(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val howItWorks: String,
    val warningSigns: List<String>,
    val howToProtect: List<String>
)

@Composable
internal fun learnTopics(): List<LearnTopic> = listOf(
    fakeBillTopic(),
    whatsAppCloningTopic(),
    emailPhishingTopic(),
    fakeJobTopic()
)

@Composable
private fun fakeBillTopic(): LearnTopic = LearnTopic(
    title = stringResource(R.string.golpe_do_falso_boleto),
    description = stringResource(R.string.como_conferir_os_dados_do_beneficiario),
    icon = Icons.Outlined.Cancel,
    iconTint = Color.White,
    iconBackground = SentinelRiskRed,
    howItWorks = stringResource(R.string.o_golpista_envia_um_boleto_falso_muita),
    warningSigns = listOf(
        stringResource(R.string.boleto_chegou_por_e_mail_ou_whatsapp_s),
        stringResource(R.string.valor_ou_vencimento_diferentes_do_que),
        stringResource(R.string.nome_do_beneficiario_nao_bate_com_a_em),
        stringResource(R.string.pressao_para_pagar_rapidamente_com_ame)
    ),
    howToProtect = listOf(
        stringResource(R.string.confira_o_nome_do_beneficiario_antes_d),
        stringResource(R.string.compare_o_codigo_de_barras_com_o_do_ap),
        stringResource(R.string.em_caso_de_duvida_ligue_para_a_empresa),
        stringResource(R.string.evite_pagar_boletos_recebidos_fora_dos)
    )
)

@Composable
private fun whatsAppCloningTopic(): LearnTopic = LearnTopic(
    title = stringResource(R.string.clonagem_de_whatsapp),
    description = stringResource(R.string.ative_a_confirmacao_em_duas_etapas_e_n),
    icon = Icons.Default.Fingerprint,
    iconTint = SentinelTealDarker,
    iconBackground = SentinelTeal,
    howItWorks = stringResource(R.string.o_golpista_descobre_seu_numero_e_tenta),
    warningSigns = listOf(
        stringResource(R.string.voce_recebe_um_sms_com_codigo_de_verif),
        stringResource(R.string.alguem_pede_esse_codigo_por_telefone_o),
        stringResource(R.string.seu_whatsapp_desconecta_sozinho_sem_mo)
    ),
    howToProtect = listOf(
        stringResource(R.string.nunca_compartilhe_o_codigo_de_verifica),
        stringResource(R.string.ative_a_confirmacao_em_duas_etapas_em),
        stringResource(R.string.se_seu_whatsapp_for_clonado_avise_seus)
    )
)

@Composable
private fun emailPhishingTopic(): LearnTopic = LearnTopic(
    title = stringResource(R.string.phishing_por_e_mail),
    description = stringResource(R.string.aprenda_a_identificar_remetentes_falso),
    icon = Icons.Outlined.Cancel,
    iconTint = SentinelLightBlueCard,
    iconBackground = SentinelPaleBlueAlt,
    howItWorks = stringResource(R.string.voce_recebe_um_e_mail_parecido_com_o_d),
    warningSigns = listOf(
        stringResource(R.string.e_mail_do_remetente_tem_dominio_estran),
        stringResource(R.string.erros_de_portugues_ou_formatacao_estra),
        stringResource(R.string.pede_para_clicar_em_link_e_inserir_sen),
        stringResource(R.string.cria_urgencia_sua_conta_sera_bloqueada)
    ),
    howToProtect = listOf(
        stringResource(R.string.nunca_clique_em_links_de_e_mail_para_a),
        stringResource(R.string.confira_o_dominio_completo_do_remetent),
        stringResource(R.string.bancos_nunca_pedem_senha_completa_por),
        stringResource(R.string.em_caso_de_duvida_entre_em_contato_pel)
    )
)

@Composable
private fun fakeJobTopic(): LearnTopic = LearnTopic(
    title = stringResource(R.string.falso_emprego_tarefas),
    description = stringResource(R.string.cuidado_com_promessas_de_dinheiro_faci),
    icon = Icons.Outlined.Warning,
    iconTint = SentinelWarningDarkest,
    iconBackground = SentinelWarningYellow,
    howItWorks = stringResource(R.string.voce_recebe_uma_oferta_de_emprego_faci),
    warningSigns = listOf(
        stringResource(R.string.promessa_de_ganho_alto_por_tarefa_simp),
        stringResource(R.string.pedem_deposito_ou_pagamento_antecipado),
        stringResource(R.string.contato_comecou_por_mensagem_nao_solic),
        stringResource(R.string.pressao_para_recrutar_mais_pessoas_par)
    ),
    howToProtect = listOf(
        stringResource(R.string.desconfie_de_qualquer_emprego_que_peca),
        stringResource(R.string.empresas_serias_nao_cobram_para_voce_t),
        stringResource(R.string.pesquise_o_nome_da_empresa_antes_de_ac),
        stringResource(R.string.nunca_deposite_dinheiro_para_desbloque)
    )
)

@Composable
fun SentinelLearnScreen(onBack: () -> Unit) {
    var selectedTopic by remember { mutableStateOf<LearnTopic?>(null) }

    if (selectedTopic != null) {
        SentinelLearnDetailScreen(
            topic = selectedTopic!!,
            onBack = { selectedTopic = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = stringResource(R.string.voltar),
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.aprender_e_proteger),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = SentinelYellow,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.seu_nivel_de_protecao_alto),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.voce_completou_2_de_4_guias_essenciais),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            learnTopics().forEach { topic ->
                SentinelLearnCard(topic, onClick = { selectedTopic = topic })
            }
        }
    }
}

@Composable
internal fun SentinelLearnCard(topic: LearnTopic, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(topic.iconBackground, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = null,
                tint = topic.iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.title,
                color = SentinelTextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = topic.description,
                color = SentinelTextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ler_agora),
                    color = SentinelLightBlueCard,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SentinelLightBlueCard,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
internal fun SentinelLearnDetailScreen(topic: LearnTopic, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = stringResource(R.string.voltar),
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.height(12.dp))
        LearnDetailHeader(topic)
        Spacer(modifier = Modifier.height(20.dp))
        LearnHowItWorksCard(topic.howItWorks)
        Spacer(modifier = Modifier.height(16.dp))
        LearnBulletCard(
            icon = Icons.Outlined.Warning,
            iconTint = SentinelRiskRed,
            title = stringResource(R.string.sinais_de_alerta),
            titleColor = SentinelRiskRed,
            bullet = "• ",
            items = topic.warningSigns
        )
        Spacer(modifier = Modifier.height(16.dp))
        LearnBulletCard(
            icon = Icons.Default.Lock,
            iconTint = SentinelTeal,
            title = stringResource(R.string.como_se_proteger),
            titleColor = SentinelTealDark,
            bullet = "✓ ",
            items = topic.howToProtect
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LearnDetailHeader(topic: LearnTopic) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(topic.iconBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = null,
                tint = topic.iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = topic.title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LearnHowItWorksCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.como_funciona),
            color = SentinelTextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, color = SentinelTextBody, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun LearnBulletCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    titleColor: Color,
    bullet: String,
    items: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Text(bullet, color = titleColor, fontWeight = FontWeight.Bold)
                Text(item, color = SentinelTextBody, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}
