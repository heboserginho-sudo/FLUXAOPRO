package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.model.InvoiceCustomization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class InvoiceCustomizationRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _customization = MutableStateFlow(loadCustomization())
    val customization: StateFlow<InvoiceCustomization> = _customization.asStateFlow()

    private fun loadCustomization(): InvoiceCustomization {
        val logoBase64 = prefs.getString(KEY_LOGO_BASE64, "") ?: ""
        val logoPath = prefs.getString(KEY_LOGO_PATH, "") ?: ""
        val accentColor = prefs.getString(KEY_ACCENT_COLOR, "#D4AF37") ?: "#D4AF37"
        val templateStyle = prefs.getString(KEY_TEMPLATE_STYLE, "MODERN") ?: "MODERN"
        val footerNotes = prefs.getString(KEY_FOOTER_NOTES, "Obrigado pela preferência! Documento processado por computador.") ?: "Obrigado pela preferência!"
        val showLogo = prefs.getBoolean(KEY_SHOW_LOGO, true)
        val showWatermark = prefs.getBoolean(KEY_SHOW_WATERMARK, true)
        val showQr = prefs.getBoolean(KEY_SHOW_QR, true)
        val bankInfo = prefs.getString(KEY_BANK_INFO, "BFA • IBAN: 0006 0000 43818047301 44") ?: ""

        return InvoiceCustomization(
            logoBase64 = logoBase64,
            logoLocalPath = logoPath,
            accentColorHex = accentColor,
            templateStyle = templateStyle,
            customFooterNotes = footerNotes,
            showLogoOnInvoice = showLogo,
            showWatermark = showWatermark,
            showQrCode = showQr,
            bankAccountInfo = bankInfo
        )
    }

    suspend fun saveLogoFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext false

            // Scale down if too large (max 500x500 to keep memory and HTML lightweight)
            val maxDim = 500
            val scaledBitmap = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                val targetW: Int
                val targetH: Int
                if (ratio > 1) {
                    targetW = maxDim
                    targetH = (maxDim / ratio).toInt().coerceAtLeast(1)
                } else {
                    targetH = maxDim
                    targetW = (maxDim * ratio).toInt().coerceAtLeast(1)
                }
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            // Save to internal file
            val logoFile = File(context.filesDir, "company_invoice_logo.png")
            FileOutputStream(logoFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }

            // Convert to Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            prefs.edit()
                .putString(KEY_LOGO_BASE64, base64String)
                .putString(KEY_LOGO_PATH, logoFile.absolutePath)
                .putBoolean(KEY_SHOW_LOGO, true)
                .apply()

            _customization.value = _customization.value.copy(
                logoBase64 = base64String,
                logoLocalPath = logoFile.absolutePath,
                showLogoOnInvoice = true
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeLogo() {
        try {
            val logoFile = File(context.filesDir, "company_invoice_logo.png")
            if (logoFile.exists()) {
                logoFile.delete()
            }
        } catch (ignored: Exception) {}

        prefs.edit()
            .remove(KEY_LOGO_BASE64)
            .remove(KEY_LOGO_PATH)
            .apply()

        _customization.value = _customization.value.copy(
            logoBase64 = "",
            logoLocalPath = ""
        )
    }

    fun updateCustomization(
        accentColorHex: String = _customization.value.accentColorHex,
        templateStyle: String = _customization.value.templateStyle,
        customFooterNotes: String = _customization.value.customFooterNotes,
        showLogoOnInvoice: Boolean = _customization.value.showLogoOnInvoice,
        showWatermark: Boolean = _customization.value.showWatermark,
        showQrCode: Boolean = _customization.value.showQrCode,
        bankAccountInfo: String = _customization.value.bankAccountInfo
    ) {
        prefs.edit()
            .putString(KEY_ACCENT_COLOR, accentColorHex)
            .putString(KEY_TEMPLATE_STYLE, templateStyle)
            .putString(KEY_FOOTER_NOTES, customFooterNotes)
            .putBoolean(KEY_SHOW_LOGO, showLogoOnInvoice)
            .putBoolean(KEY_SHOW_WATERMARK, showWatermark)
            .putBoolean(KEY_SHOW_QR, showQrCode)
            .putString(KEY_BANK_INFO, bankAccountInfo)
            .apply()

        _customization.value = _customization.value.copy(
            accentColorHex = accentColorHex,
            templateStyle = templateStyle,
            customFooterNotes = customFooterNotes,
            showLogoOnInvoice = showLogoOnInvoice,
            showWatermark = showWatermark,
            showQrCode = showQrCode,
            bankAccountInfo = bankAccountInfo
        )
    }

    companion object {
        private const val PREFS_NAME = "invoice_customization_prefs"
        private const val KEY_LOGO_BASE64 = "key_logo_base64"
        private const val KEY_LOGO_PATH = "key_logo_path"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_TEMPLATE_STYLE = "key_template_style"
        private const val KEY_FOOTER_NOTES = "key_footer_notes"
        private const val KEY_SHOW_LOGO = "key_show_logo"
        private const val KEY_SHOW_WATERMARK = "key_show_watermark"
        private const val KEY_SHOW_QR = "key_show_qr"
        private const val KEY_BANK_INFO = "key_bank_info"
    }
}
