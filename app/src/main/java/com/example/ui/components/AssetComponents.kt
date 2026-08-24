package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asset.GameAsset
import com.example.asset.SpritePixelMatrix
import com.example.audio.GameSoundSynthesizer

@Composable
fun PixelArtCanvas(
    pixelArt: SpritePixelMatrix?,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    if (pixelArt == null || pixelArt.pixels.isEmpty()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(primaryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
        }
        return
    }

    val width = pixelArt.width
    val height = pixelArt.height

    Canvas(modifier = modifier) {
        val cellW = size.width / width
        val cellH = size.height / height

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (idx in pixelArt.pixels.indices) {
                    val hex = pixelArt.pixels[idx]
                    if (hex != "#00000000" && hex.isNotBlank()) {
                        val color = try {
                            val cleanHex = hex.removePrefix("#")
                            if (cleanHex.length == 8) {
                                Color(cleanHex.toLong(16))
                            } else {
                                Color(0xFF000000 or cleanHex.toLong(16))
                            }
                        } catch (_: Exception) {
                            primaryColor
                        }

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x * cellW, y * cellH),
                            size = Size(cellW * 0.95f, cellH * 0.95f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier
            .height(28.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = listOf(phase1, phase3, phase2, phase1 * 0.9f, phase3 * 0.7f, phase2 * 1.1f, phase1)
        bars.forEach { heightScale ->
            val actualHeight = if (isPlaying) (24.dp * heightScale.coerceIn(0.15f, 1f)) else 6.dp
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(actualHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isPlaying) accentColor else accentColor.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
fun AudioPreviewPill(
    asset: GameAsset,
    onPlay: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(asset.primaryColorHex)
    val secondaryColor = Color(asset.secondaryColorHex)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B2E)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.5f), secondaryColor.copy(alpha = 0.5f)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onPlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = primaryColor
                        )
                    }
                }

                Column {
                    Text(
                        text = asset.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (asset.sfxType != null) "Sound FX • ${asset.properties["Waveform"] ?: "Synth"}"
                        else "BGM • ${asset.properties["BPM"] ?: "120"} BPM • Loop",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B0C0)
                    )
                }
            }

            AudioWaveformVisualizer(
                isPlaying = isPlaying,
                accentColor = primaryColor
            )
        }
    }
}
