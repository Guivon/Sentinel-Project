package br.com.sentinel.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard

enum class PermissionTutorial(
    @StringRes val titleRes: Int,
    @StringRes val tipRes: Int,
    @StringRes val actionRes: Int,
    val steps: List<MockStep>
) {
    NOTIFICATIONS(
        R.string.tutorial_notifications_title,
        R.string.tutorial_notifications_tip,
        R.string.tutorial_notifications_action,
        listOf(
            MockStep(
                R.string.mock_notif_caption_1,
                R.string.mock_notif_screen_title,
                listOf(R.string.mock_app_whatsapp, R.string.mock_sentinel, R.string.mock_app_chrome),
                1,
                MockKind.LIST
            ),
            MockStep(
                R.string.mock_notif_caption_2,
                R.string.mock_sentinel,
                listOf(R.string.mock_notif_row_allow),
                0,
                MockKind.TOGGLE
            ),
            MockStep(
                R.string.mock_notif_caption_3,
                R.string.mock_sentinel,
                listOf(R.string.mock_notif_dialog_text, R.string.mock_cancel, R.string.mock_ok),
                2,
                MockKind.DIALOG
            )
        )
    ),
    GMAIL(
        R.string.tutorial_gmail_title,
        R.string.tutorial_gmail_tip,
        R.string.tutorial_gmail_action,
        listOf(
            MockStep(
                R.string.mock_gmail_caption_1,
                R.string.mock_gmail_screen_title,
                listOf(R.string.mock_gmail_account, R.string.mock_gmail_add_account),
                0,
                MockKind.LIST
            ),
            MockStep(
                R.string.mock_gmail_caption_2,
                R.string.mock_sentinel,
                listOf(R.string.mock_gmail_dialog_text, R.string.mock_deny, R.string.mock_ok),
                2,
                MockKind.DIALOG
            )
        )
    ),
    SMS(
        R.string.tutorial_sms_title,
        R.string.tutorial_sms_tip,
        R.string.tutorial_sms_action,
        listOf(
            MockStep(
                R.string.mock_sms_caption_1,
                R.string.mock_sentinel,
                listOf(R.string.mock_sms_dialog_text, R.string.mock_deny, R.string.mock_ok),
                2,
                MockKind.DIALOG
            ),
            MockStep(
                R.string.mock_sms_caption_2,
                R.string.mock_sms_perm_screen_title,
                listOf(R.string.mock_sms_perm_row),
                0,
                MockKind.LIST
            )
        )
    ),
    CALLS(
        R.string.tutorial_calls_title,
        R.string.tutorial_calls_tip,
        R.string.tutorial_calls_action,
        listOf(
            MockStep(
                R.string.mock_calls_caption_1,
                R.string.mock_sentinel,
                listOf(R.string.mock_calls_dialog_text, R.string.mock_cancel, R.string.mock_calls_set_default),
                2,
                MockKind.DIALOG
            ),
            MockStep(
                R.string.mock_calls_caption_2,
                R.string.mock_sentinel,
                listOf(R.string.mock_calls_contacts_text, R.string.mock_deny, R.string.mock_ok),
                2,
                MockKind.DIALOG
            )
        )
    ),
    SCREEN(
        R.string.tutorial_screen_title,
        R.string.tutorial_screen_tip,
        R.string.tutorial_screen_action,
        listOf(
            MockStep(
                R.string.mock_screen_caption_1,
                R.string.mock_screen_title_accessibility,
                listOf(
                    R.string.mock_screen_row_talkback,
                    R.string.mock_screen_row_installed,
                    R.string.mock_screen_row_captions
                ),
                1,
                MockKind.LIST
            ),
            MockStep(
                R.string.mock_screen_caption_2,
                R.string.mock_screen_row_installed,
                listOf(R.string.mock_screen_row_other, R.string.mock_screen_row_service),
                1,
                MockKind.LIST
            ),
            MockStep(
                R.string.mock_screen_caption_3,
                R.string.mock_screen_row_service,
                listOf(R.string.mock_screen_row_use),
                0,
                MockKind.TOGGLE
            ),
            MockStep(
                R.string.mock_screen_caption_4,
                R.string.mock_screen_row_service,
                listOf(R.string.mock_screen_dialog_text, R.string.mock_cancel, R.string.mock_ok),
                2,
                MockKind.DIALOG
            )
        )
    )
}

@Composable
fun PermissionTutorialDialog(
    tutorial: PermissionTutorial,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorial.steps.size })

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(tutorial.titleRes)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HorizontalPager(state = pagerState) { page ->
                    val step = tutorial.steps[page]
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(
                                R.string.tutorial_step_counter,
                                page + 1,
                                tutorial.steps.size
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelLightBlueCard
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TutorialMockScreen(step)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(step.caption), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PagerDots(count = tutorial.steps.size, current = pagerState.currentPage)
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.tutorial_swipe_hint), fontSize = 11.sp, color = Color.Gray)
                Text(stringResource(R.string.tutorial_disclaimer), fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(tutorial.tipRes), fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(tutorial.actionRes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

@Composable
private fun PagerDots(count: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val color = if (index == current) SentinelLightBlueCard else Color.LightGray
            Row(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(8.dp)
                    .background(color, CircleShape)
            ) {}
        }
    }
}
