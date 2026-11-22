package br.com.sentinel.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelBackgroundLight
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelTheme
import java.time.Instant

const val CONSENT_VERSION = "2026-11-21"

private class ConsentSection(val title: String, val intro: String? = null, val items: List<String> = emptyList())

@Composable
private fun consentSections(): List<ConsentSection> = listOf(
    ConsentSection(
        title = stringResource(R.string.s_1_o_que_e_o_sentinel),
        intro = stringResource(R.string.o_sentinel_e_um_aplicativo_de_apoio_qu),
        items = listOf(
            stringResource(R.string.o_resultado_e_uma_estimativa_nenhum_si),
            stringResource(R.string.o_sentinel_nao_substitui_o_contato_ofi),
            stringResource(R.string.voce_deve_usar_o_aplicativo_apenas_par),
            stringResource(R.string.menores_de_18_anos_so_podem_usar_o_apl)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_2_quais_dados_usamos),
        items = listOf(
            stringResource(R.string.conta_seu_nome_e_mail_e_senha_a_senha),
            stringResource(R.string.mensagens_que_voce_analisa_o_texto_que),
            stringResource(R.string.mensagens_recebidas_sms_notificacoes_d),
            stringResource(R.string.texto_da_tela_se_voce_ativar_a_leitura),
            stringResource(R.string.contatos_usados_apenas_para_saber_se_o),
            stringResource(R.string.registro_de_cada_analise_no_servidor_s),
            stringResource(R.string.relatos_de_golpe_se_voce_usar_registra),
            stringResource(R.string.historico_a_lista_de_alertas_fica_salv)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_3_para_que_usamos_esses_dados),
        items = listOf(
            stringResource(R.string.identificar_possiveis_golpes_e_avisar),
            stringResource(R.string.mostrar_seu_historico_de_analises_e_o),
            stringResource(R.string.manter_sua_conta_e_proteger_o_acesso_a),
            stringResource(R.string.reconhecer_novos_tipos_de_golpe_a_part),
            stringResource(R.string.nao_vendemos_seus_dados_e_nao_os_usamo)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_4_com_quem_os_dados_sao_compartilhados),
        intro = stringResource(R.string.para_funcionar_o_sentinel_usa_servicos),
        items = listOf(
            stringResource(R.string.supabase_login_e_banco_de_dados_dos_re),
            stringResource(R.string.amazon_web_services_amazon_bedrock_int),
            stringResource(R.string.render_hospedagem_do_nosso_servidor),
            stringResource(R.string.google_leitura_dos_seus_e_mails_pela_a),
            stringResource(R.string.tambem_podemos_compartilhar_dados_se_a)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_5_como_protegemos_sua_privacidade),
        items = listOf(
            stringResource(R.string.a_comunicacao_entre_o_aplicativo_e_o_s),
            stringResource(R.string.cada_pedido_ao_servidor_exige_que_voce),
            stringResource(R.string.coletamos_o_minimo_necessario_o_texto),
            stringResource(R.string.a_analise_de_sms_whatsapp_e_mails_e_te),
            stringResource(R.string.ha_limite_de_uso_por_pessoa_para_evita)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_6_seus_direitos_lgpd),
        intro = stringResource(R.string.conforme_a_lei_geral_de_protecao_de_da),
        items = listOf(
            stringResource(R.string.confirmar_se_tratamos_seus_dados_e_ter),
            stringResource(R.string.corrigir_dados_incompletos_ou_desatual),
            stringResource(R.string.pedir_a_anonimizacao_o_bloqueio_ou_a_e),
            stringResource(R.string.pedir_a_portabilidade_dos_seus_dados_e),
            stringResource(R.string.revogar_este_consentimento_para_isso_d)
        )
    ),
    ConsentSection(
        title = stringResource(R.string.s_7_atualizacoes),
        intro = stringResource(R.string.podemos_atualizar_estes_termos_quando, CONSENT_VERSION)
    )
)

@Preview(showBackground = true, name = "Termos e privacidade")
@Composable
fun SentinelConsentScreenPreview() {
    SentinelTheme {
        SentinelConsentScreen(onBack = {}, onAccept = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelConsentScreen(onBack: () -> Unit, onAccept: (acceptedAt: String) -> Unit) {
    var agreed by rememberSaveable { mutableStateOf(false) }
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.termos_e_privacidade),
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
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Column(modifier = Modifier.navigationBarsPadding().padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Checkbox) { agreed = !agreed },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = agreed, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.li_e_concordo_com_os_termos_de_uso_e_c),
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAccept(Instant.now().toString()) },
                        enabled = agreed,
                        colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.aceitar_e_continuar), color = SentinelTextLight, fontSize = 15.sp)
                    }
                }
            }
        },
        containerColor = SentinelBackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.antes_de_criar_sua_conta_leia_como_o_s),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            consentSections().forEach { section ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = section.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelDarkBlueButton
                        )
                        section.intro?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(it, fontSize = 13.sp, color = Color.DarkGray)
                        }
                        section.items.forEach { item ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                Text(stringResource(R.string.bullet_symbol), fontSize = 13.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
