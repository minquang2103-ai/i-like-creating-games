package com.example.engine

import androidx.compose.ui.geometry.Offset
import com.example.audio.GameSoundSynthesizer
import com.example.model.EntityType
import com.example.model.GameGenre
import com.example.model.GameProject
import com.example.model.TileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var colorHex: Long,
    var size: Float,
    var alpha: Float = 1.0f,
    var lifeTime: Float = 1.0f
)

data class FloatingText(
    val id: String = java.util.UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    val text: String,
    val colorHex: Long,
    var alpha: Float = 1.0f,
    var age: Float = 0.0f
)

data class Projectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val isPlayer: Boolean,
    val damage: Int = 1,
    val colorHex: Long = 0xFF00FFFF,
    var alive: Boolean = true
)

data class RuntimeEntity(
    val id: String,
    val type: EntityType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var health: Int = 3,
    var maxHealth: Int = 3,
    var speed: Float = 2.0f,
    var damage: Int = 1,
    var patrolStartX: Float = 0f,
    var patrolEndX: Float = 0f,
    var direction: Int = 1,
    var isAlive: Boolean = true,
    var name: String = "",
    var dialogText: String = "",
    var shootCooldown: Float = 0f
)

data class Tower(
    val x: Float,
    val y: Float,
    val type: String, // "ARCHER", "CANNON", "FROST"
    val range: Float = 120f,
    val damage: Int = 2,
    var cooldown: Float = 0f,
    val colorHex: Long = 0xFF00E5FF
)

data class GameState(
    var playerX: Float = 0f,
    var playerY: Float = 0f,
    var playerVx: Float = 0f,
    var playerVy: Float = 0f,
    var playerHealth: Int = 5,
    var playerMaxHealth: Int = 5,
    var playerLives: Int = 3,
    var score: Int = 0,
    var goldKeys: Int = 0,
    var silverKeys: Int = 0,
    var isGrounded: Boolean = false,
    var facingRight: Boolean = true,
    var isJumping: Boolean = false,
    var jumpsRemaining: Int = 2,
    var isAttacking: Boolean = false,
    var attackTimer: Float = 0f,
    var speedBoostTimer: Float = 0f,
    var isGameOver: Boolean = false,
    var isGameWon: Boolean = false,
    var isPaused: Boolean = false,
    var levelTimeSeconds: Float = 0f,
    var screenShakeAmount: Float = 0f,
    var activeDialogue: String? = null,
    var currentLevelIndex: Int = 0,
    var goldCurrency: Int = 100 // for tower defense
)

