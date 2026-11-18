package com.coloros.gallery.editor.text

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

enum class SpliceBlendMode(val label: String, val composeBlend: BlendMode) {
    NORMAL("Normal", BlendMode.SrcOver),
    OVERLAY("Overlay", BlendMode.Overlay),
    SCREEN("Screen", BlendMode.Screen),
    MULTIPLY("Multiply", BlendMode.Multiply),
    SOFT_LIGHT("Soft Light", BlendMode.Softlight),
    HARD_LIGHT("Hard Light", BlendMode.Hardlight),
    COLOR_DODGE("Color Dodge", BlendMode.ColorDodge)
}

data class GlowingTextPreset(
    val id: String,
    val name: String,
    val textColor: Color,
    val glowColor: Color,
    val glowRadius: Float,
    val backgroundColor: Color,
    val backgroundAlpha: Float
)

data class TextOverlayLayer(
    val id: String,
    val text: String,
    val fontName: String = "ColorOS Sans",
    val customFontPath: String? = null,
    val fontSizeSp: Float = 28f,
    val textColor: Color = Color.White,
    val backgroundColor: Color = Color.Black,
    val backgroundOpacity: Float = 0.45f,
    val textOpacity: Float = 1.0f,
    val spliceMode: SpliceBlendMode = SpliceBlendMode.NORMAL,
    val glowPreset: GlowingTextPreset? = null
)

object ProTextEngine {
    val glowingPresets = listOf(
        GlowingTextPreset("none", "Clean Minimal", Color.White, Color.Transparent, 0f, Color.Black, 0.35f),
        GlowingTextPreset("neon_cyan", "Neon Cyan", Color(0xFFE0FFFF), Color(0xFF00E5FF), 24f, Color(0xFF001F29), 0.55f),
        GlowingTextPreset("warm_gold", "Royal Gold", Color(0xFFFFF8E1), Color(0xFFFFB300), 22f, Color(0xFF261A00), 0.50f),
        GlowingTextPreset("cyber_pink", "Cyberpunk Pink", Color(0xFFFFE4F2), Color(0xFFFF1744), 26f, Color(0xFF2A0010), 0.55f),
        GlowingTextPreset("emerald", "ColorOS Emerald", Color(0xFFE8F5E9), Color(0xFF00E676), 20f, Color(0xFF002411), 0.45f)
    )

    val builtInFonts: Map<String, FontFamily> = mapOf(
        "ColorOS Sans" to FontFamily.SansSerif,
        "Editorial Serif" to FontFamily.Serif,
        "Cinema Mono" to FontFamily.Monospace,
        "Modern Display" to FontFamily.Cursive
    )

    fun importCustomFont(context: Context, fontUri: Uri, fileName: String): File? {
        return try {
            val fontsDir = File(context.filesDir, "custom_fonts").apply { mkdirs() }
            val destFile = File(fontsDir, fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_"))
            context.contentResolver.openInputStream(fontUri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            destFile
        } catch (e: Exception) { null }
    }

    fun buildComposeTextStyle(layer: TextOverlayLayer): TextStyle {
        val family = if (!layer.customFontPath.isNullOrBlank() && File(layer.customFontPath).exists()) {
            FontFamily(Typeface.createFromFile(layer.customFontPath))
        } else {
            builtInFonts[layer.fontName] ?: FontFamily.SansSerif
        }
        val shadow = layer.glowPreset?.takeIf { it.glowRadius > 0f }?.let {
            Shadow(color = it.glowColor, offset = Offset.Zero, blurRadius = it.glowRadius)
        }
        return TextStyle(
            color = layer.textColor.copy(alpha = layer.textOpacity),
            fontSize = layer.fontSizeSp.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = family,
            shadow = shadow
        )
    }
}
