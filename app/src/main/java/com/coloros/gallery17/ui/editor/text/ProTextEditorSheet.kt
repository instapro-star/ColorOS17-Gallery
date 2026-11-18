package com.coloros.gallery.editor.text

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProTextEditorSheet(
    layer: TextOverlayLayer,
    onUpdate: (TextOverlayLayer) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val saved = ProTextEngine.importCustomFont(context, it, "custom_${System.currentTimeMillis()}.ttf")
            if (saved != null) onUpdate(layer.copy(fontName = "Custom Font", customFontPath = saved.absolutePath))
        }
    }
    val bgPalette = listOf(Color.Transparent, Color.Black, Color.White, Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFFFFB300))

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xD916181D)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Pro Text & Splice Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))) {
                Text("Apply", color = Color.White)
            }
        }
        OutlinedTextField(
            value = layer.text,
            onValueChange = { onUpdate(layer.copy(text = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter text...", color = Color.Gray) }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AssistChip(
                    onClick = { fontPicker.launch(arrayOf("*/*")) },
                    label = { Text("+ Import .TTF/.OTF", color = Color(0xFF00E5FF)) }
                )
            }
            items(ProTextEngine.builtInFonts.keys.toList()) { font ->
                FilterChip(selected = layer.fontName == font, onClick = { onUpdate(layer.copy(fontName = font, customFontPath = null)) }, label = { Text(font) })
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProTextEngine.glowingPresets) { p ->
                FilterChip(selected = layer.glowPreset?.id == p.id, onClick = {
                    onUpdate(layer.copy(glowPreset = p, textColor = p.textColor, backgroundColor = p.backgroundColor, backgroundOpacity = p.backgroundAlpha))
                }, label = { Text(p.name) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("BG:", color = Color.LightGray, fontSize = 12.sp)
            bgPalette.forEach { c ->
                Box(Modifier.size(26.dp).clip(CircleShape).background(if (c == Color.Transparent) Color.DarkGray else c)
                    .border(2.dp, if (layer.backgroundColor == c) Color(0xFF0A84FF) else Color.Gray, CircleShape)
                    .clickable { onUpdate(layer.copy(backgroundColor = c)) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("BG Opacity: ${(layer.backgroundOpacity * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(115.dp))
            Slider(value = layer.backgroundOpacity, onValueChange = { onUpdate(layer.copy(backgroundOpacity = it)) }, modifier = Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Text Opacity: ${(layer.textOpacity * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(115.dp))
            Slider(value = layer.textOpacity, onValueChange = { onUpdate(layer.copy(textOpacity = it)) }, valueRange = 0.1f..1f, modifier = Modifier.weight(1f))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SpliceBlendMode.values()) { m ->
                FilterChip(selected = layer.spliceMode == m, onClick = { onUpdate(layer.copy(spliceMode = m)) }, label = { Text(m.label) })
            }
        }
    }
}
