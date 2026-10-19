package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.SurfaceElevated

@Composable
fun LogoGraphic(
    title: String,
    style: String,
    primaryColorHex: String,
    accentColorHex: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(primaryColorHex))
    } catch (e: Exception) {
        Color(0xFF00E5FF)
    }

    val accentColor = try {
        Color(android.graphics.Color.parseColor(accentColorHex))
    } catch (e: Exception) {
        Color(0xFFA855F7)
    }

    // Map known showcase projects to high-res studio renders
    val knownDrawable = when {
        title.contains("Apex", ignoreCase = true) -> R.drawable.logo_apex
        title.contains("Vortex", ignoreCase = true) || title.contains("Quantum", ignoreCase = true) -> R.drawable.logo_quantum
        title.contains("Shadow", ignoreCase = true) || title.contains("Tiger", ignoreCase = true) -> R.drawable.logo_tiger
        title.contains("Nova", ignoreCase = true) || title.contains("Synapse", ignoreCase = true) -> R.drawable.logo_nova
        else -> null
    }

    if (knownDrawable != null) {
        Image(
            painter = painterResource(id = knownDrawable),
            contentDescription = title,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // High-end procedural vector logo canvas rendering with dynamic glow and geometry
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            SurfaceElevated,
                            Color(0xFF0B0E14)
                        )
                    )
                )
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize(0.75f)) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                when {
                    style.contains("Emblem", ignoreCase = true) || style.contains("3D", ignoreCase = true) -> {
                        // 3D Isometric Crystal Cube / Hexagon
                        val hexPath = Path().apply {
                            moveTo(w * 0.5f, h * 0.1f)
                            lineTo(w * 0.85f, h * 0.3f)
                            lineTo(w * 0.85f, h * 0.7f)
                            lineTo(w * 0.5f, h * 0.9f)
                            lineTo(w * 0.15f, h * 0.7f)
                            lineTo(w * 0.15f, h * 0.3f)
                            close()
                        }
                        drawPath(
                            hexPath,
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor)),
                            style = Stroke(width = 6f)
                        )
                        // Inner facets
                        drawLine(
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor)),
                            start = Offset(w * 0.5f, h * 0.5f),
                            end = Offset(w * 0.5f, h * 0.1f),
                            strokeWidth = 4f
                        )
                        drawLine(
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor)),
                            start = Offset(w * 0.5f, h * 0.5f),
                            end = Offset(w * 0.85f, h * 0.7f),
                            strokeWidth = 4f
                        )
                        drawLine(
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor)),
                            start = Offset(w * 0.5f, h * 0.5f),
                            end = Offset(w * 0.15f, h * 0.7f),
                            strokeWidth = 4f
                        )
                    }
                    style.contains("Mascot", ignoreCase = true) -> {
                        // Cyber Shield and Crest
                        val shield = Path().apply {
                            moveTo(w * 0.5f, h * 0.1f)
                            lineTo(w * 0.85f, h * 0.25f)
                            lineTo(w * 0.8f, h * 0.65f)
                            lineTo(w * 0.5f, h * 0.9f)
                            lineTo(w * 0.2f, h * 0.65f)
                            lineTo(w * 0.15f, h * 0.25f)
                            close()
                        }
                        drawPath(
                            shield,
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor)),
                            style = Stroke(width = 6f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(listOf(primaryColor, Color.Transparent)),
                            radius = w * 0.22f,
                            center = center
                        )
                    }
                    style.contains("Monogram", ignoreCase = true) -> {
                        // Intertwined cyber dynamic ribbon
                        drawArc(
                            brush = Brush.sweepGradient(listOf(primaryColor, accentColor, primaryColor)),
                            startAngle = 45f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(w * 0.2f, h * 0.2f),
                            size = Size(w * 0.6f, h * 0.6f),
                            style = Stroke(width = 12f)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 12f,
                            center = Offset(w * 0.65f, h * 0.35f)
                        )
                    }
                    else -> {
                        // Minimalist Geometric Diamond Star
                        val star = Path().apply {
                            moveTo(w * 0.5f, h * 0.12f)
                            lineTo(w * 0.62f, h * 0.4f)
                            lineTo(w * 0.9f, h * 0.5f)
                            lineTo(w * 0.62f, h * 0.6f)
                            lineTo(w * 0.5f, h * 0.88f)
                            lineTo(w * 0.38f, h * 0.6f)
                            lineTo(w * 0.1f, h * 0.5f)
                            lineTo(w * 0.38f, h * 0.4f)
                            close()
                        }
                        drawPath(
                            star,
                            brush = Brush.linearGradient(listOf(primaryColor, accentColor))
                        )
                    }
                }
            }
        }
    }
}
