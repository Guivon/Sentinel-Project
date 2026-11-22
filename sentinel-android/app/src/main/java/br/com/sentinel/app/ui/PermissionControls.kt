package br.com.sentinel.app.ui

import android.Manifest
import android.accounts.AccountManager
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import br.com.sentinel.app.R
import br.com.sentinel.app.data.GmailHelper
import br.com.sentinel.app.services.GmailScheduler
import br.com.sentinel.app.services.PermissionsHelper
import br.com.sentinel.app.services.SentinelAccessibilityService
import br.com.sentinel.app.ui.components.PermissionTutorial
import br.com.sentinel.app.ui.components.PermissionTutorialDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SERVICE_DISABLE_DELAY_MS = 600L

@Stable
class ProtectionPermissions(
    val notificationsEnabled: Boolean,
    val connectedEmail: String?,
    val screenActive: Boolean,
    val requestNotificationAccess: () -> Unit,
    val connectGmail: () -> Unit,
    val requestScreenReading: () -> Unit,
    val turnOffScreenReading: () -> Unit,
    val openNotificationSettings: () -> Unit,
    val callsActive: Boolean,
    val requestCallProtection: () -> Unit,
    val openCallSettings: () -> Unit,
    val smsActive: Boolean,
    val requestSms: () -> Unit,
    val openSmsSettings: () -> Unit
)

private class NotificationAccess(val active: Boolean, val open: () -> Unit)

private class GmailAccess(val email: String?, val open: () -> Unit)

private class ScreenAccess(val active: Boolean, val open: () -> Unit, val turnOff: () -> Unit)

private class CallAccess(val active: Boolean, val request: () -> Unit)

private class SmsAccess(val active: Boolean, val request: () -> Unit, val openSettings: () -> Unit)

@Composable
private fun rememberNotificationAccess(): NotificationAccess {
    val context = LocalContext.current
    var active by remember { mutableStateOf(PermissionsHelper.isNotificationListenerEnabled(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        active = PermissionsHelper.isNotificationListenerEnabled(context)
    }
    LaunchedEffect(Unit) { active = PermissionsHelper.isNotificationListenerEnabled(context) }
    return NotificationAccess(active) {
        launcher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

@Composable
private fun rememberGmailAccess(): GmailAccess {
    val context = LocalContext.current
    var email by remember { mutableStateOf(GmailHelper.connectedEmail(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val chosen = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (chosen != null) {
            GmailHelper.saveConnectedEmail(context, chosen)
            email = chosen
            GmailScheduler.schedule(context)
        }
    }
    return GmailAccess(email) {
        launcher.launch(
            AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
        )
    }
}

@Composable
private fun rememberScreenAccess(): ScreenAccess {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var active by remember { mutableStateOf(PermissionsHelper.isAccessibilityServiceEnabled(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        active = PermissionsHelper.isAccessibilityServiceEnabled(context)
    }
    LaunchedEffect(Unit) { active = PermissionsHelper.isAccessibilityServiceEnabled(context) }
    val open = { launcher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    val turnOff = {
        if (SentinelAccessibilityService.turnOff()) {
            scope.launch {
                delay(SERVICE_DISABLE_DELAY_MS)
                active = PermissionsHelper.isAccessibilityServiceEnabled(context)
            }
        } else {
            open()
        }
    }
    return ScreenAccess(active, { open() }, { turnOff() })
}

@Composable
private fun rememberCallAccess(): CallAccess {
    val context = LocalContext.current
    var active by remember { mutableStateOf(PermissionsHelper.isCallScreeningRoleHeld(context)) }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        active = PermissionsHelper.isCallScreeningRoleHeld(context)
        val contactsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (active && !contactsGranted) contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
    }
    LaunchedEffect(Unit) { active = PermissionsHelper.isCallScreeningRoleHeld(context) }
    return CallAccess(active) {
        val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else {
            null
        }
        if (manager != null) {
            launcher.launch(manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        } else {
            Toast.makeText(context, R.string.calls_unsupported, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun rememberSmsAccess(): SmsAccess {
    val context = LocalContext.current
    var active by remember { mutableStateOf(PermissionsHelper.isSmsPermissionGranted(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        active = PermissionsHelper.isSmsPermissionGranted(context)
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        active = PermissionsHelper.isSmsPermissionGranted(context)
    }
    LaunchedEffect(Unit) { active = PermissionsHelper.isSmsPermissionGranted(context) }
    return SmsAccess(
        active = active,
        request = { permissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
        openSettings = {
            settingsLauncher.launch(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
            )
        }
    )
}

@Composable
fun rememberProtectionPermissions(): ProtectionPermissions {
    val context = LocalContext.current
    val notifications = rememberNotificationAccess()
    val gmail = rememberGmailAccess()
    val screen = rememberScreenAccess()
    val calls = rememberCallAccess()
    val sms = rememberSmsAccess()
    var tutorial by remember { mutableStateOf<PermissionTutorial?>(null) }

    tutorial?.let { current ->
        PermissionTutorialDialog(
            tutorial = current,
            onDismiss = { tutorial = null },
            onConfirm = {
                tutorial = null
                when (current) {
                    PermissionTutorial.NOTIFICATIONS -> notifications.open()
                    PermissionTutorial.GMAIL -> gmail.open()
                    PermissionTutorial.SCREEN -> screen.open()
                    PermissionTutorial.CALLS -> calls.request()
                    PermissionTutorial.SMS -> sms.request()
                }
            }
        )
    }

    return ProtectionPermissions(
        notificationsEnabled = notifications.active,
        connectedEmail = gmail.email,
        screenActive = screen.active,
        requestNotificationAccess = { tutorial = PermissionTutorial.NOTIFICATIONS },
        connectGmail = { tutorial = PermissionTutorial.GMAIL },
        requestScreenReading = { tutorial = PermissionTutorial.SCREEN },
        turnOffScreenReading = screen.turnOff,
        openNotificationSettings = notifications.open,
        callsActive = calls.active,
        requestCallProtection = { tutorial = PermissionTutorial.CALLS },
        openCallSettings = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        },
        smsActive = sms.active,
        requestSms = { tutorial = PermissionTutorial.SMS },
        openSmsSettings = sms.openSettings
    )
}
