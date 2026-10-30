package com.example.myapplication

import android.app.Application

class SentinelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SentinelState.inicializar(this)
        NotificacaoHelper.criarCanal(this)
    }
}
