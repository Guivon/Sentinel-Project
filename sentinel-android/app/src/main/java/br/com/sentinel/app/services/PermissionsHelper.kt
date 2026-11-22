package br.com.sentinel.app.services

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionsHelper {

    private const val TAG = "PermissionsHelper"

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false

            val expected = ComponentName(context, SentinelNotificationListener::class.java).flattenToString()
            enabledListeners.split(":").any { it == expected }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read permission state: ${e.message}")
            false
        }
    }

    fun isSmsPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isCallScreeningRoleHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val manager = context.getSystemService(RoleManager::class.java) ?: return false
        return manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val activeItems = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val expectedResult = ComponentName(context, SentinelAccessibilityService::class.java).flattenToString()
            activeItems.split(":").any { it.equals(expectedResult, ignoreCase = true) }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read permission state: ${e.message}")
            false
        }
    }

}
