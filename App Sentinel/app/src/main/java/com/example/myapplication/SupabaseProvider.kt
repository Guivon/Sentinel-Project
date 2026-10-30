package com.example.myapplication

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://rfeofvssjntsxrdvoidu.supabase.co",
        supabaseKey = "sb_publishable_YM30b62OM6vMB2iTu_eKnQ_UuZKwgSt"
    ) {
        install(Auth)
        install(Postgrest)
    }
}