package com.example.model

import java.util.UUID

enum class GameGenre(val displayName: String, val iconName: String, val description: String) {
    PLATFORMER("Platformer", "sports_score", "Side-scrolling jump & run with hazards, coins, and bosses"),
    DUNGEON_RPG("Dungeon RPG", "shield", "Top-down adventure with swords, magic, chests, and locked doors"),
    BRICK_BREAKER("Brick Breaker", "videogame_asset", "Retro arcade paddle action with multi-balls and powerups"),
    STEALTH_MAZE("Stealth Maze", "visibility_off", "Navigate maze avoiding guard vision cones and lasers"),
    TOWER_DEFENSE("Tower Defense", "fort", "Place tactical towers and defend against creeping enemy waves")
}

enum class GameTheme(
    val displayName: String,
    val bgGradientStart: Long,
    val bgGradientEnd: Long,
    val primaryColor: Long,
    val accentColor: Long,
    val wallColor: Long,
    val hazardColor: Long
) {
    RETRO_NEON("Retro Neon", 0xFF0D0221, 0xFF190061, 0xFF00F0FF, 0xFFFF0055, 0xFF240046, 0xFFFF007F),
    CYBERPUNK("Cyberpunk", 0xFF08071A, 0xFF140F2D, 0xFF00FFE0, 0xFFFFE600, 0xFF1B143A, 0xFFFF2A6D),
    FANTASY_DUNGEON("Dungeon Castle", 0xFF1A1423, 0xFF281E38, 0xFFE0A96D, 0xFF774936, 0xFF3D2C4D, 0xFFC1121F),
    VOLCANIC_CAVERN("Lava Cavern", 0xFF1F0808, 0xFF381010, 0xFFFF5400, 0xFFFFD000, 0xFF4A1E1E, 0xFFFF0000),
    FOREST_QUEST("Enchanted Forest", 0xFF081C15, 0xFF1B4332, 0xFF52B788, 0xFFD8F3DC, 0xFF2D6A4F, 0xFFE76F51),
    SPACE_ODYSSEY("Deep Space", 0xFF03071E, 0xFF0B132B, 0xFF4CC9F0, 0xFFF72585, 0xFF1C2541, 0xFFE63946),
    CANDY_LAND("Sweet Kingdom", 0xFF2B0938, 0xFF441151, 0xFFFF70A6, 0xFFFF9770, 0xFF5C1B6E, 0xFFFF0055)
}

enum class TileType(val symbol: Char, val label: String, val solid: Boolean) {
    EMPTY('.', "Empty", false),
    SOLID('#', "Solid Wall / Ground", true),
    PLATFORM('=', "Jump-through Platform", true),
    SPIKES('^', "Spikes Hazard", false),
    LAVA('~', "Lava / Acid Hazard", false),
    ICE('_', "Slippery Ice", true),
    BOUNCE_PAD('U', "Spring Jump Pad", true),
    COIN('o', "Gold Coin", false),
    GEM('*', "Power Gem", false),
    KEY_GOLD('k', "Gold Key", false),
    KEY_SILVER('s', "Silver Key", false),
    DOOR_GOLD('D', "Gold Locked Door", true),
    DOOR_SILVER('S', "Silver Locked Door", true),
    CHEST('C', "Treasure Chest", false),
    BREAKABLE_CRATE('B', "Breakable Crate", true),
    SWITCH_TOGGLE('T', "Switch Trigger", false),
    LASER_BARRIER('L', "Laser Barrier", true),
    WATER('W', "Water Pool", false),
    LADDER('H', "Climb Ladder", false),
    GOAL_FLAG('X', "Victory Goal", false)
}

enum class EntityType(val label: String, val category: String) {
    PLAYER_SPAWN("Player Spawn", "Hero"),
    ENEMY_PATROL("Patrol Enemy", "Enemy"),
    ENEMY_FLYING("Flying Bat / Drone", "Enemy"),
    ENEMY_SHOOTER("Turret / Archer", "Enemy"),
    ENEMY_BOUNCER("Jumping Slime", "Enemy"),
    ENEMY_BOSS("Dungeon Boss", "Boss"),
    NPC_GUIDE("NPC Story Teller", "NPC"),
    POWERUP_SPEED("Speed Boots", "Powerup"),
    POWERUP_SHIELD("Energy Shield", "Powerup"),
    POWERUP_BLASTER("Magic Wand / Blaster", "Powerup"),
    CHECKPOINT("Save Flag", "Item")
}

data class GameEntityConfig(
    val id: String = UUID.randomUUID().toString(),
    val type: EntityType = EntityType.PLAYER_SPAWN,
    var gridX: Int = 0,
    var gridY: Int = 0,
    var health: Int = 3,
    var speed: Float = 2.0f,
    var damage: Int = 1,
    var patrolRange: Int = 4,
    var name: String = "Entity",
    var dialogText: String = "",
    var scoreValue: Int = 100
)

data class GameConfig(
    var gravity: Float = 0.5f,
    var jumpForce: Float = 11.0f,
    var moveSpeed: Float = 4.5f,
    var playerMaxHealth: Int = 5,
    var startingLives: Int = 3,
    var playerAttackDamage: Int = 1,
    var timeLimitSeconds: Int = 120,
    var scoreMultiplier: Float = 1.0f,
    var allowDoubleJump: Boolean = true,
    var hasAttack: Boolean = true,
    var winCondition: String = "Reach the Goal Flag or defeat the Boss"
)

data class GameLevel(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Level 1",
    var width: Int = 24,
    var height: Int = 14,
    // 2D grid matrix stored as flat string row array for efficient serialization
    var gridData: List<String> = emptyList(),
    var entities: MutableList<GameEntityConfig> = mutableListOf()
) {
    fun getTileAt(x: Int, y: Int): TileType {
        if (y !in gridData.indices) return TileType.EMPTY
        val row = gridData[y]
        if (x !in row.indices) return TileType.EMPTY
        val symbol = row[x]
        return TileType.values().firstOrNull { it.symbol == symbol } ?: TileType.EMPTY
    }

    fun setTileAt(x: Int, y: Int, tile: TileType) {
        if (y !in 0 until height || x !in 0 until width) return
        val currentRows = gridData.toMutableList()
        while (currentRows.size < height) {
            currentRows.add(".".repeat(width))
        }
        val row = currentRows[y].padEnd(width, '.').toCharArray()
        row[x] = tile.symbol
        currentRows[y] = String(row)
        gridData = currentRows
    }
}

data class HighscoreEntry(
    val id: String = UUID.randomUUID().toString(),
    val playerName: String,
    val score: Int,
    val timeSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameReview(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameProject(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "New Game",
    var description: String = "A fun arcade game made with Arcade Studio.",
    var author: String = "Creator",
    var genre: GameGenre = GameGenre.PLATFORMER,
    var theme: GameTheme = GameTheme.RETRO_NEON,
    var difficulty: String = "Medium",
    var tags: List<String> = listOf("Arcade", "Action"),
    var createdAt: Long = System.currentTimeMillis(),
    var playCount: Int = 0,
    var likesCount: Int = 0,
    var rating: Float = 4.8f,
    var isPublished: Boolean = true,
    var isLikedByUser: Boolean = false,
    var config: GameConfig = GameConfig(),
    var levels: MutableList<GameLevel> = mutableListOf(),
    var highscores: MutableList<HighscoreEntry> = mutableListOf(),
    var reviews: MutableList<GameReview> = mutableListOf(),
    var shareCode: String = ""
)