class GameEngine(val project: GameProject) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    var particles = mutableListOf<Particle>()
    var floatingTexts = mutableListOf<FloatingText>()
    var projectiles = mutableListOf<Projectile>()
    var runtimeEntities = mutableListOf<RuntimeEntity>()
    var towers = mutableListOf<Tower>()

    // Breakout specific
    var ballX = 200f
    var ballY = 300f
    var ballVx = 3.5f
    var ballVy = -4.5f
    var paddleX = 200f
    var paddleWidth = 90f

    // Grid map clone
    var activeGrid: MutableList<MutableList<TileType>> = mutableListOf()
    var levelWidth = 24
    var levelHeight = 14
    var tileSizePx = 32f

    init {
        resetLevel(0)
    }

    fun resetLevel(levelIndex: Int) {
        val lvl = project.levels.getOrNull(levelIndex) ?: return
        levelWidth = lvl.width
        levelHeight = lvl.height

        activeGrid = MutableList(lvl.height) { y ->
            MutableList(lvl.width) { x ->
                lvl.getTileAt(x, y)
            }
        }

        particles.clear()
        floatingTexts.clear()
        projectiles.clear()
        runtimeEntities.clear()
        towers.clear()

        var pX = 2f
        var pY = 2f

        for (e in lvl.entities) {
            if (e.type == EntityType.PLAYER_SPAWN) {
                pX = e.gridX.toFloat()
                pY = e.gridY.toFloat()
            } else {
                runtimeEntities.add(
                    RuntimeEntity(
                        id = e.id,
                        type = e.type,
                        x = e.gridX.toFloat(),
                        y = e.gridY.toFloat(),
                        health = e.health,
                        maxHealth = e.health,
                        speed = e.speed,
                        damage = e.damage,
                        patrolStartX = (e.gridX - e.patrolRange).toFloat().coerceAtLeast(0f),
                        patrolEndX = (e.gridX + e.patrolRange).toFloat().coerceAtMost((lvl.width - 1).toFloat()),
                        name = e.name,
                        dialogText = e.dialogText
                    )
                )
            }
        }

        _gameState.value = GameState(
            playerX = pX,
            playerY = pY,
            playerHealth = project.config.playerMaxHealth,
            playerMaxHealth = project.config.playerMaxHealth,
            playerLives = project.config.startingLives,
            score = 0,
            goldKeys = 0,
            silverKeys = 0,
            jumpsRemaining = if (project.config.allowDoubleJump) 2 else 1,
            currentLevelIndex = levelIndex
        )

        // Breakout defaults
        ballX = levelWidth * tileSizePx / 2f
        ballY = (levelHeight - 3) * tileSizePx
        ballVx = 3.5f
        ballVy = -4.5f
        paddleX = ballX

        // Sound start
        GameSoundSynthesizer.startMusic(
            when (project.genre) {
                GameGenre.PLATFORMER -> GameSoundSynthesizer.MusicTheme.HERO_QUEST
                GameGenre.DUNGEON_RPG -> GameSoundSynthesizer.MusicTheme.DUNGEON_MYSTERY
                GameGenre.BRICK_BREAKER -> GameSoundSynthesizer.MusicTheme.ARCADE_FEVER
                GameGenre.STEALTH_MAZE -> GameSoundSynthesizer.MusicTheme.CYBER_SYNTH
                GameGenre.TOWER_DEFENSE -> GameSoundSynthesizer.MusicTheme.PEACEFUL_HAVEN
            }
        )
    }

    fun update(dt: Float, moveInputX: Float, moveInputY: Float) {
        val state = _gameState.value
        if (state.isGameOver || state.isGameWon || state.isPaused) return

        state.levelTimeSeconds += dt
        if (state.screenShakeAmount > 0) {
            state.screenShakeAmount = (state.screenShakeAmount - dt * 2f).coerceAtLeast(0f)
        }

        // Particle updates
        val particleIter = particles.iterator()
        while (particleIter.hasNext()) {
            val p = particleIter.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= dt / p.lifeTime
            if (p.alpha <= 0) particleIter.remove()
        }

        // Floating texts
        val textIter = floatingTexts.iterator()
        while (textIter.hasNext()) {
            val ft = textIter.next()
            ft.y -= dt * 25f
            ft.alpha -= dt * 0.8f
            if (ft.alpha <= 0) textIter.remove()
        }

        // Branch by genre
        when (project.genre) {
            GameGenre.PLATFORMER -> updatePlatformer(state, dt, moveInputX)
            GameGenre.DUNGEON_RPG -> updateTopDownRpg(state, dt, moveInputX, moveInputY)
            GameGenre.BRICK_BREAKER -> updateBrickBreaker(state, dt, moveInputX)
            GameGenre.STEALTH_MAZE -> updateStealthMaze(state, dt, moveInputX, moveInputY)
            GameGenre.TOWER_DEFENSE -> updateTowerDefense(state, dt)
        }

        // Update Projectiles
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
            val proj = projIter.next()
            proj.x += proj.vx
            proj.y += proj.vy

            // Check tile collision
            val gx = (proj.x / tileSizePx).toInt()
            val gy = (proj.y / tileSizePx).toInt()
            if (isTileSolid(gx, gy)) {
                spawnParticles(proj.x, proj.y, proj.colorHex, 6)
                projIter.remove()
                continue
            }

            if (proj.isPlayer) {
                for (entity in runtimeEntities.filter { it.isAlive }) {
                    val ex = entity.x * tileSizePx + tileSizePx / 2
                    val ey = entity.y * tileSizePx + tileSizePx / 2
                    if (abs(proj.x - ex) < tileSizePx * 0.7f && abs(proj.y - ey) < tileSizePx * 0.7f) {
                        damageEntity(entity, proj.damage, state)
                        projIter.remove()
                        break
                    }
                }
            } else {
                // Enemy projectile hitting player
                val px = state.playerX * tileSizePx + tileSizePx / 2
                val py = state.playerY * tileSizePx + tileSizePx / 2
                if (abs(proj.x - px) < tileSizePx * 0.6f && abs(proj.y - py) < tileSizePx * 0.6f) {
                    damagePlayer(proj.damage, state)
                    projIter.remove()
                }
            }
        }

        _gameState.value = state.copy()
    }

    private fun updatePlatformer(state: GameState, dt: Float, moveInputX: Float) {
        val speed = project.config.moveSpeed * (if (state.speedBoostTimer > 0) 1.4f else 1.0f)
        state.playerVx = moveInputX * (speed * 0.08f)

        if (moveInputX > 0.1f) state.facingRight = true
        if (moveInputX < -0.1f) state.facingRight = false

        // Gravity
        state.playerVy += project.config.gravity * 0.035f

        // Move X with collision
        val newX = state.playerX + state.playerVx
        val nextTileX = if (state.playerVx > 0) (newX + 0.6f).toInt() else newX.toInt()
        val curY = state.playerY.toInt()

        if (!isTileSolid(nextTileX, curY) && !isTileSolid(nextTileX, (state.playerY + 0.9f).toInt())) {
            state.playerX = newX.coerceIn(0f, (levelWidth - 1).toFloat())
        } else {
            state.playerVx = 0f
        }

        // Move Y with collision
        val newY = state.playerY + state.playerVy
        val checkY = if (state.playerVy > 0) (newY + 1.0f).toInt() else newY.toInt()
        val checkX = (state.playerX + 0.3f).toInt()

        if (isTileSolid(checkX, checkY) || isTileSolid((state.playerX + 0.6f).toInt(), checkY)) {
            if (state.playerVy > 0) {
                // Landed on floor
                state.playerY = checkY.toFloat() - 1.0f
                state.playerVy = 0f
                state.isGrounded = true
                state.jumpsRemaining = if (project.config.allowDoubleJump) 2 else 1
            } else {
                // Hit ceiling
                state.playerY = checkY.toFloat() + 1.0f
                state.playerVy = 0f
            }
        } else {
            state.playerY = newY
            state.isGrounded = false
        }

        // Fall out of world
        if (state.playerY > levelHeight + 1) {
            damagePlayer(state.playerHealth, state)
        }

        // Check tile pickups and hazards
        checkTileInteractions(state)

        // Update entities (Enemies, NPCs)
        updateEntities(state, dt)
    }

    private fun updateTopDownRpg(state: GameState, dt: Float, moveInputX: Float, moveInputY: Float) {
        val speed = project.config.moveSpeed * 0.06f
        val newX = state.playerX + moveInputX * speed
        val newY = state.playerY + moveInputY * speed

        if (moveInputX > 0.1f) state.facingRight = true
        if (moveInputX < -0.1f) state.facingRight = false

        if (!isTileSolid(newX.toInt(), state.playerY.toInt()) && !isTileSolid((newX + 0.6f).toInt(), state.playerY.toInt())) {
            state.playerX = newX.coerceIn(0f, (levelWidth - 1).toFloat())
        }
        if (!isTileSolid(state.playerX.toInt(), newY.toInt()) && !isTileSolid(state.playerX.toInt(), (newY + 0.6f).toInt())) {
            state.playerY = newY.coerceIn(0f, (levelHeight - 1).toFloat())
        }

        checkTileInteractions(state)
        updateEntities(state, dt)
    }

    private fun updateBrickBreaker(state: GameState, dt: Float, moveInputX: Float) {
        paddleX += moveInputX * 8f
        val maxX = levelWidth * tileSizePx - paddleWidth / 2
        paddleX = paddleX.coerceIn(paddleWidth / 2, maxX)

        ballX += ballVx
        ballY += ballVy

        // Wall collisions
        val boundW = levelWidth * tileSizePx
        if (ballX < 12f) {
            ballX = 12f
            ballVx = abs(ballVx)
            GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SWITCH_CLICK)
        } else if (ballX > boundW - 12f) {
            ballX = boundW - 12f
            ballVx = -abs(ballVx)
            GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SWITCH_CLICK)
        }

        if (ballY < 12f) {
            ballY = 12f
            ballVy = abs(ballVy)
            GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SWITCH_CLICK)
        }

        // Paddle collision
        val paddleY = (levelHeight - 2) * tileSizePx
        if (ballY in (paddleY - 14f)..(paddleY + 10f)) {
            if (ballX in (paddleX - paddleWidth / 2 - 8f)..(paddleX + paddleWidth / 2 + 8f)) {
                ballVy = -abs(ballVy)
                val hitOffset = (ballX - paddleX) / (paddleWidth / 2)
                ballVx = hitOffset * 5.5f
                spawnParticles(ballX, ballY, 0xFF00F0FF, 8)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.JUMP)
            }
        }

        // Bottom ball loss
        if (ballY > levelHeight * tileSizePx) {
            damagePlayer(1, state)
            ballX = paddleX
            ballY = paddleY - 20f
            ballVy = -4.5f
            ballVx = (listOf(-3.5f, 3.5f).random())
        }

        // Brick collision
        val gx = (ballX / tileSizePx).toInt()
        val gy = (ballY / tileSizePx).toInt()
        if (gy in activeGrid.indices && gx in activeGrid[gy].indices) {
            val tile = activeGrid[gy][gx]
            if (tile == TileType.GEM || tile == TileType.COIN || tile == TileType.BREAKABLE_CRATE || tile == TileType.SOLID) {
                if (tile != TileType.SOLID) {
                    activeGrid[gy][gx] = TileType.EMPTY
                    state.score += 150
                    spawnParticles(ballX, ballY, 0xFFFF0055, 12)
                    addFloatingText(ballX, ballY, "+150", 0xFFFFD700)
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.ENEMY_HIT)
                    ballVy = -ballVy

                    // Check remaining bricks win
                    val remaining = activeGrid.sumOf { row -> row.count { it == TileType.GEM || it == TileType.COIN || it == TileType.BREAKABLE_CRATE } }
                    if (remaining == 0) {
                        winGame(state)
                    }
                } else {
                    ballVy = -ballVy
                }
            }
        }
    }

    private fun updateStealthMaze(state: GameState, dt: Float, moveInputX: Float, moveInputY: Float) {
        updateTopDownRpg(state, dt, moveInputX, moveInputY)

        // Guard cone detection
        for (guard in runtimeEntities.filter { it.isAlive }) {
            val dx = state.playerX - guard.x
            val dy = state.playerY - guard.y
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < 3.5f) {
                // Guard vision cone check
                val guardDirX = if (guard.direction > 0) 1f else -1f
                val dot = dx * guardDirX
                if (dot > 0.3f && dist < 3.2f) {
                    // Spotted!
                    damagePlayer(1, state)
                    state.screenShakeAmount = 1.0f
                    addFloatingText(state.playerX * tileSizePx, state.playerY * tileSizePx, "SPOTTED!", 0xFFFF0000)
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.HURT)
                    break
                }
            }
        }
    }

    private fun updateTowerDefense(state: GameState, dt: Float) {
        // Tower target shooting
        for (tower in towers) {
            tower.cooldown -= dt
            if (tower.cooldown <= 0f) {
                val target = runtimeEntities.firstOrNull { it.isAlive && it.type == EntityType.ENEMY_PATROL }
                if (target != null) {
                    val ex = target.x * tileSizePx
                    val ey = target.y * tileSizePx
                    val dist = sqrt((tower.x - ex) * (tower.x - ex) + (tower.y - ey) * (tower.y - ey))
                    if (dist <= tower.range) {
                        tower.cooldown = 0.8f
                        val angle = atan2(ey - tower.y, ex - tower.x)
                        projectiles.add(
                            Projectile(
                                x = tower.x,
                                y = tower.y,
                                vx = cos(angle) * 7f,
                                vy = sin(angle) * 7f,
                                isPlayer = true,
                                damage = tower.damage,
                                colorHex = tower.colorHex
                            )
                        )
                        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.LASER_BLAST)
                    }
                }
            }
        }

        // Creep path movement
        for (creep in runtimeEntities.filter { it.isAlive }) {
            creep.x += creep.speed * 0.03f
            if (creep.x >= levelWidth - 2) {
                creep.isAlive = false
                damagePlayer(1, state)
                addFloatingText(creep.x * tileSizePx, creep.y * tileSizePx, "BASE BREACHED!", 0xFFFF0000)
            }
        }

        // Check if all creeps defeated
        if (runtimeEntities.isNotEmpty() && runtimeEntities.all { !it.isAlive }) {
            winGame(state)
        }
    }

    fun jump() {
        val state = _gameState.value
        if (state.isGameOver || state.isGameWon) return

        if (state.isGrounded || state.jumpsRemaining > 0) {
            state.playerVy = -project.config.jumpForce * 0.042f
            state.isGrounded = false
            state.jumpsRemaining--
            val isDouble = state.jumpsRemaining == 0 && project.config.allowDoubleJump
            GameSoundSynthesizer.playSfx(if (isDouble) GameSoundSynthesizer.SoundFx.DOUBLE_JUMP else GameSoundSynthesizer.SoundFx.JUMP)
            spawnParticles(state.playerX * tileSizePx + 16, (state.playerY + 1) * tileSizePx, 0xFF00F0FF, 6)
            _gameState.value = state.copy()
        }
    }

    fun attack() {
        val state = _gameState.value
        if (state.isGameOver || state.isGameWon || !project.config.hasAttack) return

        state.isAttacking = true
        state.attackTimer = 0.25f
        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SWORD_SLASH)

        // Spawn attack projectile or slash
        val pX = state.playerX * tileSizePx + 16
        val pY = state.playerY * tileSizePx + 16
        val dir = if (state.facingRight) 1f else -1f

        projectiles.add(
            Projectile(
                x = pX + dir * 18,
                y = pY,
                vx = dir * 9f,
                vy = 0f,
                isPlayer = true,
                damage = project.config.playerAttackDamage,
                colorHex = 0xFFFF0055
            )
        )

        spawnParticles(pX + dir * 20, pY, 0xFFFF0055, 8)
        _gameState.value = state.copy()
    }

    fun placeTower(gridX: Int, gridY: Int) {
        val state = _gameState.value
        if (state.goldCurrency >= 40) {
            state.goldCurrency -= 40
            towers.add(
                Tower(
                    x = gridX * tileSizePx + tileSizePx / 2,
                    y = gridY * tileSizePx + tileSizePx / 2,
                    type = "ARCHER",
                    damage = 2,
                    colorHex = 0xFF00FFE0
                )
            )
            spawnParticles(gridX * tileSizePx + 16, gridY * tileSizePx + 16, 0xFF00FFE0, 10)
            GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.POWERUP)
            _gameState.value = state.copy()
        }
    }

    private fun checkTileInteractions(state: GameState) {
        val gx = (state.playerX + 0.4f).toInt()
        val gy = (state.playerY + 0.5f).toInt()

        if (gy !in activeGrid.indices || gx !in activeGrid[gy].indices) return

        when (activeGrid[gy][gx]) {
            TileType.COIN -> {
                activeGrid[gy][gx] = TileType.EMPTY
                state.score += 100
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "+100", 0xFFFFD700)
                spawnParticles(gx * tileSizePx + 16, gy * tileSizePx + 16, 0xFFFFD700, 8)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.COIN)
            }
            TileType.GEM -> {
                activeGrid[gy][gx] = TileType.EMPTY
                state.score += 500
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "+500 GEM!", 0xFF00FFFF)
                spawnParticles(gx * tileSizePx + 16, gy * tileSizePx + 16, 0xFF00FFFF, 12)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.GEM)
            }
            TileType.KEY_GOLD -> {
                activeGrid[gy][gx] = TileType.EMPTY
                state.goldKeys++
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "GOLD KEY FOUND!", 0xFFFFD700)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.KEY)
            }
            TileType.KEY_SILVER -> {
                activeGrid[gy][gx] = TileType.EMPTY
                state.silverKeys++
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "SILVER KEY FOUND!", 0xFFE0E0E0)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.KEY)
            }
            TileType.BOUNCE_PAD -> {
                state.playerVy = -18f * 0.042f
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SPRING_BOUNCE)
                spawnParticles(gx * tileSizePx + 16, gy * tileSizePx + 16, 0xFFFFBE0B, 10)
            }
            TileType.SPIKES, TileType.LAVA -> {
                damagePlayer(1, state)
            }
            TileType.CHEST -> {
                activeGrid[gy][gx] = TileType.EMPTY
                state.score += 1000
                state.playerHealth = (state.playerHealth + 2).coerceAtMost(state.playerMaxHealth)
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "TREASURE! +1000", 0xFFFFD700)
                spawnParticles(gx * tileSizePx + 16, gy * tileSizePx + 16, 0xFFFFD700, 16)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.CHEST_OPEN)
            }
            TileType.SWITCH_TOGGLE -> {
                activeGrid[gy][gx] = TileType.EMPTY
                addFloatingText(gx * tileSizePx, gy * tileSizePx, "SWITCH TRIGGERED!", 0xFF00FF88)
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.SWITCH_CLICK)
                // Unlock doors and laser barriers on map
                for (row in activeGrid) {
                    for (i in row.indices) {
                        if (row[i] == TileType.LASER_BARRIER || row[i] == TileType.DOOR_GOLD) {
                            row[i] = TileType.EMPTY
                        }
                    }
                }
            }
            TileType.GOAL_FLAG -> {
                winGame(state)
            }
            else -> {}
        }

        // Check if player bumped into locked door with key
        for (oy in -1..1) {
            for (ox in -1..1) {
                val tx = gx + ox
                val ty = gy + oy
                if (ty in activeGrid.indices && tx in activeGrid[ty].indices) {
                    if (activeGrid[ty][tx] == TileType.DOOR_GOLD && state.goldKeys > 0) {
                        state.goldKeys--
                        activeGrid[ty][tx] = TileType.EMPTY
                        addFloatingText(tx * tileSizePx, ty * tileSizePx, "DOOR UNLOCKED!", 0xFFFFD700)
                        spawnParticles(tx * tileSizePx + 16, ty * tileSizePx + 16, 0xFFFFD700, 14)
                        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.DOOR_OPEN)
                    } else if (activeGrid[ty][tx] == TileType.DOOR_SILVER && state.silverKeys > 0) {
                        state.silverKeys--
                        activeGrid[ty][tx] = TileType.EMPTY
                        addFloatingText(tx * tileSizePx, ty * tileSizePx, "DOOR UNLOCKED!", 0xFFE0E0E0)
                        spawnParticles(tx * tileSizePx + 16, ty * tileSizePx + 16, 0xFFE0E0E0, 14)
                        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.DOOR_OPEN)
                    }
                }
            }
        }
    }

    private fun updateEntities(state: GameState, dt: Float) {
        val px = state.playerX
        val py = state.playerY

        for (entity in runtimeEntities.filter { it.isAlive }) {
            // Patrol movement
            if (entity.type == EntityType.ENEMY_PATROL || entity.type == EntityType.ENEMY_BOUNCER) {
                entity.x += entity.direction * entity.speed * 0.03f
                if (entity.x > entity.patrolEndX) {
                    entity.x = entity.patrolEndX
                    entity.direction = -1
                } else if (entity.x < entity.patrolStartX) {
                    entity.x = entity.patrolStartX
                    entity.direction = 1
                }
            } else if (entity.type == EntityType.ENEMY_BOSS) {
                // Boss slowly moves toward player
                val dx = px - entity.x
                if (abs(dx) > 0.2f) {
                    entity.x += (if (dx > 0) 1 else -1) * entity.speed * 0.02f
                }
            }

            // Check collision with player
            val dist = sqrt((px - entity.x) * (px - entity.x) + (py - entity.y) * (py - entity.y))
            if (dist < 0.8f) {
                if (entity.type == EntityType.NPC_GUIDE) {
                    state.activeDialogue = entity.dialogText.ifBlank { "Greetings, adventurer!" }
                } else {
                    damagePlayer(entity.damage, state)
                }
            }
        }
    }

    fun dismissDialogue() {
        val state = _gameState.value
        state.activeDialogue = null
        _gameState.value = state.copy()
    }

    private fun damageEntity(entity: RuntimeEntity, dmg: Int, state: GameState) {
        entity.health -= dmg
        spawnParticles(entity.x * tileSizePx + 16, entity.y * tileSizePx + 16, 0xFFFF0055, 10)
        addFloatingText(entity.x * tileSizePx, entity.y * tileSizePx, "-$dmg", 0xFFFF0055)
        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.ENEMY_HIT)

        if (entity.health <= 0) {
            entity.isAlive = false
            val reward = if (entity.type == EntityType.ENEMY_BOSS) 2500 else 250
            state.score += reward
            spawnParticles(entity.x * tileSizePx + 16, entity.y * tileSizePx + 16, 0xFFFFD700, 20)
            addFloatingText(entity.x * tileSizePx, entity.y * tileSizePx, "+$reward DEFEATED!", 0xFFFFD700)
            GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.EXPLOSION)

            if (entity.type == EntityType.ENEMY_BOSS) {
                winGame(state)
            }
        }
    }

    private fun damagePlayer(dmg: Int, state: GameState) {
        if (state.isGameOver || state.isGameWon) return

        state.playerHealth -= dmg
        state.screenShakeAmount = 0.8f
        spawnParticles(state.playerX * tileSizePx + 16, state.playerY * tileSizePx + 16, 0xFFFF0000, 12)
        addFloatingText(state.playerX * tileSizePx, state.playerY * tileSizePx, "-$dmg HP", 0xFFFF3838)
        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.HURT)

        if (state.playerHealth <= 0) {
            state.playerLives--
            if (state.playerLives > 0) {
                // Respawn at origin
                state.playerHealth = state.playerMaxHealth
                state.playerX = 1f
                state.playerY = 1f
                state.playerVx = 0f
                state.playerVy = 0f
            } else {
                state.isGameOver = true
                GameSoundSynthesizer.stopMusic()
                GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.GAME_OVER)
            }
        }
    }

    private fun winGame(state: GameState) {
        state.isGameWon = true
        val timeBonus = (project.config.timeLimitSeconds - state.levelTimeSeconds).coerceAtLeast(0f) * 10
        state.score += timeBonus.toInt()
        GameSoundSynthesizer.stopMusic()
        GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.VICTORY)
    }

    private fun isTileSolid(gx: Int, gy: Int): Boolean {
        if (gy !in activeGrid.indices || gx !in activeGrid[gy].indices) return false
        val t = activeGrid[gy][gx]
        return t.solid
    }

    fun spawnParticles(x: Float, y: Float, color: Long, count: Int) {
        for (i in 0 until count) {
            val angle = (0..360).random() * PI.toFloat() / 180f
            val speed = (2..6).random().toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    colorHex = color,
                    size = (4..8).random().toFloat(),
                    lifeTime = 0.4f + (0..4).random() * 0.1f
                )
            )
        }
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Long) {
        floatingTexts.add(FloatingText(x = x, y = y, text = text, colorHex = color))
    }
}
