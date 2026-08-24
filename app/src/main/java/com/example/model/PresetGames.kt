package com.example.model

import java.util.UUID

object PresetGames {

    fun createDefaultPlatformer(): GameProject {
        val level1 = GameLevel(
            id = "plat_lvl_1",
            title = "Neon Citadel: Act 1",
            width = 28,
            height = 14,
            gridData = listOf(
                "............................",
                "............................",
                "....................X.......",
                "..................#####.....",
                ".............**.............",
                "..........======............",
                "......oo....................",
                "....#####..........k........",
                ".................=====......",
                "..oo........................",
                "#####.......U...D...........",
                "......^^..####.######.......",
                "############################",
                "############################"
            ),
            entities = mutableListOf(
                GameEntityConfig(
                    id = "p1",
                    type = EntityType.PLAYER_SPAWN,
                    gridX = 1,
                    gridY = 9,
                    name = "Cyber Runner"
                ),
                GameEntityConfig(
                    id = "e1",
                    type = EntityType.ENEMY_PATROL,
                    gridX = 6,
                    gridY = 6,
                    patrolRange = 3,
                    speed = 1.5f,
                    name = "Drone Patrol"
                ),
                GameEntityConfig(
                    id = "e2",
                    type = EntityType.ENEMY_BOUNCER,
                    gridX = 16,
                    gridY = 9,
                    patrolRange = 2,
                    speed = 2.0f,
                    name = "Laser Slime"
                ),
                GameEntityConfig(
                    id = "npc1",
                    type = EntityType.NPC_GUIDE,
                    gridX = 3,
                    gridY = 9,
                    name = "Cipher",
                    dialogText = "Collect the Gold Key [k] to unlock the security door [D] and reach the terminal flag [X]!"
                )
            )
        )

        return GameProject(
            id = "preset_neon_runner",
            title = "Neon Cyber Runner",
            description = "Sprint, leap across laser spires, dodge patrol sentries, unlock secure blast doors, and reach the exit portal in this high-octane 2D platformer!",
            author = "PixelForge",
            genre = GameGenre.PLATFORMER,
            theme = GameTheme.RETRO_NEON,
            difficulty = "Medium",
            tags = listOf("Platformer", "Neon", "Sci-Fi", "Action"),
            playCount = 1420,
            likesCount = 388,
            rating = 4.9f,
            isPublished = true,
            config = GameConfig(
                gravity = 0.55f,
                jumpForce = 11.5f,
                moveSpeed = 4.8f,
                playerMaxHealth = 5,
                startingLives = 3,
                allowDoubleJump = true,
                hasAttack = true,
                winCondition = "Reach the Goal Flag [X]"
            ),
            levels = mutableListOf(level1),
            highscores = mutableListOf(
                HighscoreEntry(playerName = "Kira", score = 3450, timeSeconds = 42),
                HighscoreEntry(playerName = "Vex", score = 3100, timeSeconds = 48),
                HighscoreEntry(playerName = "Ghost", score = 2800, timeSeconds = 55)
            ),
            reviews = mutableListOf(
                GameReview(author = "Alex_99", rating = 5, comment = "The jump physics feel super crisp! Love the double jump."),
                GameReview(author = "ArcadeKing", rating = 5, comment = "Awesome theme and tight platforming challenges.")
            ),
            shareCode = "NEON-RUN-9921"
        )
    }

