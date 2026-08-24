package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.community.CommunityManager
import com.example.model.EntityType
import com.example.model.GameEntityConfig
import com.example.model.GameGenre
import com.example.model.GameLevel
import com.example.model.GameProject
import com.example.model.TileType
import java.util.Locale
import java.util.UUID

enum class EditorBrushMode {
    TILES,
    ENTITIES
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LevelEditorView(
    activeProject: GameProject,
    onPlaytest: (GameProject) -> Unit,
    onOpenScripting: (GameProject) -> Unit,
    onOpenAssets: () -> Unit,
    onPublish: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectTitle by remember { mutableStateOf(activeProject.title) }
    var currentLevelIdx by remember { mutableIntStateOf(0) }
    val levels = remember { mutableStateListOf<GameLevel>().apply { addAll(activeProject.levels) } }

    val activeLevel = levels.getOrElse(currentLevelIdx) { levels.first() }
    val grid = remember(activeLevel) {
        mutableStateListOf<MutableList<TileType>>().apply {
            if (activeLevel.gridData.isNotEmpty()) {
                activeLevel.gridData.forEach { rowStr ->
                    val row = rowStr.map { ch ->
                        TileType.values().firstOrNull { it.symbol == ch } ?: TileType.EMPTY
                    }.toMutableList()
                    add(row)
                }
            } else {
                repeat(activeLevel.height) { y ->
                    val row = MutableList(activeLevel.width) { x ->
                        if (y == 0 || y == activeLevel.height - 1 || x == 0 || x == activeLevel.width - 1) TileType.SOLID else TileType.EMPTY
                    }
                    add(row)
                }
            }
        }
    }

    val entities = remember(activeLevel) {
        mutableStateListOf<GameEntityConfig>().apply {
            addAll(activeLevel.entities)
        }
    }

    var brushMode by remember { mutableStateOf(EditorBrushMode.TILES) }
    var selectedTileBrush by remember { mutableStateOf(TileType.SOLID) }
    var selectedEntityBrush by remember { mutableStateOf(EntityType.ENEMY_PATROL) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }

    fun syncLevelData() {
        if (currentLevelIdx in levels.indices) {
            val serializedGrid = grid.map { row ->
                row.map { it.symbol }.joinToString("")
            }
            levels[currentLevelIdx] = levels[currentLevelIdx].copy(
                gridData = serializedGrid,
                entities = entities.toMutableList()
            )
            activeProject.title = projectTitle
            activeProject.levels = levels.toMutableList()
            CommunityManager.saveLocalProject(activeProject)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C22))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = projectTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF261F4D)
                        ) {
                            Text(
                                text = activeProject.genre.displayName,
                                color = Color(0xFF00F0FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Level ${currentLevelIdx + 1} of ${levels.size} • Paint tiles or drop spawners",
                        fontSize = 11.sp,
                        color = Color(0xFFA0A0BA)
                    )
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Physics Settings", tint = Color.White)
                    }

                    Button(
                        onClick = {
                            syncLevelData()
                            onPlaytest(activeProject)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0F0C20), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            syncLevelData()
                            showPublishDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Publish, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publish", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Level Tabs Row (+ Add Level)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(levels.size) { idx ->
                    FilterChip(
                        selected = currentLevelIdx == idx,
                        onClick = {
                            syncLevelData()
                            currentLevelIdx = idx
                        },
                        label = { Text("Level ${idx + 1}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF),
                            selectedLabelColor = Color(0xFF0F0C20),
                            containerColor = Color(0xFF1F1A3B),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF261F4D),
                        modifier = Modifier.clickable {
                            val newLvl = GameLevel(
                                id = "level_${levels.size + 1}",
                                title = "Level ${levels.size + 1}",
                                width = 24,
                                height = 14,
                                gridData = List(14) { y ->
                                    if (y == 0 || y == 13) "########################" else "#......................#"
                                }
                            )
                            levels.add(newLvl)
                            currentLevelIdx = levels.size - 1
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Level", tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New Level", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Grid Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0718))
                    .border(1.dp, Color(0xFF27214B), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(brushMode, selectedTileBrush, selectedEntityBrush, grid) {
                            detectTapGestures { offset ->
                                val cellW = size.width / grid[0].size
                                val cellH = size.height / grid.size

                                val x = (offset.x / cellW).toInt().coerceIn(0, grid[0].size - 1)
                                val y = (offset.y / cellH).toInt().coerceIn(0, grid.size - 1)

                                if (brushMode == EditorBrushMode.TILES) {
                                    grid[y][x] = selectedTileBrush
                                    syncLevelData()
                                } else {
                                    entities.removeAll { it.gridX == x && it.gridY == y }
                                    entities.add(
                                        GameEntityConfig(
                                            id = "entity_${UUID.randomUUID()}",
                                            type = selectedEntityBrush,
                                            gridX = x,
                                            gridY = y
                                        )
                                    )
                                    syncLevelData()
                                }
                            }
                        }
                        .pointerInput(brushMode, selectedTileBrush, grid) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                if (brushMode == EditorBrushMode.TILES) {
                                    val cellW = size.width / grid[0].size
                                    val cellH = size.height / grid.size

                                    val x = (change.position.x / cellW).toInt().coerceIn(0, grid[0].size - 1)
                                    val y = (change.position.y / cellH).toInt().coerceIn(0, grid.size - 1)

                                    if (grid[y][x] != selectedTileBrush) {
                                        grid[y][x] = selectedTileBrush
                                        syncLevelData()
                                    }
                                }
                            }
                        }
                ) {
                    val rows = grid.size
                    val cols = grid[0].size
                    val cellW = size.width / cols
                    val cellH = size.height / rows

                    // Grid lines
                    for (y in 0..rows) {
                        drawLine(
                            color = Color(0xFF1B163B),
                            start = Offset(0f, y * cellH),
                            end = Offset(size.width, y * cellH),
                            strokeWidth = 1f
                        )
                    }
                    for (x in 0..cols) {
                        drawLine(
                            color = Color(0xFF1B163B),
                            start = Offset(x * cellW, 0f),
                            end = Offset(x * cellW, size.height),
                            strokeWidth = 1f
                        )
                    }

                    // Render Painted Tiles
                    for (y in 0 until rows) {
                        for (x in 0 until cols) {
                            val tile = grid[y][x]
                            val tx = x * cellW
                            val ty = y * cellH

                            when (tile) {
                                TileType.SOLID -> {
                                    drawRoundRect(
                                        color = Color(0xFF3A3066),
                                        topLeft = Offset(tx + 1, ty + 1),
                                        size = Size(cellW - 2, cellH - 2),
                                        cornerRadius = CornerRadius(2f, 2f)
                                    )
                                    drawRect(
                                        color = Color(0xFF00F0FF),
                                        topLeft = Offset(tx + 1, ty + 1),
                                        size = Size(cellW - 2, 2f)
                                    )
                                }
                                TileType.PLATFORM -> {
                                    drawRoundRect(
                                        color = Color(0xFF00F0FF),
                                        topLeft = Offset(tx + 1, ty + 1),
                                        size = Size(cellW - 2, cellH * 0.35f),
                                        cornerRadius = CornerRadius(2f, 2f)
                                    )
                                }
                                TileType.COIN -> {
                                    drawCircle(
                                        color = Color(0xFFFFD700),
                                        radius = cellW * 0.35f,
                                        center = Offset(tx + cellW * 0.5f, ty + cellH * 0.5f)
                                    )
                                }
                                TileType.GEM -> {
                                    drawCircle(
                                        color = Color(0xFF00F0FF),
                                        radius = cellW * 0.35f,
                                        center = Offset(tx + cellW * 0.5f, ty + cellH * 0.5f)
                                    )
                                }
                                TileType.SPIKES -> {
                                    drawRoundRect(
                                        color = Color(0xFFFF0055),
                                        topLeft = Offset(tx + 2, ty + cellH * 0.6f),
                                        size = Size(cellW - 4, cellH * 0.4f),
                                        cornerRadius = CornerRadius(2f, 2f)
                                    )
                                }
                                TileType.LAVA -> {
                                    drawRect(
                                        color = Color(0xFFFF5400),
                                        topLeft = Offset(tx + 1, ty + cellH * 0.4f),
                                        size = Size(cellW - 2, cellH * 0.6f)
                                    )
                                }
                                TileType.KEY_GOLD -> {
                                    drawCircle(
                                        color = Color(0xFFFFD700),
                                        radius = cellW * 0.3f,
                                        center = Offset(tx + cellW * 0.5f, ty + cellH * 0.5f)
                                    )
                                }
                                TileType.DOOR_GOLD -> {
                                    drawRoundRect(
                                        color = Color(0xFFFFD700),
                                        topLeft = Offset(tx + 1, ty + 1),
                                        size = Size(cellW - 2, cellH - 2),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                                TileType.CHEST -> {
                                    drawRoundRect(
                                        color = Color(0xFF8B5A2B),
                                        topLeft = Offset(tx + 2, ty + 2),
                                        size = Size(cellW - 4, cellH - 4),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                                TileType.GOAL_FLAG -> {
                                    drawCircle(
                                        color = Color(0xFF7209B7),
                                        radius = cellW * 0.45f,
                                        center = Offset(tx + cellW * 0.5f, ty + cellH * 0.5f)
                                    )
                                }
                                TileType.SWITCH_TOGGLE -> {
                                    drawCircle(
                                        color = Color(0xFF00FF88),
                                        radius = cellW * 0.3f,
                                        center = Offset(tx + cellW * 0.5f, ty + cellH * 0.5f)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }

                    // Render Entity Spawners
                    for (ent in entities) {
                        val ex = ent.gridX * cellW
                        val ey = ent.gridY * cellH
                        when (ent.type) {
                            EntityType.PLAYER_SPAWN -> {
                                drawRoundRect(
                                    color = Color(0xFF00FF88),
                                    topLeft = Offset(ex + 2, ey + 2),
                                    size = Size(cellW - 4, cellH - 4),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = cellW * 0.2f,
                                    center = Offset(ex + cellW * 0.5f, ey + cellH * 0.5f)
                                )
                            }
                            EntityType.ENEMY_BOSS -> {
                                drawRoundRect(
                                    color = Color(0xFFFF0055),
                                    topLeft = Offset(ex + 1, ey + 1),
                                    size = Size(cellW * 1.5f, cellH * 1.5f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                            EntityType.NPC_GUIDE -> {
                                drawRoundRect(
                                    color = Color(0xFF9B5DE5),
                                    topLeft = Offset(ex + 2, ey + 2),
                                    size = Size(cellW - 4, cellH - 4),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                            else -> {
                                drawCircle(
                                    color = Color(0xFFFF3366),
                                    radius = cellW * 0.4f,
                                    center = Offset(ex + cellW * 0.5f, ey + cellH * 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brush Mode Toggle (Tiles vs Entities) & Palette
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = brushMode == EditorBrushMode.TILES,
                        onClick = { brushMode = EditorBrushMode.TILES },
                        label = { Text("🧱 Tiles", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00F0FF),
                            selectedLabelColor = Color(0xFF0F0C20),
                            containerColor = Color(0xFF1E193C),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = brushMode == EditorBrushMode.ENTITIES,
                        onClick = { brushMode = EditorBrushMode.ENTITIES },
                        label = { Text("👾 Spawners", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF0055),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E193C),
                            labelColor = Color(0xFFB0B0C4)
                        ),
                        border = null
                    )
                }

                // Clear / Eraser tool
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF261F4D),
                    modifier = Modifier.clickable {
                        selectedTileBrush = TileType.EMPTY
                        brushMode = EditorBrushMode.TILES
                    }
                ) {
                    Text("Eraser / Empty", color = Color(0xFFFFBE0B), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Palette Swatches Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (brushMode == EditorBrushMode.TILES) {
                    val tileBrushes = listOf(
                        TileType.SOLID, TileType.PLATFORM, TileType.COIN, TileType.GEM,
                        TileType.SPIKES, TileType.LAVA, TileType.KEY_GOLD, TileType.DOOR_GOLD,
                        TileType.CHEST, TileType.SWITCH_TOGGLE, TileType.GOAL_FLAG
                    )
                    items(tileBrushes) { t ->
                        val isSelected = selectedTileBrush == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF1F1A3B),
                            modifier = Modifier.clickable { selectedTileBrush = t }
                        ) {
                            Text(
                                text = t.name.replace("_", " ").lowercase(),
                                color = if (isSelected) Color(0xFF0F0C20) else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    val entityBrushes = listOf(
                        EntityType.PLAYER_SPAWN, EntityType.ENEMY_PATROL,
                        EntityType.ENEMY_SHOOTER, EntityType.ENEMY_BOUNCER,
                        EntityType.ENEMY_BOSS, EntityType.NPC_GUIDE
                    )
                    items(entityBrushes) { e ->
                        val isSelected = selectedEntityBrush == e
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFFF0055) else Color(0xFF1F1A3B),
                            modifier = Modifier.clickable { selectedEntityBrush = e }
                        ) {
                            Text(
                                text = e.name.replace("ENEMY_", "").replace("NPC_", "").replace("_", " ").lowercase(),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Publish to Community Dialog
        if (showPublishDialog) {
            var tagsText by remember { mutableStateOf("Platformer, Action, Retro") }
            var difficultyText by remember { mutableStateOf("Medium") }

            AlertDialog(
                onDismissRequest = { showPublishDialog = false },
                containerColor = Color(0xFF171333),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text("Publish Game to Community", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = projectTitle,
                            onValueChange = { projectTitle = it },
                            label = { Text("Game Title", color = Color(0xFF9E9EB8)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            label = { Text("Tags (comma separated)", color = Color(0xFF9E9EB8)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Difficulty: $difficultyText", color = Color.White, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Casual", "Normal", "Challenging", "Hardcore").forEach { diff ->
                                FilterChip(
                                    selected = difficultyText == diff,
                                    onClick = { difficultyText = diff },
                                    label = { Text(diff, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF0055),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF261F4D),
                                        labelColor = Color(0xFFB0B0C4)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            activeProject.title = projectTitle
                            activeProject.tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            activeProject.difficulty = difficultyText
                            activeProject.isPublished = true
                            CommunityManager.publishGame(activeProject)
                            showPublishDialog = false
                            onPublish(activeProject)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Publish Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showPublishDialog = false }, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        // Game Settings Bottom Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = Color(0xFF16122E)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Game Physics & Mechanics Tuning", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

                    // Speed Slider
                    Column {
                        Text("Player Move Speed: ${String.format(Locale.US, "%.1f", activeProject.config.moveSpeed)}", color = Color(0xFF00F0FF), fontSize = 12.sp)
                        Slider(
                            value = activeProject.config.moveSpeed,
                            onValueChange = { activeProject.config.moveSpeed = it },
                            valueRange = 2f..12f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00F0FF), activeTrackColor = Color(0xFF00F0FF))
                        )
                    }

                    // Jump Force
                    Column {
                        Text("Jump Velocity: ${String.format(Locale.US, "%.1f", activeProject.config.jumpForce)}", color = Color(0xFFFF0055), fontSize = 12.sp)
                        Slider(
                            value = activeProject.config.jumpForce,
                            onValueChange = { activeProject.config.jumpForce = it },
                            valueRange = 8f..22f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF0055), activeTrackColor = Color(0xFFFF0055))
                        )
                    }

                    // Attack toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Melee / Blaster Attack", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = activeProject.config.hasAttack,
                            onCheckedChange = { activeProject.config.hasAttack = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                        )
                    }

                    Button(
                        onClick = { showSettingsSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Apply Settings", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
