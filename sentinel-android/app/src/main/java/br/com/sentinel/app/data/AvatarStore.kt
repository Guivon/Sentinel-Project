package br.com.sentinel.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AvatarStore {

    private const val TAG = "AvatarStore"
    private const val MAX_SIZE = 512
    private const val JPEG_QUALITY = 90
    private const val GOOGLE_SIZE_PARAM = "=s96-c"
    private const val GOOGLE_SIZE_LARGE = "=s256-c"

    private fun file(context: Context): File? {
        val userId = AuthRepository.currentUserId() ?: return null
        return File(context.filesDir, "avatar_$userId.jpg")
    }

    fun hasPhoto(context: Context): Boolean = file(context)?.exists() == true

    fun load(context: Context): Bitmap? {
        val target = file(context) ?: return null
        if (!target.exists()) return null
        return BitmapFactory.decodeFile(target.path)
    }

    suspend fun saveFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeScaled(context, uri) ?: return@withContext false
            save(context, rotateByExif(context, uri, bitmap))
        } catch (e: Exception) {
            Log.w(TAG, "Could not save selected photo: ${e.message}")
            false
        }
    }

    suspend fun syncFromAccount(context: Context) {
        if (hasPhoto(context)) return
        val url = AuthRepository.currentAvatarUrl() ?: return
        if (!url.startsWith("https://")) return
        withContext(Dispatchers.IO) {
            try {
                val bytes = HttpClientProvider.client
                    .get(url.replace(GOOGLE_SIZE_PARAM, GOOGLE_SIZE_LARGE))
                    .readRawBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) save(context, scale(bitmap))
            } catch (e: Exception) {
                Log.w(TAG, "Could not download account photo: ${e.message}")
            }
        }
    }

    fun remove(context: Context) {
        file(context)?.delete()
    }

    private fun save(context: Context, bitmap: Bitmap): Boolean {
        val target = file(context) ?: return false
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        return true
    }

    private fun decodeScaled(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIZE * 2 || bounds.outHeight / sample > MAX_SIZE * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null
        return scale(decoded)
    }

    private fun scale(bitmap: Bitmap): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= MAX_SIZE) return bitmap
        val ratio = MAX_SIZE.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    }

    private fun rotateByExif(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
