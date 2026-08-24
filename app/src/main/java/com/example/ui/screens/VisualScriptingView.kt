package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameProject
import com.example.scripting.NodeCategory
import com.example.scripting.NodeConnection
import com.example.scripting.ScriptExecutionEngine
import com.example.scripting.ScriptLogEntry
import com.example.scripting.ScriptNode
import com.example.scripting.ScriptNodeType
import com.example.scripting.ScriptTemplates
import com.example.scripting.VisualScriptGraph
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualScriptingView(
    activeProject: GameProject?,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var currentGraph by remember { mutableStateOf(ScriptTemplates.createCoinJackpotScript()) }
    val nodes = remember { mutableStateListOf<ScriptNode>().apply { addAll(currentGraph.nodes) } }
    val connections = remember { mutableStateListOf<NodeConnection>().apply { addAll(currentGraph.connections) } }

    var selectedPortForConnection by remember { mutableStateOf<Pair<String, String>?>(null) } // NodeId, PortId
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var activeNodePulseId by remember { mutableStateOf<String?>(null) }
    val executionLogs = remember { mutableStateListOf<ScriptLogEntry>() }
    var isSimulating by remember { mutableStateOf(false) }

    val scriptEngine = remember(nodes, connections) {
        val g = VisualScriptGraph(nodes = nodes, connections = connections)
        ScriptExecutionEngine(
            graph = g,
            onLog = { log -> executionLogs.add(0, log) },
            onNodeActive = { activeId -> activeNodePulseId = activeId }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0A1C))
    ) {
        // Infinite Grid Background & Connection Wire Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40f
            // Grid dots
            for (x in 0 until (size.width / gridStep).toInt() + 1) {
                for (y in 0 until (size.height / gridStep).toInt() + 1) {
                    drawCircle(
                        color = Color(0xFF221D42),
                        radius = 1.5f,
                        center = Offset(x * gridStep, y * gridStep)
                    )
                }
            }

            // Draw Connection Wires (Bezier Splines)
            for (conn in connections) {
                val fromNode = nodes.firstOrNull { it.id == conn.fromNodeId }
                val toNode = nodes.firstOrNull { it.id == conn.toNodeId }
                if (fromNode != null && toNode != null) {
                    val fromPos = Offset(fromNode.posX + 240f, fromNode.posY + 55f)
                    val toPos = Offset(toNode.posX, toNode.posY + 55f)

                    val dx = (toPos.x - fromPos.x) / 2
                    val path = Path().apply {
                        moveTo(fromPos.x, fromPos.y)
                        cubicTo(
                            fromPos.x + dx.coerceAtLeast(50f), fromPos.y,
                            toPos.x - dx.coerceAtLeast(50f), toPos.y,
                            toPos.x, toPos.y
                        )
                    }

                    // Shadow / Outer Glow wire
                    drawPath(
                        path = path,
                        color = Color(0xFF00F0FF).copy(alpha = 0.35f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )
                    // Core wire
                    drawPath(
                        path = path,
                        color = Color(0xFF00F0FF),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Render Draggable Interactive Nodes
        nodes.forEach { node ->
            key(node.id) {
                val isPulsing = activeNodePulseId == node.id

                DraggableScriptNode(
                    node = node,
                    isPulsing = isPulsing,
                    onMove = { dx, dy ->
                        node.posX += dx
                        node.posY += dy
                    },
                    onPortClick = { portId, isOutput ->
                        if (isOutput) {
                            selectedPortForConnection = Pair(node.id, portId)
                        } else {
                            selectedPortForConnection?.let { (fromNodeId, fromPortId) ->
                                if (fromNodeId != node.id) {
                                    // Avoid duplicates
                                    connections.removeAll { it.fromNodeId == fromNodeId && it.toNodeId == node.id }
                                    connections.add(
                                        NodeConnection(
                                            fromNodeId = fromNodeId,
                                            fromPortId = fromPortId,
                                            toNodeId = node.id,
                                            toPortId = portId
                                        )
                                    )
                                    selectedPortForConnection = null
                                }
                            }
                        }
                    },
                    onDelete = {
                        connections.removeAll { it.fromNodeId == node.id || it.toNodeId == node.id }
                        nodes.remove(node)
                    }
                )
            }
        }

        // Top Control Ribbon
        Surface(
            color = Color(0xFF14102C).copy(alpha = 0.94f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Visual Logic Engine",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${nodes.size} Nodes • ${connections.size} Logic Connections",
                        color = Color(0xFF9E9EB8),
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showTemplatesDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Templates", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showAddNodeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F0C20), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Node", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Connection Helper Toast when selecting output
        selectedPortForConnection?.let { (fromNodeId, portId) ->
            Surface(
                color = Color(0xFFFF0055),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select a Target Input Pin (Execute In) to connect", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { selectedPortForConnection = null }, modifier = Modifier.size(18.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                }
            }
        }

        // Bottom Simulation & Debug Console Drawer
        Surface(
            color = Color(0xFF13102A).copy(alpha = 0.96f),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2C2750))),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Execution Simulator & Triggers", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { executionLogs.clear() },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF9E9EB8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fast Action Trigger Buttons
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    scriptEngine.triggerEvent(ScriptNodeType.EVENT_ON_COLLECT)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Coin Collected", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    scriptEngine.triggerEvent(ScriptNodeType.EVENT_ON_JUMP)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Player Jumped", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    scriptEngine.triggerEvent(ScriptNodeType.EVENT_ON_DAMAGE)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Player Damaged", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    scriptEngine.triggerEvent(ScriptNodeType.EVENT_ON_SWITCH)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Switch Activated", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    scriptEngine.triggerEvent(ScriptNodeType.EVENT_ON_BOSS_ENRAGE)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Boss Enrage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Log outputs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090715))
                        .padding(8.dp)
                ) {
                    if (executionLogs.isEmpty()) {
                        Text(
                            text = "Tap a trigger above or interact with your game to see live execution flow...",
                            color = Color(0xFF6B6B84),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            items(executionLogs) { log ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (log.isSuccess) "✓ " else "⚠ ",
                                        color = if (log.isSuccess) Color(0xFF00FF88) else Color(0xFFFF5400),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "[${log.nodeTitle}] ",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = log.message,
                                        color = Color(0xFFD4D4E8),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Node Modal Dialog
        if (showAddNodeDialog) {
            AddNodeDialog(
                onDismiss = { showAddNodeDialog = false },
                onAddNode = { nodeType ->
                    val newNode = ScriptNode(
                        type = nodeType,
                        posX = 200f + (0..150).random(),
                        posY = 150f + (0..150).random(),
                        params = nodeType.defaultParams.toMutableMap()
                    )
                    nodes.add(newNode)
                    showAddNodeDialog = false
                }
            )
        }

        // Template selector dialog
        if (showTemplatesDialog) {
            TemplatesDialog(
                onDismiss = { showTemplatesDialog = false },
                onSelectTemplate = { tpl ->
                    nodes.clear()
                    nodes.addAll(tpl.nodes)
                    connections.clear()
                    connections.addAll(tpl.connections)
                    showTemplatesDialog = false
                    executionLogs.add(0, ScriptLogEntry(nodeTitle = "Engine", message = "Loaded template '${tpl.title}'"))
                }
            )
        }
    }
}

@Composable
fun DraggableScriptNode(
    node: ScriptNode,
    isPulsing: Boolean,
    onMove: (Float, Float) -> Unit,
    onPortClick: (portId: String, isOutput: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val headerColor = Color(node.type.category.colorHex)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191535)),
        border = BorderStroke(
            width = if (isPulsing) 2.5.dp else 1.dp,
            color = if (isPulsing) Color(0xFF00F0FF) else Color(0xFF332D5A)
        ),
        modifier = Modifier
            .offset { IntOffset(node.posX.roundToInt(), node.posY.roundToInt()) }
            .width(240.dp)
    ) {
        Column {
            // Header Bar (Draggable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onMove(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (node.type.category) {
                            NodeCategory.EVENT -> Icons.Default.FlashOn
                            NodeCategory.CONDITION -> Icons.Default.LinearScale
                            NodeCategory.ACTION -> Icons.Default.Extension
                        },
                        contentDescription = null,
                        tint = Color(0xFF0F0C20),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = node.type.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F0C20),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF0F0C20), modifier = Modifier.size(14.dp))
                }
            }

            // Description
            Text(
                text = node.type.description,
                fontSize = 10.sp,
                color = Color(0xFFA0A0BA),
                lineHeight = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )

            // Dynamic Parameters Form
            if (node.params.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    node.params.forEach { (k, v) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = k, fontSize = 10.sp, color = Color(0xFF8E8EA8))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF26204D)
                            ) {
                                Text(
                                    text = v,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Ports row (Inputs on Left, Outputs on Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF130F2B))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Inputs
                Column {
                    node.inputs.forEach { port ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onPortClick(port.id, false) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(port.portColorHex))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = port.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Outputs
                Column(horizontalAlignment = Alignment.End) {
                    node.outputs.forEach { port ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onPortClick(port.id, true) }
                                .padding(4.dp)
                        ) {
                            Text(text = port.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(port.portColorHex))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNodeDialog(
    onDismiss: () -> Unit,
    onAddNode: (ScriptNodeType) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(NodeCategory.EVENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF171333),
        shape = RoundedCornerShape(18.dp),
        title = {
            Text("Add Visual Logic Node", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Category Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NodeCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorHex),
                                selectedLabelColor = Color(0xFF0F0C20),
                                containerColor = Color(0xFF241F48),
                                labelColor = Color(0xFFB0B0C4)
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val matching = ScriptNodeType.values().filter { it.category == selectedCategory }
                    items(matching) { nodeType ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddNode(nodeType) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF211C44))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = nodeType.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = nodeType.description,
                                    color = Color(0xFFA0A0BA),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun TemplatesDialog(
    onDismiss: () -> Unit,
    onSelectTemplate: (VisualScriptGraph) -> Unit
) {
    val templates = listOf(
        ScriptTemplates.createCoinJackpotScript(),
        ScriptTemplates.createBossEnrageScript(),
        ScriptTemplates.createSwitchUnlockScript()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF171333),
        shape = RoundedCornerShape(18.dp),
        title = {
            Text("Logic Graph Templates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pick a pre-configured node network to accelerate game mechanics:",
                    color = Color(0xFFA0A0BA),
                    fontSize = 12.sp
                )

                templates.forEach { tpl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(tpl) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF231D4A)),
                        border = BorderStroke(1.dp, Color(0xFF3B3370))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = tpl.title,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${tpl.nodes.size} Nodes connected with triggers and actions",
                                color = Color(0xFFD4D4E8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Close", color = Color.White)
            }
        }
    )
}
