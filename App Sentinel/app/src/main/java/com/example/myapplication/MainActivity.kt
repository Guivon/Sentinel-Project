package com.example.myapplication

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificacaoHelper.criarCanal(this)
        SentinelState.inicializar(this)

        setContent {
            MyApplicationTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                val smsPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                val contatosPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    contatosPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }

                SentinelApp()
            }
        }
    }
}