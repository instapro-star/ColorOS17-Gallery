package com.coloros.gallery17.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateTaken: Long,
    val isVideo: Boolean,
    val durationMs: Long = 0L,
    val bucketName: String = "Camera"
)

data class KeyframePoint(
    val timeMs: Long,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
)

enum class OverlayBlendMode(val label: String) {
    NORMAL("Normal"),
    SCREEN("Screen (Light Leak)"),
    OVERLAY("Overlay"),
    MULTIPLY("Multiply"),
    LIGHTEN("Lighten")
}

data class MediaOverlayLayer(
    val id: String,
    val label: String,
    val overlayUri: Uri? = null,
    val textContent: String? = null,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 5000L,
    val blendMode: OverlayBlendMode = OverlayBlendMode.NORMAL,
    val keyframes: MutableList<KeyframePoint> = mutableListOf(
        KeyframePoint(timeMs = 0L, scale = 1f, offsetX = 0f, offsetY = 0f, rotation = 0f, opacity = 1f)
    )
)

data class FilterPreset(
    val name: String,
    val contrast: Float,
    val saturation: Float,
    val brightness: Float,
    val warmth: Float,
    val sharpness: Float
)

val ColorOSMasterFilters = listOf(
    FilterPreset("Original", 1.0f, 1.0f, 0.0f, 0.0f, 0.0f),
    FilterPreset("4K CC Pro", 1.24f, 1.22f, 0.04f, 0.08f, 0.85f),
    FilterPreset("Emerald", 1.12f, 1.18f, 0.02f, -0.06f, 0.25f),
    FilterPreset("Serenity", 0.96f, 0.88f, 0.05f, -0.10f, 0.15f),
    FilterPreset("Radiance", 1.15f, 1.25f, 0.06f, 0.15f, 0.30f),
    FilterPreset("Hasselblad XPAN", 1.30f, 0.0f, -0.02f, 0.0f, 0.45f),
    FilterPreset("Cyber Neon", 1.28f, 1.38f, 0.0f, -0.18f, 0.50f)
)
