package com.example.scripting

import com.example.audio.GameSoundSynthesizer
import kotlinx.coroutines.delay

data class ScriptLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val nodeTitle: String,
    val message: String,
    val isSuccess: Boolean = true
)

class ScriptExecutionEngine(
    val graph: VisualScriptGraph,
    private val onLog: (ScriptLogEntry) -> Unit,
    private val onNodeActive: (String) -> Unit
) {
    var playerHp: Int = 3
    var score: Int = 1000
    var hasGoldKey: Boolean = true
    var isDoorUnlocked: Boolean = false
    var isScreenShaking: Boolean = false

    suspend fun triggerEvent(eventType: ScriptNodeType, payload: Map<String, Any> = emptyMap()) {
        val matchingNodes = graph.nodes.filter { it.type == eventType }
        if (matchingNodes.isEmpty()) {
            onLog(ScriptLogEntry(nodeTitle = "Event Dispatcher", message = "No listener found for event ${eventType.title}", isSuccess = false))
            return
        }

        for (eventNode in matchingNodes) {
            executeNode(eventNode.id, "out_fired")
        }
    }

    private suspend fun executeNode(nodeId: String, outputPortId: String) {
        val node = graph.nodes.firstOrNull { it.id == nodeId } ?: return
        onNodeActive(node.id)
        delay(120)

        // Find connected target nodes
        val outgoingConns = graph.connections.filter { it.fromNodeId == nodeId && it.fromPortId == outputPortId }

        for (conn in outgoingConns) {
            val targetNode = graph.nodes.firstOrNull { it.id == conn.toNodeId } ?: continue
            onNodeActive(targetNode.id)
            delay(150)

            when (targetNode.type) {
                // CONDITIONS
                ScriptNodeType.COND_HEALTH_CHECK -> {
                    val threshold = targetNode.params["Threshold"]?.toIntOrNull() ?: 2
                    val op = targetNode.params["Operator"] ?: "<"
                    val conditionMet = if (op == "<") playerHp < threshold else playerHp >= threshold
                    val outPort = if (conditionMet) "out_true" else "out_false"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Health ($playerHp) $op $threshold => $conditionMet", isSuccess = conditionMet))
                    executeNode(targetNode.id, outPort)
                }
                ScriptNodeType.COND_HAS_ITEM -> {
                    val req = targetNode.params["RequiredKey"] ?: "GOLD_KEY"
                    val hasItem = if (req.contains("GOLD", true)) hasGoldKey else true
                    val outPort = if (hasItem) "out_true" else "out_false"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Checked inventory for $req => $hasItem", isSuccess = hasItem))
                    executeNode(targetNode.id, outPort)
                }
                ScriptNodeType.COND_CHANCE_ROLL -> {
                    val chance = targetNode.params["ChancePercent"]?.toIntOrNull() ?: 50
                    val roll = (1..100).random()
                    val success = roll <= chance
                    val outPort = if (success) "out_true" else "out_false"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Rolled $roll against $chance% => ${if (success) "SUCCESS" else "FAIL"}", isSuccess = success))
                    executeNode(targetNode.id, outPort)
                }
                ScriptNodeType.COND_SCORE_CHECK -> {
                    val target = targetNode.params["TargetScore"]?.toIntOrNull() ?: 1000
                    val passed = score >= target
                    val outPort = if (passed) "out_true" else "out_false"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Score ($score) >= $target => $passed", isSuccess = passed))
                    executeNode(targetNode.id, outPort)
                }
                ScriptNodeType.VAR_SET_VALUE -> {
                    val varName = targetNode.params["VarName"] ?: "Var1"
                    val value = targetNode.params["Value"]?.toFloatOrNull() ?: 0f
                    graph.variables[varName] = value
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Set variable '$varName' = $value"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.VAR_ADD_VALUE -> {
                    val varName = targetNode.params["VarName"] ?: "Var1"
                    val amt = targetNode.params["Amount"]?.toFloatOrNull() ?: 1f
                    val current = graph.variables[varName] ?: 0f
                    graph.variables[varName] = current + amt
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Modified '$varName' (${current} + $amt = ${current + amt})"))
                    executeNode(targetNode.id, "out_done")
                }

                // ACTIONS
                ScriptNodeType.ACT_PLAY_SOUND -> {
                    val sfxName = targetNode.params["SoundType"] ?: "COIN"
                    val sfx = GameSoundSynthesizer.SoundFx.values().firstOrNull { it.name.equals(sfxName, true) }
                        ?: GameSoundSynthesizer.SoundFx.COIN
                    GameSoundSynthesizer.playSfx(sfx)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Played sound synthesizer '${sfx.title}'"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_SPAWN_PARTICLES -> {
                    val color = targetNode.params["ParticleColor"] ?: "CYAN"
                    val count = targetNode.params["Count"] ?: "12"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Exploded $count $color particle burst in game world"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_ADD_SCORE -> {
                    val pts = targetNode.params["Points"]?.toIntOrNull() ?: 250
                    score += pts
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Awarded +$pts Score Points (Total: $score)"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_HEAL_PLAYER -> {
                    val heal = targetNode.params["HealAmount"]?.toIntOrNull() ?: 1
                    playerHp += heal
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.POWERUP)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Restored +$heal Player Health (HP: $playerHp)"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_SCREEN_SHAKE -> {
                    isScreenShaking = true
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.EXPLOSION)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Screen Camera Shaking (${targetNode.params["Intensity"] ?: "Medium"})"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_SPEED_BOOST -> {
                    val mult = targetNode.params["Multiplier"] ?: "1.5"
                    val dur = targetNode.params["DurationSec"] ?: "4"
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.DOUBLE_JUMP)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Applied ${mult}x Speed Velocity for ${dur}s"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_DAMAGE_ENEMIES -> {
                    val dmg = targetNode.params["Damage"] ?: "2"
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.ENEMY_HIT)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Shockwave dealt $dmg damage to all screen enemies"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_UNLOCK_DOOR -> {
                    isDoorUnlocked = true
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.DOOR_OPEN)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Unlocked security door gate '${targetNode.params["DoorType"] ?: "GOLD_DOOR"}'"))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_SHOW_MESSAGE -> {
                    val msg = targetNode.params["Message"] ?: "Story trigger"
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "Displayed dialogue toast: \"$msg\""))
                    executeNode(targetNode.id, "out_done")
                }
                ScriptNodeType.ACT_TRIGGER_VICTORY -> {
                    GameSoundSynthesizer.playSfx(GameSoundSynthesizer.SoundFx.VICTORY)
                    onLog(ScriptLogEntry(nodeTitle = targetNode.type.title, message = "LEVEL COMPLETED - Triggered Victory State!"))
                    executeNode(targetNode.id, "out_done")
                }
                else -> {}
            }
        }
    }
}
