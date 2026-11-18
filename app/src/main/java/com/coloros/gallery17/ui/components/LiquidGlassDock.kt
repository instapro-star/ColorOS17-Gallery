package com.coloros.gallery17.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 120Hz Flagship Spring Spec
fun <T> colorOSSpring() = spring<T>(
    dampingRatio = 0.78f,
    stiffness = 420f
)

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val glassBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xCC1E222D),
                Color(0xB314171F)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xE6FFFFFF),
                Color(0xCCF4F6FA)
            )
        )
    }

    val rimBorder = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.06f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFCBD5E1).copy(alpha = 0.45f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.14f),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassBg)
            .border(1.2.dp, rimBorder, RoundedCornerShape(cornerRadius))
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(26.dp)
            )
        }
        content()
    }
}

data class DockTab(val title: String, val icon: ImageVector)

@Composable
fun ColorOSLiquidGlassDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        DockTab("Photos", Icons.Rounded.PhotoLibrary),
        DockTab("Albums", Icons.Rounded.FolderCopy),
        DockTab("Explore", Icons.Rounded.AutoAwesome)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidGlassSurface(
            modifier = Modifier.weight(1f).height(66.dp),
            cornerRadius = 33.dp,
            isDark = isDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.90f else if (isSelected) 1.04f else 1f,
                        animationSpec = colorOSSpring(),
                        label = "dockScale"
                    )

                    val pillBg = if (isSelected) {
                        if (isDark) Color(0xFF3B82F6).copy(alpha = 0.24f) else Color(0xFF0066FF).copy(alpha = 0.12f)
                    } else Color.Transparent

                    val contentColor = if (isSelected) {
                        if (isDark) Color(0xFF60A5FA) else Color(0xFF0066FF)
                    } else {
                        if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
                    }

                    Row(
                        modifier = Modifier
                            .scale(scale)
                            .clip(CircleShape)
                            .background(pillBg)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onTabSelected(index) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = tab.title,
                                color = contentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Floating Liquid Glass Search Orb (ColorOS 17 Signature)
        LiquidGlassSurface(
            modifier = Modifier
                .size(66.dp)
                .clickable { onSearchClick() },
            cornerRadius = 33.dp,
            isDark = isDark
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "AI Search",
                    tint = if (isDark) Color.White else Color(0xFF111318),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
