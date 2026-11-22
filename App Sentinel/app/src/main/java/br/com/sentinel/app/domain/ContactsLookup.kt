package br.com.sentinel.app.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

object ContactsLookup {

    private const val TAG = "Sentinel"

    fun isKnown(context: Context, number: String): Boolean? {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted || number.none { it.isDigit() }) return null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return try {
            context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Contact lookup failed: ${e.message}")
            null
        }
    }
}
