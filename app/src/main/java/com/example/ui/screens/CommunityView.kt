package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.community.CommunityManager
import com.example.model.GameGenre
import com.example.model.GameProject
import com.example.model.GameReview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityView(
    onPlayGame: (GameProject) -> Unit,
    onRemixGame: (GameProject) -> Unit,
    onPublishProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val communityGames by CommunityManager.communityGames.collectAsState()
    val myProjects by CommunityManager.myProjects.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Trending, 1: Top Rated, 2: New, 3: My Uploads
    var selectedGenreFilter by remember { mutableStateOf<GameGenre?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGameForDetails by remember { mutableStateOf<GameProject?>(null) }
    var showShareCodeDialog by remember { mutableStateOf(false) }
    var shareCodeInput by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    val tabTitles = listOf("🔥 Trending & Featured", "⭐ Top Rated", "✨ New Releases", "🚀 My Uploads")

    // Filter games
    val sourceList = if (activeTab == 3) myProjects.filter { it.isPublished } else communityGames

    val sortedList = when (activeTab) {
        0 -> sourceList.sortedByDescending { it.playCount + it.likesCount * 3 }
        1 -> sourceList.sortedByDescending { it.rating }
        2 -> sourceList.sortedByDescending { it.createdAt }
        else -> sourceList
    }

    val filteredGames = sortedList.filter { game ->
        val matchesGenre = selectedGenreFilter == null || game.genre == selectedGenreFilter
        val matchesSearch = searchQuery.isBlank() ||
                game.title.contains(searchQuery, ignoreCase = true) ||
                game.author.contains(searchQuery, ignoreCase = true) ||
                game.tags.any { it.contains(searchQuery, ignoreCase = true) }
        matchesGenre && matchesSearch
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C091C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arcade Community Hub",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Play creator games, leave reviews, and share your levels",
                        fontSize = 13.sp,
                        color = Color(0xFFA0A0BA)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showShareCodeDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Code", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onPublishProject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publish", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search game titles, authors, or genres...", color = Color(0xFF7E7E94)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF00F0FF))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF272146),
                    focusedContainerColor = Color(0xFF16122C),
                    unfocusedContainerColor = Color(0xFF16122C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF00F0FF),
                        height = 3.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == index) Color(0xFF00F0FF) else Color(0xFF9E9EB8)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Genre Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedGenreFilter == null,
                        onClick = { selectedGenreFilter = null },
                        label = { Text("All Genres", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF),
                            selectedLabelColor = Color(0xFF0C091C),
                            containerColor = Color(0xFF1E183B),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }
                items(GameGenre.values()) { genre ->
                    FilterChip(
                        selected = selectedGenreFilter == genre,
                        onClick = { selectedGenreFilter = if (selectedGenreFilter == genre) null else genre },
                        label = { Text(genre.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF0055),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E183B),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Community Games
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTab == 3) "You haven't uploaded any games yet.\nCreate one in the Studio and tap Publish!" else "No community games found.",
                        color = Color(0xFF7E7E94),
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        CommunityGameCard(
                            game = game,
                            onPlay = { onPlayGame(game) },
                            onLike = { CommunityManager.toggleLike(game.id) },
                            onOpenDetails = { selectedGameForDetails = game },
                            onRemix = {
                                val forked = CommunityManager.forkGameToMyProjects(game)
                                onRemixGame(forked)
                                notificationMessage = "Remixed '${game.title}' into your Studio projects!"
                            }
                        )
                    }
                }
            }
        }

        // Notification Banner
        notificationMessage?.let { msg ->
            Surface(
                color = Color(0xFF00E5FF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF0F0C20))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { notificationMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF0F0C20))
                    }
                }
            }
        }

        // Community Game Detail & Rating Dialog
        selectedGameForDetails?.let { game ->
            val liveGame = communityGames.firstOrNull { it.id == game.id } ?: game

            CommunityGameDetailDialog(
                game = liveGame,
                onDismiss = { selectedGameForDetails = null },
                onPlay = {
                    selectedGameForDetails = null
                    onPlayGame(liveGame)
                },
                onRemix = {
                    val forked = CommunityManager.forkGameToMyProjects(liveGame)
                    selectedGameForDetails = null
                    onRemixGame(forked)
                    notificationMessage = "Remixed '${liveGame.title}' into your Studio projects!"
                },
                onAddReview = { author, stars, comment ->
                    CommunityManager.addReview(liveGame.id, author, stars, comment)
                    notificationMessage = "Thank you! Review submitted."
                }
            )
        }

        // Share Code Loader Dialog
        if (showShareCodeDialog) {
            AlertDialog(
                onDismissRequest = { showShareCodeDialog = false },
                containerColor = Color(0xFF171330),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text("Load Game by Share Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Enter a 8-digit share code (e.g., NEON-RUN-9921) to instantly load and play any community game:",
                            color = Color(0xFFA0A0BA),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = shareCodeInput,
                            onValueChange = { shareCodeInput = it.uppercase() },
                            placeholder = { Text("NEON-RUN-9921", color = Color(0xFF6E6E88)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00F0FF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val matched = communityGames.firstOrNull { it.shareCode.equals(shareCodeInput.trim(), ignoreCase = true) }
                            if (matched != null) {
                                showShareCodeDialog = false
                                onPlayGame(matched)
                            } else {
                                notificationMessage = "Invalid or expired share code."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Play Game", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showShareCodeDialog = false }, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityGameCard(
    game: GameProject,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onOpenDetails: () -> Unit,
    onRemix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = game.theme
    val primaryColor = Color(theme.primaryColor)
    val accentColor = Color(theme.accentColor)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16122E)),
        border = BorderStroke(1.dp, Color(0xFF2C2550))
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(theme.bgGradientStart), Color(theme.bgGradientEnd))
                        )
                    )
                    .padding(12.dp)
            ) {
                // Genre & Difficulty Badges
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = game.genre.displayName,
                            color = Color(0xFF0F0C20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF251E45).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = game.difficulty,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Rating & Share Code
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0D0A1C).copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", game.rating),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = " (${game.reviews.size})",
                            color = Color(0xFFA0A0BA),
                            fontSize = 10.sp
                        )
                    }

                    if (game.shareCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#${game.shareCode}",
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Big Game Title in Banner
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            // Body content
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = game.description,
                    fontSize = 12.sp,
                    color = Color(0xFFC0C0D4),
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    game.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF221D42)
                        ) {
                            Text(
                                text = "#$tag",
                                color = accentColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer with Stats & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author & Plays
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "By ${game.author}",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9EB8),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "🎮 ${game.playCount} plays",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9EB8)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onLike() }
                        ) {
                            Icon(
                                imageVector = if (game.isLikedByUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (game.isLikedByUser) Color(0xFFFF0055) else Color(0xFF9E9EB8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${game.likesCount}",
                                fontSize = 11.sp,
                                color = if (game.isLikedByUser) Color(0xFFFF0055) else Color(0xFF9E9EB8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Action Buttons (Remix & Play)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF292250),
                            modifier = Modifier.clickable { onRemix() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ForkRight, contentDescription = "Remix", tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Remix", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onPlay,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF0F0C20), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Play", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityGameDetailDialog(
    game: GameProject,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onRemix: () -> Unit,
    onAddReview: (author: String, rating: Int, comment: String) -> Unit
) {
    var userRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var showReviewInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16122E),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = game.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = game.description, color = Color(0xFFD4D4E8), fontSize = 13.sp, lineHeight = 18.sp)
                }

                // Stats Pill Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF221C44))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐ Rating", color = Color(0xFF9E9EB8), fontSize = 11.sp)
                            Text("${game.rating} / 5", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎮 Plays", color = Color(0xFF9E9EB8), fontSize = 11.sp)
                            Text("${game.playCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("❤️ Likes", color = Color(0xFF9E9EB8), fontSize = 11.sp)
                            Text("${game.likesCount}", color = Color(0xFFFF0055), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡ Genre", color = Color(0xFF9E9EB8), fontSize = 11.sp)
                            Text(game.genre.displayName, color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Highscores Leaderboard
                item {
                    Text("🏆 Hall of Fame Leaderboard", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (game.highscores.isEmpty()) {
                        Text("Be the first to set a high score!", color = Color(0xFF7E7E94), fontSize = 12.sp)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1D173B))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            game.highscores.take(5).forEachIndexed { idx, hs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${hs.playerName}",
                                        color = if (idx == 0) Color(0xFFFFD700) else Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${hs.score} pts (${hs.timeSeconds}s)",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Reviews & Comments Header with "Rate this Game" button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬 Player Reviews (${game.reviews.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        OutlinedButton(
                            onClick = { showReviewInput = !showReviewInput },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RateReview, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Game", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                // Review input form
                if (showReviewInput) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF241D48)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Select Star Rating:", color = Color(0xFFA0A0BA), fontSize = 11.sp)
                                Row {
                                    (1..5).forEach { star ->
                                        IconButton(onClick = { userRating = star }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "$star stars",
                                                tint = Color(0xFFFFD700)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = authorName,
                                    onValueChange = { authorName = it },
                                    label = { Text("Your Gamer Tag", color = Color(0xFF9E9EB8)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = reviewComment,
                                    onValueChange = { reviewComment = it },
                                    label = { Text("Write your review...", color = Color(0xFF9E9EB8)) },
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (reviewComment.isNotBlank()) {
                                            onAddReview(authorName, userRating, reviewComment)
                                            reviewComment = ""
                                            showReviewInput = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Submit Review", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Reviews List
                items(game.reviews) { rev ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D173B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = rev.author, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Row {
                                    (1..rev.rating).forEach {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = rev.comment, color = Color(0xFFC0C0D8), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRemix, shape = RoundedCornerShape(10.dp)) {
                    Icon(imageVector = Icons.Default.ForkRight, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remix Studio", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0F0C20), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Now", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {}
    )
}
