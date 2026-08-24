package com.example.asset

import java.util.UUID

enum class AssetCategory(val displayName: String, val iconName: String, val description: String) {
    CHARACTERS("Characters", "person", "Heroes, enemies, bosses, NPCs, and animated sprites"),
    ENVIRONMENTS("Environments", "landscape", "Tilesets, obstacles, hazards, scenery, and portals"),
    UI_ELEMENTS("UI Elements", "dashboard", "Health gauges, dialogue boxes, virtual controls, and victory badges"),
    SOUND_EFFECTS("Sound Effects", "volume_up", "8-bit jump, blaster, coin chimes, impacts, and fanfare"),
    BACKGROUND_MUSIC("Background Music", "music_note", "Chiptune melodies, synthwave drives, dungeon ambiances")
}

enum class SpriteTag {
    HERO, ENEMY, BOSS, NPC, TILE, HAZARD, ITEM, POWERUP, DECORATION, HUD, RETRO, SCI_FI, FANTASY
}

data class SpritePixelMatrix(
    val width: Int = 8,
    val height: Int = 8,
    // Colors stored as hexadecimal ARGB strings or palette index
    val pixels: List<String> = emptyList()
)

data class GameAsset(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: AssetCategory,
    val tags: List<SpriteTag>,
    val description: String,
    val author: String = "Arcade Studio Library",
    val primaryColorHex: Long = 0xFF00E5FF,
    val secondaryColorHex: Long = 0xFFFF0055,
    val previewIcon: String = "star",
    val sfxType: String? = null,
    val bgmType: String? = null,
    val pixelArt: SpritePixelMatrix? = null,
    val properties: Map<String, String> = emptyMap(),
    val isCustomUserCreated: Boolean = false
)