    fun createDungeonRpg(): GameProject {
        val level1 = GameLevel(
            id = "rpg_lvl_1",
            title = "Catacombs of Eldoria",
            width = 24,
            height = 14,
            gridData = listOf(
                "########################",
                "#...k...#......#...C...#",
                "#.BBB...#..**..#.BBBBB.#",
                "#.......D......#.......#",
                "#####.#######..#####.###",
                "#...........#..........#",
                "#...o...o...#....^^....#",
                "#...........#..........#",
                "###.#####.########.#####",
                "#...#...#....s.....#...#",
                "#.C.#.#.#..........#...#",
                "#...#.#.#....S.....#...X",
                "#...#...#..........#...#",
                "########################"
            ),
            entities = mutableListOf(
                GameEntityConfig(
                    id = "hero1",
                    type = EntityType.PLAYER_SPAWN,
                    gridX = 2,
                    gridY = 6,
                    name = "Knight of Valor"
                ),
                GameEntityConfig(
                    id = "skel1",
                    type = EntityType.ENEMY_PATROL,
                    gridX = 7,
                    gridY = 2,
                    health = 3,
                    speed = 1.2f,
                    damage = 1,
                    name = "Skeleton Sentry"
                ),
                GameEntityConfig(
                    id = "skel2",
                    type = EntityType.ENEMY_SHOOTER,
                    gridX = 14,
                    gridY = 6,
                    health = 2,
                    speed = 0.8f,
                    name = "Cursed Archer"
                ),
                GameEntityConfig(
                    id = "boss1",
                    type = EntityType.ENEMY_BOSS,
                    gridX = 20,
                    gridY = 2,
                    health = 8,
                    speed = 1.5f,
                    damage = 2,
                    name = "Crypt Guardian"
                ),
                GameEntityConfig(
                    id = "wizard",
                    type = EntityType.NPC_GUIDE,
                    gridX = 1,
                    gridY = 7,
                    name = "Old Wizard",
                    dialogText = "Brave warrior! Smash crates [B], search for keys [k, s], defeat the guardian and reach the portal [X]!"
                )
            )
        )

        return GameProject(
            id = "preset_catacombs_rpg",
            title = "Catacombs of Eldoria",
            description = "Explore a dark medieval dungeon, smash crates for loot, solve door locks, wield magic blades against undead sentries, and claim the ancient relic!",
            author = "DragonLord",
            genre = GameGenre.DUNGEON_RPG,
            theme = GameTheme.FANTASY_DUNGEON,
            difficulty = "Hard",
            tags = listOf("RPG", "Dungeon", "Fantasy", "Action"),
            playCount = 2190,
            likesCount = 594,
            rating = 4.95f,
            isPublished = true,
            config = GameConfig(
                moveSpeed = 4.2f,
                playerMaxHealth = 6,
                startingLives = 3,
                playerAttackDamage = 2,
                hasAttack = true,
                winCondition = "Find the keys, defeat the Boss and reach Exit Flag"
            ),
            levels = mutableListOf(level1),
            highscores = mutableListOf(
                HighscoreEntry(playerName = "ShadowBlade", score = 5200, timeSeconds = 74),
                HighscoreEntry(playerName = "Eldor", score = 4850, timeSeconds = 88)
            ),
            reviews = mutableListOf(
                GameReview(author = "MysticKnight", rating = 5, comment = "Boss fight was intense! Great layout design.")
            ),
            shareCode = "ELDO-RPG-4402"
        )
    }

    fun createBrickBreaker(): GameProject {
        val level1 = GameLevel(
            id = "brick_lvl_1",
            title = "Neon Vaporwave Breakout",
            width = 20,
            height = 14,
            gridData = listOf(
                "####################",
                "#..................#",
                "#.****************.#",
                "#.oooooooooooooooo.#",
                "#.BBBBBBBBBBBBBBBB.#",
                "#.****************.#",
                "#.oooooooooooooooo.#",
                "#..................#",
                "#..................#",
                "#..................#",
                "#..................#",
                "#..................#",
                "#........===.......#",
                "#..................#"
            ),
            entities = mutableListOf(
                GameEntityConfig(
                    id = "power1",
                    type = EntityType.POWERUP_BLASTER,
                    gridX = 10,
                    gridY = 3,
                    name = "Multi-Ball & Laser"
                )
            )
        )

        return GameProject(
            id = "preset_neon_breakout",
            title = "Hyper Neon Breakout",
            description = "Smash vibrant glowing bricks with high-velocity paddle physics, unleash multi-ball laser storm powerups, and clear the retro synth grid!",
            author = "VaporWaveDev",
            genre = GameGenre.BRICK_BREAKER,
            theme = GameTheme.CYBERPUNK,
            difficulty = "Easy",
            tags = listOf("Arcade", "Retro", "Bricks", "Fast-Paced"),
            playCount = 1850,
            likesCount = 420,
            rating = 4.85f,
            isPublished = true,
            config = GameConfig(
                moveSpeed = 7.0f,
                playerMaxHealth = 3,
                startingLives = 3,
                winCondition = "Destroy all glowing bricks!"
            ),
            levels = mutableListOf(level1),
            highscores = mutableListOf(
                HighscoreEntry(playerName = "Nova", score = 12400, timeSeconds = 62),
                HighscoreEntry(playerName = "Pulse", score = 10900, timeSeconds = 75)
            ),
            reviews = mutableListOf(
                GameReview(author = "SynthFan", rating = 5, comment = "So satisfying to shatter whole rows of bricks.")
            ),
            shareCode = "BRICK-NEON-881"
        )
    }

