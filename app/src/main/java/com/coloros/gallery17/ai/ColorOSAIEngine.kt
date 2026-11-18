package com.coloros.gallery17.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object ColorOSAIEngine {

    /**
     * ColorOS 17 Smart Cutout: Long-press subject to lift & extract PNG sticker
     */
    fun extractSmartCutout(
        bitmap: Bitmap,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        val segmenter = SubjectSegmentation.getClient(options)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                val foreground = result.foregroundBitmap
                if (foreground != null) {
                    onSuccess(foreground)
                } else {
                    onFailure(IllegalStateException("No main subject detected"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * ColorOS 17 AI Eraser (Smart Patch Inpainting around brushed mask)
     */
    fun applyAIEraserPatch(source: Bitmap, centerX: Float, centerY: Float, radius: Float): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val sampleX = (centerX - radius * 1.4f).toInt().coerceIn(0, source.width - 1)
        val sampleY = centerY.toInt().coerceIn(0, source.height - 1)
        val sampleColor = source.getPixel(sampleX, sampleY)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sampleColor
            maskFilter = android.graphics.BlurMaskFilter(radius * 0.45f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(centerX, centerY, radius, paint)
        return result
    }

    /**
     * Live OCR Text Extraction from Photos
     */
    fun extractTextFromImage(bitmap: Bitmap, onResult: (String) -> Unit) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onResult(visionText.text)
            }
            .addOnFailureListener {
                onResult("")
            }
    }
}
