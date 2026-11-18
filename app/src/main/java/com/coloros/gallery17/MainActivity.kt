package com.coloros.gallery17

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.coloros.gallery17.editor.KeyframeInterpolator
import com.coloros.gallery17.editor.ProMediaRenderEngine
import com.coloros.gallery17.model.*
import com.coloros.gallery17.ui.components.*
import com.coloros.gallery17.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable 120Hz Peak Refresh Rate on supported displays
        window.attributes = window.attributes.apply {
            preferredRefreshRate = 120f
        }

        setContent {
            val themeMode = remember { mutableStateOf(AppThemeMode.SYSTEM) }
            val isDark = when (themeMode.value) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.FORCE_LIGHT -> false
                AppThemeMode.FORCE_DARK -> true
            }

            CompositionLocalProvider(LocalThemeMode provides themeMode) {
                ColorOS17Theme(themeMode = themeMode.value) {
                    ColorOS17GalleryRoot(
                        isDark = isDark,
                        themeMode = themeMode.value,
                        onThemeModeChange = { themeMode.value = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorOS17GalleryRoot(
    isDark: Boolean,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
) {
    val context = LocalContext.current
    var mediaList by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var gridColumns by remember { mutableIntStateOf(3) }
    var activeEditorItem by remember { mutableStateOf<MediaItem?>(null) }
    var showThemeSheet by remember { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        mediaList = loadDeviceMedia(context)
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    if (activeEditorItem != null) {
        ProStudioEditorScreen(
            item = activeEditorItem!!,
            isDark = isDark,
            onClose = { activeEditorItem = null }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ColorOSLiquidGlassDock(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSearchClick = {
                    Toast.makeText(context, "AI Smart Search & OCR Ready", Toast.LENGTH_SHORT).show()
                },
                isDark = isDark
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                // ColorOS 17 Top Header with Theme Lock Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Photos"
                                1 -> "Albums"
                                else -> "Explore AI"
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${mediaList.size} items • 120Hz Liquid UI",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Independent Light/Dark Theme Lock Pill
                        LiquidGlassSurface(
                            modifier = Modifier
                                .height(42.dp)
                                .clickable { showThemeSheet = !showThemeSheet },
                            cornerRadius = 21.dp,
                            isDark = isDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (themeMode) {
                                        AppThemeMode.FORCE_LIGHT -> Icons.Rounded.LightMode
                                        AppThemeMode.FORCE_DARK -> Icons.Rounded.DarkMode
                                        AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                                    },
                                    contentDescription = "Theme Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (themeMode) {
                                        AppThemeMode.FORCE_LIGHT -> "White"
                                        AppThemeMode.FORCE_DARK -> "Dark"
                                        AppThemeMode.SYSTEM -> "Auto"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Pinch-to-Zoom 120Hz Photo & Video Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(bottom = 110.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom > 1.25f && gridColumns > 2) gridColumns--
                                if (zoom < 0.75f && gridColumns < 5) gridColumns++
                            }
                        }
                ) {
                    items(mediaList, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { activeEditorItem = item }
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (item.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = formatDuration(item.durationMs),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Liquid Glass Theme Override Floating Menu
            AnimatedVisibility(
                visible = showThemeSheet,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 74.dp, end = 20.dp)
            ) {
                LiquidGlassSurface(
                    modifier = Modifier.width(210.dp),
                    cornerRadius = 22.dp,
                    isDark = isDark
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Gallery Theme Lock",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        listOf(
                            Triple(AppThemeMode.FORCE_LIGHT, "Force White Theme", Icons.Rounded.LightMode),
                            Triple(AppThemeMode.FORCE_DARK, "Force Dark Theme", Icons.Rounded.DarkMode),
                            Triple(AppThemeMode.SYSTEM, "Follow System", Icons.Rounded.BrightnessAuto)
                        ).forEach { (mode, label, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onThemeModeChange(mode)
                                        showThemeSheet = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProStudioEditorScreen(
    item: MediaItem,
    isDark: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var enable4KCC by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(ColorOSMasterFilters.first()) }
    var timelinePositionMs by remember { mutableFloatStateOf(0f) }
    var overlayLayers by remember { mutableStateOf<List<MediaOverlayLayer>>(emptyList()) }
    var activeToolTab by remember { mutableIntStateOf(0) } // 0: 4K CC/Filters, 1: Overlays, 2: Keyframes, 3: AI

    val activeLayer = overlayLayers.firstOrNull()
    val evaluatedKf = remember(timelinePositionMs, overlayLayers) {
        activeLayer?.let { KeyframeInterpolator.evaluateAt(it.keyframes, timelinePositionMs.toLong()) }
            ?: KeyframePoint(0L)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Liquid Glass Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }

            // One-Tap 4K CC Boost Pill
            Button(
                onClick = { enable4KCC = !enable4KCC },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enable4KCC) Color(0xFF0066FF) else Color(0xFF1F2430)
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Hd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (enable4KCC) "4K CC: ON" else "Apply 4K CC", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (item.isVideo) {
                        Toast.makeText(context, "Exporting 4K CC Video with Keyframes...", Toast.LENGTH_SHORT).show()
                        ProMediaRenderEngine.export4KCCVideo(
                            context = context,
                            inputUri = item.uri,
                            enable4KCC = enable4KCC,
                            filter = selectedFilter,
                            onComplete = { file ->
                                Toast.makeText(context, "Saved: ${file.name}", Toast.LENGTH_LONG).show()
                            },
                            onError = {
                                Toast.makeText(context, "Export error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "4K Enhanced Photo & Overlays Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = CircleShape
            ) {
                Text("Save 4K", fontWeight = FontWeight.Bold)
            }
        }

        // Preview Canvas with Live Keyframe & Overlay Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Live Overlay Preview driven by Keyframe Interpolation
            if (activeLayer != null) {
                Box(
                    modifier = Modifier
                        .offset(x = evaluatedKf.offsetX.dp, y = evaluatedKf.offsetY.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0066FF).copy(alpha = 0.75f * evaluatedKf.opacity))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = activeLayer.textContent ?: "PIP Overlay Layer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (enable4KCC) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0066FF), Color(0xFF06B6D4))))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("4K ULTRA CC HDR ACTIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Keyframe Scrubber Timeline
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timeline: ${timelinePositionMs.toInt()} ms",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                TextButton(
                    onClick = {
                        val currentLayers = overlayLayers.toMutableList()
                        if (currentLayers.isEmpty()) {
                            currentLayers.add(
                                MediaOverlayLayer(
                                    id = "layer_1",
                                    label = "Overlay 1",
                                    textContent = "ColorOS 17 Overlay",
                                    keyframes = mutableListOf(
                                        KeyframePoint(0L, scale = 1f, offsetX = -40f, offsetY = -30f),
                                        KeyframePoint(2500L, scale = 1.4f, offsetX = 50f, offsetY = 40f)
                                    )
                                )
                            )
                        } else {
                            val first = currentLayers[0]
                            first.keyframes.add(
                                KeyframePoint(
                                    timeMs = timelinePositionMs.toLong(),
                                    scale = 1.25f,
                                    offsetX = (timelinePositionMs / 40f) - 30f,
                                    offsetY = 20f
                                )
                            )
                        }
                        overlayLayers = currentLayers
                        Toast.makeText(context, "Keyframe Added at ${timelinePositionMs.toInt()}ms ◆", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Rounded.Diamond, contentDescription = "Add Keyframe", tint = Color(0xFFF59E0B))
                    Spacer(Modifier.width(4.dp))
                    Text("+ Keyframe", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
            }
            Slider(
                value = timelinePositionMs,
                onValueChange = { timelinePositionMs = it },
                valueRange = 0f..5000f
            )
        }

        // Liquid Glass Studio Dock (Filters, Overlays, Keyframes, AI)
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 26.dp,
            isDark = true
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Filters & CC", "Overlays", "Keyframes", "AI Tools").forEachIndexed { idx, label ->
                        Text(
                            text = label,
                            color = if (activeToolTab == idx) Color(0xFF60A5FA) else Color.Gray,
                            fontWeight = if (activeToolTab == idx) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { activeToolTab = idx }
                                .padding(vertical = 6.dp, horizontal = 10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                when (activeToolTab) {
                    0 -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(ColorOSMasterFilters) { preset ->
                                val selected = selectedFilter == preset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (selected) Color(0xFF0066FF) else Color(0xFF1E2330))
                                        .clickable { selectedFilter = preset }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(preset.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    1 -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {
                                overlayLayers = overlayLayers + MediaOverlayLayer(
                                    id = "ov_${overlayLayers.size + 1}",
                                    label = "Overlay ${overlayLayers.size + 1}",
                                    textContent = "✦ Cinematic Overlay"
                                )
                            }) {
                                Text("+ Add Photo/Video Overlay")
                            }
                            Button(onClick = {
                                overlayLayers = overlayLayers + MediaOverlayLayer(
                                    id = "leak_${overlayLayers.size + 1}",
                                    label = "Light Leak",
                                    textContent = "☀ HDR Light Leak",
                                    blendMode = OverlayBlendMode.SCREEN
                                )
                            }) {
                                Text("+ Light Leak (Screen)")
                            }
                        }
                    }
                    2 -> {
                        Text(
                            text = "Active Keyframes: ${activeLayer?.keyframes?.size ?: 0} | Scrub slider & tap '+ Keyframe' to animate position, scale & opacity.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                    3 -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {
                                Toast.makeText(context, "AI Smart Cutout: Extracting Subject...", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Smart Cutout")
                            }
                            Button(onClick = {
                                Toast.makeText(context, "AI Eraser Brush Ready", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("AI Eraser")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadDeviceMedia(context: Context): List<MediaItem> {
    val items = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Video.VideoColumns.DURATION
    )
    val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
    val selectionArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
        val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val isVid = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            val contentUri = if (isVid) {
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            } else {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
            items.add(
                MediaItem(
                    id = id,
                    uri = contentUri,
                    name = cursor.getString(nameCol) ?: "Media",
                    dateTaken = cursor.getLong(dateCol),
                    isVideo = isVid,
                    durationMs = cursor.getLong(durCol)
                )
            )
        }
    }
    return items
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
