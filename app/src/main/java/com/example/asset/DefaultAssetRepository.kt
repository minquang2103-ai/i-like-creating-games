package com.example.asset

import java.util.UUID

object DefaultAssetRepository {

    private val customAssets = mutableListOf<GameAsset>()

    private fun createPixelArt(pattern: List<String>, charColorMap: Map<Char, String>): SpritePixelMatrix {
        val height = pattern.size
        val width = if (height > 0) pattern[0].length else 0
        val pixels = mutableListOf<String>()

        for (row in pattern) {
            for (char in row) {
                pixels.add(charColorMap[char] ?: "#00000000")
            }
        }
        return SpritePixelMatrix(width, height, pixels)
    }

    val defaultAssets: List<GameAsset> by lazy {
        val list = mutableListOf<GameAsset>()

        // 1. CHARACTERS
        list.add(
            GameAsset(
                id = "char_cyber_ninja",
                title = "Cyber Ninja",
                category = AssetCategory.CHARACTERS,
                tags = listOf(SpriteTag.HERO, SpriteTag.SCI_FI, SpriteTag.RETRO),
                description = "Agile cyber warrior with glowing visor, katana dash, and double-jump thrusters.",
                primaryColorHex = 0xFF00F0FF,
                secondaryColorHex = 0xFFFF0055,
                previewIcon = "face",
                properties = mapOf("Speed" to "4.8", "Health" to "5", "Special" to "Double Jump"),
                pixelArt = createPixelArt(
                    listOf(
                        "..####..",
                        ".#0000#.",
                        ".#1111#.",
                        ".######.",
                        "..####..",
                        ".##..##.",
                        ".##..##.",
                        "##....##"
                    ),
                    mapOf('#' to "#FF1A1A2E", '0' to "#FF00F0FF", '1' to "#FFFF0055")
                )
            )
        )

        list.add(
            GameAsset(
                id = "char_paladin_knight",
                title = "Paladin Knight",
                category = AssetCategory.CHARACTERS,
                tags = listOf(SpriteTag.HERO, SpriteTag.FANTASY),
                description = "Armored holy knight with golden broadsword and radiant shield block.",
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFFE0E1DD,
                previewIcon = "shield",
                properties = mapOf("Speed" to "3.8", "Health" to "8", "Special" to "Shield Bash"),
                pixelArt = createPixelArt(
                    listOf(
                        "..####..",
                        ".#GGGG#.",
                        ".#GSSG#.",
                        ".#GGGG#.",
                        ".######.",
                        "##BBBB##",
                        ".#BBBB#.",
                        ".##..##."
                    ),
                    mapOf('#' to "#FF242424", 'G' to "#FFFFD700", 'S' to "#FFE0E1DD", 'B' to "#FF1D3557")
                )
            )
        )

        list.add(
            GameAsset(
                id = "char_slime_sentinel",
                title = "Laser Slime",
                category = AssetCategory.CHARACTERS,
                tags = listOf(SpriteTag.ENEMY, SpriteTag.RETRO),
                description = "Bouncing gelatinous slime infused with unstable plasma particles.",
                primaryColorHex = 0xFF00FF88,
                secondaryColorHex = 0xFF003820,
                previewIcon = "bug_report",
                properties = mapOf("Speed" to "2.2", "Damage" to "1", "Behavior" to "Hop Patrol"),
                pixelArt = createPixelArt(
                    listOf(
                        "........",
                        "...##...",
                        "..#GG#..",
                        ".#GGGG#.",
                        ".#EGEG#.",
                        "#GGGGGG#",
                        "########",
                        "........"
                    ),
                    mapOf('#' to "#FF005F33", 'G' to "#FF00FF88", 'E' to "#FFFF0055")
                )
            )
        )

        list.add(
            GameAsset(
                id = "char_mecha_boss",
                title = "Dread Mecha Boss",
                category = AssetCategory.CHARACTERS,
                tags = listOf(SpriteTag.BOSS, SpriteTag.SCI_FI),
                description = "Colossal cybernetic battle tank with laser cannon and shield generator.",
                primaryColorHex = 0xFFFF0055,
                secondaryColorHex = 0xFF2B0938,
                previewIcon = "warning",
                properties = mapOf("Speed" to "1.5", "Health" to "20", "Damage" to "3"),
                pixelArt = createPixelArt(
                    listOf(
                        "##.##.##",
                        "#RRRRRR#",
                        "#R0RR0R#",
                        "########",
                        "##RRRR##",
                        "########",
                        "#BB..BB#",
                        "##....##"
                    ),
                    mapOf('#' to "#FF1A051D", 'R' to "#FFFF0055", '0' to "#FFFFFF00", 'B' to "#FF3A0CA3")
                )
            )
        )

        list.add(
            GameAsset(
                id = "char_npc_wizard",
                title = "Chronos Mage NPC",
                category = AssetCategory.CHARACTERS,
                tags = listOf(SpriteTag.NPC, SpriteTag.FANTASY),
                description = "Mystical time weaver providing lore, quest hints, and item upgrades.",
                primaryColorHex = 0xFF9B5DE5,
                secondaryColorHex = 0xFFFEE440,
                previewIcon = "auto_stories",
                properties = mapOf("Type" to "Dialogue Guide", "Quest" to "Collect 3 Keys"),
                pixelArt = createPixelArt(
                    listOf(
                        "..####..",
                        "..#PP#..",
                        ".#PPPP#.",
                        ".#PWWP#.",
                        "..#WW#..",
                        ".#PPPP#.",
                        ".#PPPP#.",
                        "..####.."
                    ),
                    mapOf('#' to "#FF301934", 'P' to "#FF9B5DE5", 'W' to "#FFFFFFFF")
                )
            )
        )

        // 2. ENVIRONMENTS
        list.add(
            GameAsset(
                id = "env_neon_grid_tile",
                title = "Neon Grid Solid Block",
                category = AssetCategory.ENVIRONMENTS,
                tags = listOf(SpriteTag.TILE, SpriteTag.RETRO),
                description = "Electroluminescent futuristic building block with border pulse.",
                primaryColorHex = 0xFF240046,
                secondaryColorHex = 0xFF00F0FF,
                previewIcon = "grid_view",
                properties = mapOf("Solid" to "True", "Friction" to "1.0", "Theme" to "Retro Neon")
            )
        )

        list.add(
            GameAsset(
                id = "env_lava_hazard",
                title = "Lava Acid Pool",
                category = AssetCategory.ENVIRONMENTS,
                tags = listOf(SpriteTag.HAZARD, SpriteTag.FANTASY),
                description = "Molten magma pool that instantly vaporizes intruders on contact.",
                primaryColorHex = 0xFFFF5400,
                secondaryColorHex = 0xFFFFD000,
                previewIcon = "local_fire_department",
                properties = mapOf("Solid" to "False", "Damage" to "Lethal", "Animation" to "Bubbling")
            )
        )

        list.add(
            GameAsset(
                id = "env_jump_pad",
                title = "Spring Jump Pad",
                category = AssetCategory.ENVIRONMENTS,
                tags = listOf(SpriteTag.ITEM, SpriteTag.RETRO),
                description = "Kinetic acceleration plate that propels heroes skyward.",
                primaryColorHex = 0xFFFFBE0B,
                secondaryColorHex = 0xFFFB5607,
                previewIcon = "expand_less",
                properties = mapOf("Impulse Force" to "16.0", "Sound" to "Spring Boing")
            )
        )

        list.add(
            GameAsset(
                id = "env_portal_goal",
                title = "Chrono Goal Portal",
                category = AssetCategory.ENVIRONMENTS,
                tags = listOf(SpriteTag.ITEM, SpriteTag.SCI_FI),
                description = "Vortex gateway completing the current level and unlocking highscores.",
                primaryColorHex = 0xFF7209B7,
                secondaryColorHex = 0xFF4CC9F0,
                previewIcon = "flag",
                properties = mapOf("Trigger" to "Win Level", "Effect" to "Vortex Particle")
            )
        )

        list.add(
            GameAsset(
                id = "env_locked_door_set",
                title = "Gold & Silver Security Gates",
                category = AssetCategory.ENVIRONMENTS,
                tags = listOf(SpriteTag.TILE, SpriteTag.ITEM),
                description = "Heavy reinforced security barriers matching golden and silver keys.",
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFFC0C0C0,
                previewIcon = "lock",
                properties = mapOf("Requires" to "Key Collect", "Animation" to "Dissolve")
            )
        )

        // 3. UI ELEMENTS
        list.add(
            GameAsset(
                id = "ui_cyber_hud",
                title = "Neon Cyber HUD Gauges",
                category = AssetCategory.UI_ELEMENTS,
                tags = listOf(SpriteTag.HUD, SpriteTag.SCI_FI),
                description = "Futuristic health hearts, energy battery bar, and combo multiplier pill.",
                primaryColorHex = 0xFF00FFE0,
                secondaryColorHex = 0xFFFF0055,
                previewIcon = "favorite",
                properties = mapOf("Style" to "Segmented Bar", "Glow" to "Neon High-Contrast")
            )
        )

        list.add(
            GameAsset(
                id = "ui_retro_dpad",
                title = "Arcade Virtual Touch Controls",
                category = AssetCategory.UI_ELEMENTS,
                tags = listOf(SpriteTag.HUD, SpriteTag.RETRO),
                description = "Ergonomic 4-way direction cross and action buttons with haptic feedback.",
                primaryColorHex = 0xFF3A0CA3,
                secondaryColorHex = 0xFF4CC9F0,
                previewIcon = "gamepad",
                properties = mapOf("Layout" to "Floating Virtual Joystick & Dual Button", "Opacity" to "80%")
            )
        )

        list.add(
            GameAsset(
                id = "ui_dialogue_card",
                title = "RPG Story Dialogue Box",
                category = AssetCategory.UI_ELEMENTS,
                tags = listOf(SpriteTag.HUD, SpriteTag.FANTASY),
                description = "Clean parchment and dark-glass conversation window with typing effect.",
                primaryColorHex = 0xFF2B2D42,
                secondaryColorHex = 0xFFE0A96D,
                previewIcon = "chat_bubble",
                properties = mapOf("Avatar Frame" to "Yes", "Typing Speed" to "30ms/char")
            )
        )

        list.add(
            GameAsset(
                id = "ui_victory_banner",
                title = "Arcade Victory Podium",
                category = AssetCategory.UI_ELEMENTS,
                tags = listOf(SpriteTag.HUD, SpriteTag.RETRO),
                description = "Post-game breakdown showing time taken, coins collected, and rank badge (S/A/B).",
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFF7209B7,
                previewIcon = "emoji_events",
                properties = mapOf("Star Rating" to "1 to 3 Stars", "Confetti Effect" to "Enabled")
            )
        )

        // 4. SOUND EFFECTS
        list.add(
            GameAsset(
                id = "sfx_jump_sound",
                title = "Acrobatic Jump & Double Jump",
                category = AssetCategory.SOUND_EFFECTS,
                tags = listOf(SpriteTag.RETRO, SpriteTag.HERO),
                description = "Crisp square-wave pitch sweeps for jumping and double mid-air jumps.",
                primaryColorHex = 0xFF00F0FF,
                secondaryColorHex = 0xFF3A86FF,
                previewIcon = "arrow_upward",
                sfxType = "JUMP",
                properties = mapOf("Waveform" to "Square Wave", "Duration" to "120ms")
            )
        )

        list.add(
            GameAsset(
                id = "sfx_coin_chime",
                title = "Gold Coin Bell Chime",
                category = AssetCategory.SOUND_EFFECTS,
                tags = listOf(SpriteTag.ITEM, SpriteTag.RETRO),
                description = "Harmonic pure sine-wave double chime on collecting gold coins.",
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFFFFBE0B,
                previewIcon = "monetization_on",
                sfxType = "COIN",
                properties = mapOf("Waveform" to "Sine Wave B5/E6", "Pitch" to "High Bell")
            )
        )

        list.add(
            GameAsset(
                id = "sfx_laser_blaster",
                title = "Plasma Laser Blaster",
                category = AssetCategory.SOUND_EFFECTS,
                tags = listOf(SpriteTag.SCI_FI, SpriteTag.HERO),
                description = "High-energy sawtooth pew pew discharge for futuristic weapons.",
                primaryColorHex = 0xFFFF007F,
                secondaryColorHex = 0xFFFF5400,
                previewIcon = "flash_on",
                sfxType = "LASER_BLAST",
                properties = mapOf("Waveform" to "Sawtooth Downsweep", "Power" to "Loud")
            )
        )

        list.add(
            GameAsset(
                id = "sfx_explosion_blast",
                title = "Crunchy Blast Explosion",
                category = AssetCategory.SOUND_EFFECTS,
                tags = listOf(SpriteTag.ENEMY, SpriteTag.HAZARD),
                description = "Filtered white noise burst for defeating enemies, shattering crates and bombs.",
                primaryColorHex = 0xFFFF0000,
                secondaryColorHex = 0xFFFF7900,
                previewIcon = "scatter_plot",
                sfxType = "EXPLOSION",
                properties = mapOf("Waveform" to "Shaped Noise Burst", "Duration" to "220ms")
            )
        )

        list.add(
            GameAsset(
                id = "sfx_victory_fanfare",
                title = "Grand Victory Fanfare",
                category = AssetCategory.SOUND_EFFECTS,
                tags = listOf(SpriteTag.RETRO, SpriteTag.ITEM),
                description = "Ascending major chord fanfare to celebrate winning levels.",
                primaryColorHex = 0xFF52B788,
                secondaryColorHex = 0xFFFFD700,
                previewIcon = "military_tech",
                sfxType = "VICTORY",
                properties = mapOf("Chords" to "C - E - G - High C", "Vibe" to "Triumphant")
            )
        )

        // 5. BACKGROUND MUSIC
        list.add(
            GameAsset(
                id = "bgm_cyber_synth",
                title = "Cyberpunk Neon Drive",
                category = AssetCategory.BACKGROUND_MUSIC,
                tags = listOf(SpriteTag.SCI_FI, SpriteTag.RETRO),
                description = "Fast 130 BPM synthwave arpeggios with pulsing basslines.",
                primaryColorHex = 0xFF00FFE0,
                secondaryColorHex = 0xFFFF0055,
                previewIcon = "graphic_eq",
                bgmType = "CYBER_SYNTH",
                properties = mapOf("BPM" to "130", "Key" to "A Minor Synth", "Loop" to "Infinite")
            )
        )

        list.add(
            GameAsset(
                id = "bgm_hero_quest",
                title = "8-Bit Hero Journey",
                category = AssetCategory.BACKGROUND_MUSIC,
                tags = listOf(SpriteTag.RETRO, SpriteTag.FANTASY),
                description = "Upbeat classic chiptune melody inspiring brave platforming exploration.",
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFF3A86FF,
                previewIcon = "music_note",
                bgmType = "HERO_QUEST",
                properties = mapOf("BPM" to "120", "Key" to "C Major Chiptune", "Mood" to "Uplifting")
            )
        )

        list.add(
            GameAsset(
                id = "bgm_dungeon_mystery",
                title = "Crypt of Shadows",
                category = AssetCategory.BACKGROUND_MUSIC,
                tags = listOf(SpriteTag.FANTASY),
                description = "Atmospheric minor key exploration theme for dark catacombs and castles.",
                primaryColorHex = 0xFF7209B7,
                secondaryColorHex = 0xFF3D2C4D,
                previewIcon = "nightlight",
                bgmType = "DUNGEON_MYSTERY",
                properties = mapOf("BPM" to "95", "Key" to "D Minor Mystic", "Mood" to "Tense Mystery")
            )
        )

        list.add(
            GameAsset(
                id = "bgm_boss_rage",
                title = "Boss Rage Battle",
                category = AssetCategory.BACKGROUND_MUSIC,
                tags = listOf(SpriteTag.BOSS, SpriteTag.SCI_FI),
                description = "Pounding high-tempo adrenaline music for intense boss showdowns.",
                primaryColorHex = 0xFFFF0000,
                secondaryColorHex = 0xFFFFD000,
                previewIcon = "whatshot",
                bgmType = "BOSS_Frenzy",
                properties = mapOf("BPM" to "145", "Key" to "E Minor Heavy", "Mood" to "Intense Battle")
            )
        )

        list.add(
            GameAsset(
                id = "bgm_arcade_fever",
                title = "Arcade Fever Breakout",
                category = AssetCategory.BACKGROUND_MUSIC,
                tags = listOf(SpriteTag.RETRO),
                description = "Bouncy rhythmic arcade theme tuned for paddle and puzzle games.",
                primaryColorHex = 0xFFFF70A6,
                secondaryColorHex = 0xFFFF9770,
                previewIcon = "videogame_asset",
                bgmType = "ARCADE_FEVER",
                properties = mapOf("BPM" to "128", "Key" to "G Major Bounce", "Mood" to "Energetic")
            )
        )

        list
    }

    fun getAllAssets(): List<GameAsset> {
        return customAssets + defaultAssets
    }

    fun addCustomAsset(asset: GameAsset) {
        customAssets.add(0, asset)
    }
}