    fun createStealthMaze(): GameProject {
        val level1 = GameLevel(
            id = "stealth_lvl_1",
            title = "Sector 7 Infiltration",
            width = 24,
            height = 14,
            gridData = listOf(
                "########################",
                "#...#......#...o...#...#",
                "#.T.#.####.#.#####.#.#.#",
                "#...#.#..#.#.#...#.#.#.#",
                "###.###..#.#.###.#.###.#",
                "#.....#..#.#...#.#.....#",
                "#.###.####.###.#.#####.#",
                "#...#........#.#.......#",
                "###.##########.#####.###",
                "#...#....k...#.#...#...#",
                "#.###.######.#.#.#.###.#",
                "#...L......#.#...#...#.#",
                "#...#......#.#####.D.#.X",
                "########################"
            ),
            entities = mutableListOf(
                GameEntityConfig(
                    id = "agent",
                    type = EntityType.PLAYER_SPAWN,
                    gridX = 1,
                    gridY = 1,
                    name = "Infiltrator"
                ),
                GameEntityConfig(
                    id = "guard1",
                    type = EntityType.ENEMY_PATROL,
                    gridX = 6,
                    gridY = 1,
                    speed = 1.0f,
                    patrolRange = 4,
                    name = "Security Sentry Alpha"
                ),
                GameEntityConfig(
                    id = "guard2",
                    type = EntityType.ENEMY_PATROL,
                    gridX = 14,
                    gridY = 7,
                    speed = 1.2f,
                    patrolRange = 5,
                    name = "Laser Drone Beta"
                ),
                GameEntityConfig(
                    id = "npc_comm",
                    type = EntityType.NPC_GUIDE,
                    gridX = 2,
                    gridY = 1,
                    name = "Handler",
                    dialogText = "Agent: Avoid the guard search cones! Toggle the terminal [T] to disable the laser grid [L], grab key [k] and extract at [X]!"
                )
            )
        )

        return GameProject(
            id = "preset_stealth_heist",
            title = "Shadow Heist: Sector 7",
            description = "Slip past high-tech security guards, avoid laser tripwires, hack terminal switches, and extract with the master keycard!",
            author = "AgentZero",
            genre = GameGenre.STEALTH_MAZE,
            theme = GameTheme.CYBERPUNK,
            difficulty = "Medium",
            tags = listOf("Stealth", "Maze", "Puzzles", "Tactical"),
            playCount = 1310,
            likesCount = 340,
            rating = 4.78f,
            isPublished = true,
            config = GameConfig(
                moveSpeed = 4.0f,
                playerMaxHealth = 3,
                startingLives = 3,
                winCondition = "Infiltrate and reach extraction point [X]"
            ),
            levels = mutableListOf(level1),
            highscores = mutableListOf(
                HighscoreEntry(playerName = "SolidSam", score = 4200, timeSeconds = 49),
                HighscoreEntry(playerName = "Ghost99", score = 3800, timeSeconds = 61)
            ),
            reviews = mutableListOf(
                GameReview(author = "SpyGamer", rating = 5, comment = "Great tension dodging the guard vision cones!")
            ),
            shareCode = "STEALTH-S7-101"
        )
    }

    fun createTowerDefense(): GameProject {
        val level1 = GameLevel(
            id = "td_lvl_1",
            title = "Frontier Outpost Defense",
            width = 24,
            height = 14,
            gridData = listOf(
                "########################",
                "#......................#",
                "#..##################..#",
                "#..#................#..#",
                "#..#..############..#..#",
                "#..#..#..........#..#..#",
                "#..#..#..######..#..#..#",
                "#..#..#..#....#..#..#..#",
                "#..#..#..#..X.#..#..#..#",
                "#..#..#..#....#..#..#..#",
                "#..#..#..######..#..#..#",
                "#..#..#..........#..#..#",
                "#..#..############..#..#",
                "########################"
            ),
            entities = mutableListOf(
                GameEntityConfig(
                    id = "spawner",
                    type = EntityType.ENEMY_PATROL,
                    gridX = 1,
                    gridY = 1,
                    name = "Creep Wave Spawner"
                )
            )
        )

        return GameProject(
            id = "preset_tower_defense",
            title = "Kingdom Sentry Defense",
            description = "Build archery towers, explosive cannons, and frost turrets along the winding path to stop waves of marauders from storming the stronghold!",
            author = "CastleMarshal",
            genre = GameGenre.TOWER_DEFENSE,
            theme = GameTheme.FOREST_QUEST,
            difficulty = "Medium",
            tags = listOf("Strategy", "Tower Defense", "Upgrades", "Waves"),
            playCount = 1640,
            likesCount = 475,
            rating = 4.88f,
            isPublished = true,
            config = GameConfig(
                playerMaxHealth = 20,
                startingLives = 1,
                winCondition = "Survive all 5 enemy creep waves!"
            ),
            levels = mutableListOf(level1),
            highscores = mutableListOf(
                HighscoreEntry(playerName = "Archon", score = 8900, timeSeconds = 120),
                HighscoreEntry(playerName = "GeneralG", score = 7600, timeSeconds = 135)
            ),
            reviews = mutableListOf(
                GameReview(author = "Tactician", rating = 5, comment = "Balancing towers between splash and frost is key!")
            ),
            shareCode = "TD-KINGDOM-777"
        )
    }

    fun getAllPresets(): List<GameProject> {
        return listOf(
            createDefaultPlatformer(),
            createDungeonRpg(),
            createBrickBreaker(),
            createStealthMaze(),
            createTowerDefense()
        )
    }
}
