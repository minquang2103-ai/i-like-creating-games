package com.example.community

import com.example.model.GameGenre
import com.example.model.GameProject
import com.example.model.GameReview
import com.example.model.HighscoreEntry
import com.example.model.PresetGames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object CommunityManager {

    private val _communityGames = MutableStateFlow<List<GameProject>>(emptyList())
    val communityGames: StateFlow<List<GameProject>> = _communityGames.asStateFlow()

    private val _myProjects = MutableStateFlow<List<GameProject>>(emptyList())
    val myProjects: StateFlow<List<GameProject>> = _myProjects.asStateFlow()

    init {
        val presets = PresetGames.getAllPresets()
        _communityGames.value = presets
        _myProjects.value = listOf(
            PresetGames.createDefaultPlatformer().copy(
                id = "my_proj_1",
                title = "My First Platformer Quest",
                author = "You",
                playCount = 12,
                likesCount = 4,
                isPublished = false
            )
        )
    }

    fun publishGame(project: GameProject): GameProject {
        val publishedCopy = project.copy(
            id = if (project.id.startsWith("preset_")) UUID.randomUUID().toString() else project.id,
            isPublished = true,
            createdAt = System.currentTimeMillis(),
            shareCode = generateShareCode(project.title)
        )

        // Add to community list if not present or update
        val currentList = _communityGames.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == publishedCopy.id }
        if (index >= 0) {
            currentList[index] = publishedCopy
        } else {
            currentList.add(0, publishedCopy)
        }
        _communityGames.value = currentList

        // Update local project status
        val myCurrent = _myProjects.value.toMutableList()
        val myIdx = myCurrent.indexOfFirst { it.id == project.id }
        if (myIdx >= 0) {
            myCurrent[myIdx] = publishedCopy
            _myProjects.value = myCurrent
        }

        return publishedCopy
    }

    fun saveLocalProject(project: GameProject) {
        val current = _myProjects.value.toMutableList()
        val idx = current.indexOfFirst { it.id == project.id }
        if (idx >= 0) {
            current[idx] = project
        } else {
            current.add(0, project)
        }
        _myProjects.value = current
    }

    fun createNewProject(title: String, genre: GameGenre): GameProject {
        val base = when (genre) {
            GameGenre.PLATFORMER -> PresetGames.createDefaultPlatformer()
            GameGenre.DUNGEON_RPG -> PresetGames.createDungeonRpg()
            GameGenre.BRICK_BREAKER -> PresetGames.createBrickBreaker()
            GameGenre.STEALTH_MAZE -> PresetGames.createStealthMaze()
            GameGenre.TOWER_DEFENSE -> PresetGames.createTowerDefense()
        }
        val newProj = base.copy(
            id = UUID.randomUUID().toString(),
            title = title,
            author = "You",
            isPublished = false,
            playCount = 0,
            likesCount = 0,
            isLikedByUser = false,
            createdAt = System.currentTimeMillis(),
            shareCode = ""
        )
        saveLocalProject(newProj)
        return newProj
    }

    fun toggleLike(gameId: String) {
        val current = _communityGames.value.toMutableList()
        val idx = current.indexOfFirst { it.id == gameId }
        if (idx >= 0) {
            val game = current[idx]
            val newLiked = !game.isLikedByUser
            val newCount = if (newLiked) game.likesCount + 1 else (game.likesCount - 1).coerceAtLeast(0)
            current[idx] = game.copy(isLikedByUser = newLiked, likesCount = newCount)
            _communityGames.value = current
        }
    }

    fun addReview(gameId: String, author: String, rating: Int, comment: String) {
        val current = _communityGames.value.toMutableList()
        val idx = current.indexOfFirst { it.id == gameId }
        if (idx >= 0) {
            val game = current[idx]
            val review = GameReview(
                author = author.ifBlank { "Player" },
                rating = rating.coerceIn(1, 5),
                comment = comment
            )
            val updatedReviews = (listOf(review) + game.reviews).toMutableList()
            val totalStars = updatedReviews.sumOf { it.rating }
            val newAvg = (totalStars.toFloat() / updatedReviews.size).let { (Math.round(it * 10) / 10.0f) }

            current[idx] = game.copy(
                reviews = updatedReviews,
                rating = newAvg
            )
            _communityGames.value = current
        }
    }

    fun recordPlayAndScore(gameId: String, playerName: String, score: Int, timeSeconds: Int) {
        val current = _communityGames.value.toMutableList()
        val idx = current.indexOfFirst { it.id == gameId }
        if (idx >= 0) {
            val game = current[idx]
            val newHighscores = (game.highscores + HighscoreEntry(
                playerName = playerName.ifBlank { "Hero" },
                score = score,
                timeSeconds = timeSeconds
            )).sortedByDescending { it.score }.take(10).toMutableList()

            current[idx] = game.copy(
                playCount = game.playCount + 1,
                highscores = newHighscores
            )
            _communityGames.value = current
        }
    }

    fun forkGameToMyProjects(game: GameProject): GameProject {
        val forked = game.copy(
            id = UUID.randomUUID().toString(),
            title = "${game.title} (Remix)",
            author = "You",
            isPublished = false,
            playCount = 0,
            likesCount = 0,
            isLikedByUser = false,
            createdAt = System.currentTimeMillis(),
            shareCode = ""
        )
        saveLocalProject(forked)
        return forked
    }

    private fun generateShareCode(title: String): String {
        val prefix = title.take(4).uppercase().replace(" ", "X")
        val num = (1000..9999).random()
        return "$prefix-$num"
    }
}
