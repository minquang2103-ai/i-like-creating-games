package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fort
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.community.CommunityManager
import com.example.engine.GameEngine
import com.example.engine.GameState
import com.example.model.EntityType
import com.example.model.GameGenre
import com.example.model.GameProject
import com.example.model.TileType
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GamePlayView(
    project: GameProject,
    onExitPlay: () -> Unit,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember(project) { GameEngine(project) }
    val gameState by engine.gameState.collectAsState()

    var moveX by remember { mutableFloatStateOf(0f) }
    var moveY by remember { mutableFloatStateOf(0f) }
    var playerNameInput by remember { mutableStateOf("ArcadeHero") }
    var hasRecordedScore by remember { mutableStateOf(false) }

    // Game loop tick
    LaunchedEffect(engine) {
        var lastTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now
                engine.update(dt, moveX, moveY)
            }
        }
    }

    val theme = project.theme
    val primaryColor = Color(theme.primaryColor)
    val accentColor = Color(theme.accentColor)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(theme.bgGradientStart))
    ) {
        // Game Canvas World
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileSize = (size.width / engine.levelWidth).coerceAtLeast(18f)
            engine.tileSizePx = tileSize

            // Background Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(theme.bgGradientStart), Color(theme.bgGradientEnd))
                )
            )

            // Screen Shake offset
            val shakeOffset = if (gameState.screenShakeAmount > 0) {
                Offset(
                    (Math.random() * 16 - 8).toFloat() * gameState.screenShakeAmount,
                    (Math.random() * 16 - 8).toFloat() * gameState.screenShakeAmount
                )
            } else Offset.Zero

            // Render Tiles
            for (y in engine.activeGrid.indices) {
                for (x in engine.activeGrid[y].indices) {
                    val tile = engine.activeGrid[y][x]
                    val drawX = x * tileSize + shakeOffset.x
                    val drawY = y * tileSize + shakeOffset.y

                    drawGameTile(tile, drawX, drawY, tileSize, theme)
                }
            }

            // Render Towers
            for (tower in engine.towers) {
                drawCircle(
                    color = Color(tower.colorHex),
                    radius = tileSize * 0.45f,
                    center = Offset(tower.x + shakeOffset.x, tower.y + shakeOffset.y)
                )
                // Range circle subtle
                drawCircle(
                    color = Color(tower.colorHex).copy(alpha = 0.12f),
                    radius = tower.range,
                    center = Offset(tower.x + shakeOffset.x, tower.y + shakeOffset.y)
                )
            }

            // Render Entities (Enemies, Boss, NPCs)
            for (entity in engine.runtimeEntities.filter { it.isAlive }) {
                val ex = entity.x * tileSize + shakeOffset.x
                val ey = entity.y * tileSize + shakeOffset.y

                when (entity.type) {
                    EntityType.ENEMY_BOSS -> {
                        drawRoundRect(
                            color = Color(0xFFFF0055),
                            topLeft = Offset(ex, ey - tileSize * 0.5f),
                            size = Size(tileSize * 1.5f, tileSize * 1.5f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        // Boss Eye
                        drawCircle(
                            color = Color.Yellow,
                            radius = tileSize * 0.2f,
                            center = Offset(ex + tileSize * 0.75f, ey + tileSize * 0.1f)
                        )
                    }
                    EntityType.ENEMY_PATROL, EntityType.ENEMY_BOUNCER -> {
                        drawCircle(
                            color = Color(0xFFFF3366),
                            radius = tileSize * 0.4f,
                            center = Offset(ex + tileSize * 0.5f, ey + tileSize * 0.5f)
                        )
                    }
                    EntityType.ENEMY_SHOOTER -> {
                        drawRoundRect(
                            color = Color(0xFFFF7700),
                            topLeft = Offset(ex + tileSize * 0.1f, ey + tileSize * 0.1f),
                            size = Size(tileSize * 0.8f, tileSize * 0.8f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                    EntityType.NPC_GUIDE -> {
                        drawRoundRect(
                            color = Color(0xFF9B5DE5),
                            topLeft = Offset(ex + tileSize * 0.1f, ey + tileSize * 0.1f),
                            size = Size(tileSize * 0.8f, tileSize * 0.8f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Speech bubble indicator
                        drawCircle(
                            color = Color.White,
                            radius = tileSize * 0.2f,
                            center = Offset(ex + tileSize * 0.5f, ey - tileSize * 0.3f)
                        )
                    }
                    else -> {}
                }
            }

            // Breakout Paddle & Ball
            if (project.genre == GameGenre.BRICK_BREAKER) {
                // Paddle
                val paddleY = (engine.levelHeight - 2) * tileSize + shakeOffset.y
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(engine.paddleX - engine.paddleWidth / 2 + shakeOffset.x, paddleY),
                    size = Size(engine.paddleWidth, tileSize * 0.5f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Ball
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = Offset(engine.ballX + shakeOffset.x, engine.ballY + shakeOffset.y)
                )
            }

            // Render Player
            if (project.genre != GameGenre.BRICK_BREAKER) {
                val px = gameState.playerX * tileSize + shakeOffset.x
                val py = gameState.playerY * tileSize + shakeOffset.y

                // Body
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(px + tileSize * 0.1f, py + tileSize * 0.1f),
                    size = Size(tileSize * 0.8f, tileSize * 0.9f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Visor / Face
                val eyeX = if (gameState.facingRight) px + tileSize * 0.55f else px + tileSize * 0.25f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(eyeX, py + tileSize * 0.25f),
                    size = Size(tileSize * 0.3f, tileSize * 0.2f),
                    cornerRadius = CornerRadius(2f, 2f)
                )

                // Attack Slash Glow
                if (gameState.isAttacking) {
                    val slashDir = if (gameState.facingRight) 1f else -1f
                    drawCircle(
                        color = Color(0xFFFF0055).copy(alpha = 0.6f),
                        radius = tileSize * 0.7f,
                        center = Offset(px + tileSize * 0.5f + slashDir * tileSize * 0.6f, py + tileSize * 0.5f)
                    )
                }
            }

            // Render Projectiles
            for (p in engine.projectiles) {
                drawCircle(
                    color = Color(p.colorHex),
                    radius = 6f,
                    center = Offset(p.x + shakeOffset.x, p.y + shakeOffset.y)
                )
            }

            // Render Particles
            for (pt in engine.particles) {
                drawCircle(
                    color = Color(pt.colorHex).copy(alpha = pt.alpha),
                    radius = pt.size,
                    center = Offset(pt.x + shakeOffset.x, pt.y + shakeOffset.y)
                )
            }
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit Button & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14102C).copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = onExitPlay) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(text = project.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(
                        text = "Score: ${gameState.score} • Time: ${gameState.levelTimeSeconds.toInt()}s",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            // Health & Keys
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Heart Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF14102C).copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF0055), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gameState.playerHealth} / ${gameState.playerMaxHealth}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Gold Key indicator if held
                if (gameState.goldKeys > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "${gameState.goldKeys}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // Restart Quick Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14102C).copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = { engine.resetLevel(gameState.currentLevelIndex) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Active Dialogue Popup Toast
        gameState.activeDialogue?.let { dialogue ->
            Surface(
                color = Color(0xFF1B1638).copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00F0FF))),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📜 Quest Guide", fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF), fontSize = 14.sp)
                        IconButton(onClick = { engine.dismissDialogue() }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = dialogue, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { engine.dismissDialogue() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Continue", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Virtual On-Screen Controls at Bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Left: D-Pad Directional Controls
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomStart)
            ) {
                // Left Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E183D).copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(46.dp)
                        .align(Alignment.CenterStart)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { moveX = -1f },
                                onDragEnd = { moveX = 0f },
                                onDragCancel = { moveX = 0f },
                                onDrag = { _, _ -> }
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
                    }
                }

                // Right Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E183D).copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(46.dp)
                        .align(Alignment.CenterEnd)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { moveX = 1f },
                                onDragEnd = { moveX = 0f },
                                onDragCancel = { moveX = 0f },
                                onDrag = { _, _ -> }
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
                    }
                }

                // Up Button (for RPG / Stealth)
                if (project.genre != GameGenre.PLATFORMER && project.genre != GameGenre.BRICK_BREAKER) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E183D).copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.TopCenter)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { moveY = -1f },
                                    onDragEnd = { moveY = 0f },
                                    onDragCancel = { moveY = 0f },
                                    onDrag = { _, _ -> }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White)
                        }
                    }

                    // Down Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E183D).copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.BottomCenter)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { moveY = 1f },
                                    onDragEnd = { moveY = 0f },
                                    onDragCancel = { moveY = 0f },
                                    onDrag = { _, _ -> }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White)
                        }
                    }
                }
            }

            // Right: Action Buttons (Jump / Attack / Build Tower)
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tower placement button for Tower Defense
                if (project.genre == GameGenre.TOWER_DEFENSE) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                engine.placeTower(
                                    (1..engine.levelWidth - 3).random(),
                                    (1..engine.levelHeight - 3).random()
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Fort, contentDescription = "Place Tower", tint = Color(0xFF0F0C20), modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // Attack Button
                if (project.config.hasAttack && project.genre != GameGenre.BRICK_BREAKER) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF0055),
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { engine.attack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Attack", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // Jump Button for Platformer
                if (project.genre == GameGenre.PLATFORMER) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor,
                        modifier = Modifier
                            .size(62.dp)
                            .clickable { engine.jump() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Jump", tint = Color(0xFF0F0C20), modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }
        }

        // Game Over Overlay Modal
        AnimatedVisibility(
            visible = gameState.isGameOver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0818).copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1432)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF0055))),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💀 GAME OVER", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFF0055))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Final Score: ${gameState.score} pts", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onExitPlay, shape = RoundedCornerShape(10.dp)) {
                                Text("Back to Hub", color = Color.White)
                            }
                            Button(
                                onClick = { engine.resetLevel(gameState.currentLevelIndex) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Victory Overlay Modal
        AnimatedVisibility(
            visible = gameState.isGameWon,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0818).copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1536)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFD700))),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏆 VICTORY ACHIEVED!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Level Completed in ${gameState.levelTimeSeconds.toInt()} seconds", color = Color(0xFFA0A0BA), fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Star ratings
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(3) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Total Score: ${gameState.score} PTS", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF00F0FF))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit to Leaderboard form
                        if (!hasRecordedScore) {
                            OutlinedTextField(
                                value = playerNameInput,
                                onValueChange = { playerNameInput = it },
                                label = { Text("Gamer Tag for Hall of Fame", color = Color(0xFF9E9EB8)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    CommunityManager.recordPlayAndScore(
                                        gameId = project.id,
                                        playerName = playerNameInput,
                                        score = gameState.score,
                                        timeSeconds = gameState.levelTimeSeconds.toInt()
                                    )
                                    hasRecordedScore = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit to Community Leaderboard", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00FF88).copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ Highscore saved to Community Leaderboard!",
                                    color = Color(0xFF00FF88),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = onExitPlay, shape = RoundedCornerShape(10.dp)) {
                                Text("Exit", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    hasRecordedScore = false
                                    engine.resetLevel(gameState.currentLevelIndex)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Play Again", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawGameTile(
    tile: TileType,
    x: Float,
    y: Float,
    tileSize: Float,
    theme: com.example.model.GameTheme
) {
    val wallColor = Color(theme.wallColor)
    val primaryColor = Color(theme.primaryColor)
    val accentColor = Color(theme.accentColor)

    when (tile) {
        TileType.SOLID -> {
            drawRoundRect(
                color = wallColor,
                topLeft = Offset(x, y),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Accent top highlight
            drawRect(
                color = primaryColor.copy(alpha = 0.6f),
                topLeft = Offset(x, y),
                size = Size(tileSize, 3f)
            )
        }
        TileType.PLATFORM -> {
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(tileSize, tileSize * 0.3f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        TileType.COIN -> {
            drawCircle(
                color = Color(0xFFFFD700),
                radius = tileSize * 0.35f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
            drawCircle(
                color = Color(0xFFFFF099),
                radius = tileSize * 0.18f,
                center = Offset(x + tileSize * 0.45f, y + tileSize * 0.45f)
            )
        }
        TileType.GEM -> {
            drawCircle(
                color = Color(0xFF00FFFF),
                radius = tileSize * 0.38f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
        }
        TileType.SPIKES -> {
            val path = Path().apply {
                moveTo(x, y + tileSize)
                lineTo(x + tileSize * 0.5f, y + tileSize * 0.2f)
                lineTo(x + tileSize, y + tileSize)
                close()
            }
            drawPath(path = path, color = Color(theme.hazardColor))
        }
        TileType.LAVA -> {
            drawRect(
                color = Color(0xFFFF5400),
                topLeft = Offset(x, y + tileSize * 0.3f),
                size = Size(tileSize, tileSize * 0.7f)
            )
        }
        TileType.BOUNCE_PAD -> {
            drawRoundRect(
                color = Color(0xFFFFBE0B),
                topLeft = Offset(x + tileSize * 0.1f, y + tileSize * 0.6f),
                size = Size(tileSize * 0.8f, tileSize * 0.4f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        TileType.KEY_GOLD -> {
            drawCircle(
                color = Color(0xFFFFD700),
                radius = tileSize * 0.25f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
        }
        TileType.KEY_SILVER -> {
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = tileSize * 0.25f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
        }
        TileType.DOOR_GOLD -> {
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(x, y),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        TileType.DOOR_SILVER -> {
            drawRoundRect(
                color = Color(0xFFB0B0B0),
                topLeft = Offset(x, y),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        TileType.CHEST -> {
            drawRoundRect(
                color = Color(0xFF8B5A2B),
                topLeft = Offset(x + tileSize * 0.1f, y + tileSize * 0.2f),
                size = Size(tileSize * 0.8f, tileSize * 0.8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(x + tileSize * 0.4f, y + tileSize * 0.4f),
                size = Size(tileSize * 0.2f, tileSize * 0.2f)
            )
        }
        TileType.BREAKABLE_CRATE -> {
            drawRoundRect(
                color = Color(0xFF6F4E37),
                topLeft = Offset(x + 1, y + 1),
                size = Size(tileSize - 2, tileSize - 2),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        TileType.SWITCH_TOGGLE -> {
            drawCircle(
                color = Color(0xFF00FF88),
                radius = tileSize * 0.3f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
        }
        TileType.LASER_BARRIER -> {
            drawRect(
                color = Color(0xFFFF0055),
                topLeft = Offset(x + tileSize * 0.4f, y),
                size = Size(tileSize * 0.2f, tileSize)
            )
        }
        TileType.GOAL_FLAG -> {
            drawCircle(
                color = Color(0xFF7209B7),
                radius = tileSize * 0.45f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
            drawCircle(
                color = Color(0xFF4CC9F0),
                radius = tileSize * 0.25f,
                center = Offset(x + tileSize * 0.5f, y + tileSize * 0.5f)
            )
        }
        else -> {}
    }
}
