package br.com.sentinel.app.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import br.com.sentinel.app.R
import br.com.sentinel.app.data.SentinelApi

const val MAX_ATTACHMENT_BYTES = 4_000_000

val ATTACHMENT_MIME_TYPES = arrayOf("image/png", "image/jpeg", "image/gif", "image/webp", "application/pdf")

data class AttachedFile(val uri: Uri, val name: String)

fun describeAttachment(context: Context, uri: Uri): Pair<String, Long> {
    var name = context.getString(R.string.default_attachment_name)
    var size = -1L
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    }.onFailure { Log.w("AttachedFile", "Could not read attachment metadata: ${it.message}") }
    return name to size
}

fun readUpload(context: Context, file: AttachedFile): SentinelApi.UploadFile? {
    val bytes = runCatching {
        context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
    }.onFailure { Log.w("AttachedFile", "Could not read attachment: ${it.message}") }.getOrNull() ?: return null
    if (bytes.isEmpty() || bytes.size > MAX_ATTACHMENT_BYTES) return null
    return SentinelApi.UploadFile(file.name, bytes)
}
