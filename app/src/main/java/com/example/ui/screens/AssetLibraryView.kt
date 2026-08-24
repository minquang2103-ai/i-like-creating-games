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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.asset.AssetCategory
import com.example.asset.DefaultAssetRepository
import com.example.asset.GameAsset
import com.example.asset.SpritePixelMatrix
import com.example.asset.SpriteTag
import com.example.audio.GameSoundSynthesizer
import com.example.model.GameProject
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.PixelArtCanvas
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssetLibraryView(
    activeProject: GameProject?,
    onImportAssetToProject: (GameAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<SpriteTag?>(null) }
    var selectedAssetForDetail by remember { mutableStateOf<GameAsset?>(null) }
    var showPixelArtCreator by remember { mutableStateOf(false) }
    var currentlyPlayingAudioId by remember { mutableStateOf<String?>(null) }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(null) + AssetCategory.values().toList()
    val allAssets = remember { mutableStateListOf<GameAsset>().apply { addAll(DefaultAssetRepository.getAllAssets()) } }

    val filteredAssets = allAssets.filter { asset ->
        val matchesCategory = selectedCategoryIndex == 0 || asset.category == categories[selectedCategoryIndex]
        val matchesSearch = searchQuery.isBlank() || asset.title.contains(searchQuery, ignoreCase = true) || asset.description.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTagFilter == null || asset.tags.contains(selectedTagFilter)
        matchesCategory && matchesSearch && matchesTag
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
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
                        text = "Asset Vault & Creator",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Browse, preview & import characters, tiles, SFX & music",
                        fontSize = 13.sp,
                        color = Color(0xFFA0A0B8)
                    )
                }

                Button(
                    onClick = { showPixelArtCreator = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "New Sprite",
                        tint = Color(0xFF0F0C20),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Sprite",
                        color = Color(0xFF0F0C20),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search 8-bit sprites, sound effects, BGM...", color = Color(0xFF7E7E94)) },
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
                    unfocusedBorderColor = Color(0xFF2B264A),
                    focusedContainerColor = Color(0xFF181432),
                    unfocusedContainerColor = Color(0xFF181432),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                        color = Color(0xFF00F0FF),
                        height = 3.dp
                    )
                }
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Text(
                                text = cat?.displayName ?: "All Assets (${allAssets.size})",
                                fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryIndex == index) Color(0xFF00F0FF) else Color(0xFF9E9EB8)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tag Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTagFilter == null,
                        onClick = { selectedTagFilter = null },
                        label = { Text("All Tags", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF),
                            selectedLabelColor = Color(0xFF0F0C20),
                            containerColor = Color(0xFF1E1A38),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }
                items(SpriteTag.values()) { tag ->
                    FilterChip(
                        selected = selectedTagFilter == tag,
                        onClick = { selectedTagFilter = if (selectedTagFilter == tag) null else tag },
                        label = { Text(tag.name.replace("_", " "), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF0055),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E1A38),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Asset Cards
            if (filteredAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assets found matching your criteria.",
                        color = Color(0xFF7E7E94),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAssets, key = { it.id }) { asset ->
                        val isAudio = asset.category == AssetCategory.SOUND_EFFECTS || asset.category == AssetCategory.BACKGROUND_MUSIC
                        val isPlaying = currentlyPlayingAudioId == asset.id

                        AssetCard(
                            asset = asset,
                            isPlaying = isPlaying,
                            onPlayAudio = {
                                if (isPlaying) {
                                    GameSoundSynthesizer.stopMusic()
                                    currentlyPlayingAudioId = null
                                } else {
                                    currentlyPlayingAudioId = asset.id
                                    if (asset.sfxType != null) {
                                        val sfx = GameSoundSynthesizer.SoundFx.values().firstOrNull { it.name == asset.sfxType }
                                            ?: GameSoundSynthesizer.SoundFx.COIN
                                        GameSoundSynthesizer.playSfx(sfx)
                                    } else if (asset.bgmType != null) {
                                        val bgm = GameSoundSynthesizer.MusicTheme.values().firstOrNull { it.name == asset.bgmType }
                                            ?: GameSoundSynthesizer.MusicTheme.CYBER_SYNTH
                                        GameSoundSynthesizer.startMusic(bgm)
                                    }
                                }
                            },
                            onClick = { selectedAssetForDetail = asset },
                            onQuickImport = {
                                onImportAssetToProject(asset)
                                importSuccessMessage = "Imported '${asset.title}' to ${activeProject?.title ?: "Project"}!"
                            }
                        )
                    }
                }
            }
        }

        // Notification Banner
        importSuccessMessage?.let { msg ->
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
                    IconButton(onClick = { importSuccessMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF0F0C20))
                    }
                }
            }
        }

        // Asset Detail Dialog
        selectedAssetForDetail?.let { asset ->
            AssetDetailDialog(
                asset = asset,
                onDismiss = { selectedAssetForDetail = null },
                onImport = {
                    onImportAssetToProject(asset)
                    importSuccessMessage = "Imported '${asset.title}' to ${activeProject?.title ?: "Project"}!"
                    selectedAssetForDetail = null
                }
            )
        }

        // Custom Pixel Art Creator Dialog
        if (showPixelArtCreator) {
            PixelArtCreatorDialog(
                onDismiss = { showPixelArtCreator = false },
                onSave = { newAsset ->
                    DefaultAssetRepository.addCustomAsset(newAsset)
                    allAssets.add(0, newAsset)
                    showPixelArtCreator = false
                    importSuccessMessage = "Created '${newAsset.title}' and saved to vault!"
                }
            )
        }
    }
}

