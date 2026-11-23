package br.com.sentinel.app.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object QrScanner {

    private const val TAG = "Sentinel"
    private const val MAX_PDF_PAGES = 3
    private const val PDF_RENDER_WIDTH = 1600
    private const val PIX_PREFIX = "000201"
    private const val PDF_MAGIC = "%PDF-"

    suspend fun findPix(context: Context, bytes: ByteArray): String? = try {
        if (String(bytes.copyOf(minOf(bytes.size, PDF_MAGIC.length)), Charsets.ISO_8859_1) == PDF_MAGIC) {
            scanPdf(context, bytes)
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { scanBitmap(it) }
        }
    } catch (e: Exception) {
        Log.w(TAG, "QR scan failed: ${e.message}")
        null
    }

    private suspend fun scanPdf(context: Context, bytes: ByteArray): String? {
        val file = File.createTempFile("pix", ".pdf", context.cacheDir)
        return try {
            file.writeBytes(bytes)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> scanPages(renderer) }
            }
        } finally {
            file.delete()
        }
    }

    private suspend fun scanPages(renderer: PdfRenderer): String? {
        for (index in 0 until minOf(renderer.pageCount, MAX_PDF_PAGES)) {
            val bitmap = renderPage(renderer, index)
            val found = scanBitmap(bitmap)
            if (found != null) return found
        }
        return null
    }

    private fun renderPage(renderer: PdfRenderer, index: Int): Bitmap = renderer.openPage(index).use { page ->
        val height = PDF_RENDER_WIDTH * page.height / page.width
        val bitmap = Bitmap.createBitmap(PDF_RENDER_WIDTH, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bitmap
    }

    private suspend fun scanBitmap(bitmap: Bitmap): String? = suspendCancellableCoroutine { continuation ->
        val scanner = BarcodeScanning.getClient()
        scanner.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { codes ->
                val pix = codes.firstNotNullOfOrNull { code -> pixOrNull(code) }
                continuation.resume(pix)
            }
            .addOnFailureListener {
                Log.w(TAG, "Barcode decoding failed: ${it.message}")
                continuation.resume(null)
            }
            .addOnCompleteListener { scanner.close() }
    }

    private fun pixOrNull(code: Barcode): String? = code.rawValue?.takeIf { it.startsWith(PIX_PREFIX) }
}