@Composable
fun AssetCard(
    asset: GameAsset,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onClick: () -> Unit,
    onQuickImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(asset.primaryColorHex)
    val secondaryColor = Color(asset.secondaryColorHex)
    val isAudio = asset.category == AssetCategory.SOUND_EFFECTS || asset.category == AssetCategory.BACKGROUND_MUSIC

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17142E)),
        border = BorderStroke(1.dp, Color(0xFF2C274E))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Visual Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0D0A1C), primaryColor.copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (asset.pixelArt != null) {
                    PixelArtCanvas(
                        pixelArt = asset.pixelArt,
                        primaryColor = primaryColor,
                        modifier = Modifier.size(72.dp)
                    )
                } else if (isAudio) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.25f),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onPlayAudio() }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = primaryColor,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioWaveformVisualizer(isPlaying = isPlaying, accentColor = primaryColor)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Category pill top right
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primaryColor.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = asset.category.name.take(3),
                        color = Color(0xFF0F0C20),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asset.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = asset.description,
                fontSize = 11.sp,
                color = Color(0xFF9E9EB8),
                maxLines = 2,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.tags.firstOrNull()?.name ?: "ASSET",
                    fontSize = 10.sp,
                    color = secondaryColor,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onQuickImport() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Import",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetDetailDialog(
    asset: GameAsset,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    val primaryColor = Color(asset.primaryColorHex)
    val secondaryColor = Color(asset.secondaryColorHex)
    var isAudioPlaying by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (isAudioPlaying) GameSoundSynthesizer.stopMusic()
            onDismiss()
        },
        containerColor = Color(0xFF171330),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Big Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0B081A), primaryColor.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (asset.pixelArt != null) {
                        PixelArtCanvas(
                            pixelArt = asset.pixelArt,
                            primaryColor = primaryColor,
                            modifier = Modifier.size(100.dp)
                        )
                    } else if (asset.category == AssetCategory.SOUND_EFFECTS || asset.category == AssetCategory.BACKGROUND_MUSIC) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    if (isAudioPlaying) {
                                        GameSoundSynthesizer.stopMusic()
                                        isAudioPlaying = false
                                    } else {
                                        isAudioPlaying = true
                                        if (asset.sfxType != null) {
                                            val sfx = GameSoundSynthesizer.SoundFx.values().firstOrNull { it.name == asset.sfxType }
                                                ?: GameSoundSynthesizer.SoundFx.COIN
                                            GameSoundSynthesizer.playSfx(sfx)
                                        } else if (asset.bgmType != null) {
                                            val bgm = GameSoundSynthesizer.MusicTheme.values().firstOrNull { it.name == asset.bgmType }
                                                ?: GameSoundSynthesizer.MusicTheme.CYBER_SYNTH
                                            GameSoundSynthesizer.startMusic(bgm)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Test Audio",
                                    tint = Color(0xFF0F0C20)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAudioPlaying) "Stop Audio" else "Play Audio Sample", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            AudioWaveformVisualizer(isPlaying = isAudioPlaying, accentColor = primaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = asset.description,
                    fontSize = 13.sp,
                    color = Color(0xFFD0D0E2)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    asset.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF241F48)
                        ) {
                            Text(
                                text = "#${tag.name}",
                                color = secondaryColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Properties table
                if (asset.properties.isNotEmpty()) {
                    Text(
                        text = "Asset Specifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF201B40))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        asset.properties.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = k, color = Color(0xFF9E9EB8), fontSize = 12.sp)
                                Text(text = v, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isAudioPlaying) GameSoundSynthesizer.stopMusic()
                    onImport()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color(0xFF0F0C20))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Import Into Project", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (isAudioPlaying) GameSoundSynthesizer.stopMusic()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Close", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelArtCreatorDialog(
    onDismiss: () -> Unit,
    onSave: (GameAsset) -> Unit
) {
    var title by remember { mutableStateOf("Custom Sprite") }
    var selectedCategory by remember { mutableStateOf(AssetCategory.CHARACTERS) }
    val palette = listOf(
        "#00000000", "#FF00F0FF", "#FFFF0055", "#FFFFD700",
        "#FF00FF88", "#FF7209B7", "#FFFFFFFF", "#FF1A1A2E",
        "#FFFF5400", "#FF4CC9F0", "#FF3A0CA3", "#FF8338EC"
    )
    var selectedColor by remember { mutableStateOf(palette[1]) }
    val pixelGrid = remember { mutableStateListOf<String>().apply { repeat(64) { add("#00000000") } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16122E),
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(text = "Pixel Art Sprite Creator", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sprite Name", color = Color(0xFF9E9EB8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Color Palette Selector
                Text(text = "Palette Colors", color = Color(0xFF9E9EB8), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(palette) { hex ->
                        val isTransparent = hex == "#00000000"
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isTransparent) Color(0xFF2C274E)
                                    else Color(hex.removePrefix("#").toLong(16) or 0xFF000000)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTransparent) {
                                Text("X", color = Color(0xFFFF3838), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 8x8 Interactive Drawing Grid
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C091A))
                        .border(1.dp, Color(0xFF2E2952), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (y in 0 until 8) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (x in 0 until 8) {
                                    val idx = y * 8 + x
                                    val cellColorHex = pixelGrid[idx]
                                    val cellColor = if (cellColorHex == "#00000000") Color.Transparent
                                    else Color(cellColorHex.removePrefix("#").toLong(16) or 0xFF000000)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .border(0.5.dp, Color(0xFF1F1A3B))
                                            .background(cellColor)
                                            .clickable {
                                                pixelGrid[idx] = selectedColor
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            for (i in 0 until 64) pixelGrid[i] = "#00000000"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Canvas", fontSize = 12.sp, color = Color(0xFFFF5400))
                    }

                    OutlinedButton(
                        onClick = {
                            // Preset smiley / heart
                            val heartPattern = listOf(
                                0,0,0,0,0,0,0,0,
                                0,1,1,0,0,1,1,0,
                                1,1,1,1,1,1,1,1,
                                1,1,1,1,1,1,1,1,
                                0,1,1,1,1,1,1,0,
                                0,0,1,1,1,1,0,0,
                                0,0,0,1,1,0,0,0,
                                0,0,0,0,0,0,0,0
                            )
                            for (i in 0 until 64) {
                                pixelGrid[i] = if (heartPattern[i] == 1) "#FFFF0055" else "#00000000"
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Heart Pattern", fontSize = 12.sp, color = Color(0xFF00F0FF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val asset = GameAsset(
                        id = "custom_asset_${UUID.randomUUID()}",
                        title = title.ifBlank { "Custom Sprite" },
                        category = selectedCategory,
                        tags = listOf(SpriteTag.HERO, SpriteTag.RETRO),
                        description = "User created 8-bit custom sprite asset.",
                        primaryColorHex = 0xFF00F0FF,
                        secondaryColorHex = 0xFFFF0055,
                        pixelArt = SpritePixelMatrix(8, 8, pixelGrid.toList()),
                        isCustomUserCreated = true
                    )
                    onSave(asset)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Save to Vault", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text(text = "Cancel", color = Color.White)
            }
        }
    )
}
