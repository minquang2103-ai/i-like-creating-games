package com.example.scripting

import java.util.UUID

enum class NodeCategory(val displayName: String, val colorHex: Long, val tag: String) {
    EVENT("Event Trigger", 0xFFFF0055, "EVENT"),
    CONDITION("Condition / Variable", 0xFFFFBE0B, "LOGIC"),
    ACTION("Game Action", 0xFF00F0FF, "ACTION")
}

enum class ScriptNodeType(
    val title: String,
    val category: NodeCategory,
    val description: String,
    val defaultParams: Map<String, String>
) {
    // EVENTS
    EVENT_ON_START("On Level Start", NodeCategory.EVENT, "Fires once when the game level begins", emptyMap()),
    EVENT_ON_JUMP("On Player Jump", NodeCategory.EVENT, "Fires whenever player jumps or double-jumps", mapOf("IncludeDoubleJump" to "true")),
    EVENT_ON_COLLECT("On Collect Item", NodeCategory.EVENT, "Fires when collecting a Coin, Key, or Gem", mapOf("ItemType" to "COIN")),
    EVENT_ON_DAMAGE("On Player Damaged", NodeCategory.EVENT, "Fires when player takes hazard or enemy hit", emptyMap()),
    EVENT_ON_ENEMY_DEFEATED("On Enemy Defeated", NodeCategory.EVENT, "Fires when an enemy or slime is slain", mapOf("EnemyType" to "ANY")),
    EVENT_ON_SWITCH("On Switch Activated", NodeCategory.EVENT, "Fires when stepping on switch trigger", emptyMap()),
    EVENT_ON_BOSS_ENRAGE("On Boss HP < 50%", NodeCategory.EVENT, "Fires when boss health drops below half", emptyMap()),
    EVENT_ON_TIMER("On Timer Interval", NodeCategory.EVENT, "Fires periodically every N seconds", mapOf("IntervalSeconds" to "5")),

    // CONDITIONS & VARIABLES
    COND_HEALTH_CHECK("Check Player Health", NodeCategory.CONDITION, "Branches flow based on current HP threshold", mapOf("Operator" to "<", "Threshold" to "2")),
    COND_HAS_ITEM("Check Inventory Key", NodeCategory.CONDITION, "Branches flow if player holds item or key", mapOf("RequiredKey" to "GOLD_KEY")),
    COND_CHANCE_ROLL("Random Chance %", NodeCategory.CONDITION, "Branches flow with probability percentage", mapOf("ChancePercent" to "50")),
    COND_SCORE_CHECK("Check High Score", NodeCategory.CONDITION, "Branches if score exceeds threshold", mapOf("TargetScore" to "1000")),
    VAR_SET_VALUE("Set Custom Variable", NodeCategory.CONDITION, "Stores a numeric game variable", mapOf("VarName" to "ComboCount", "Value" to "1")),
    VAR_ADD_VALUE("Add to Variable", NodeCategory.CONDITION, "Increments or modifies stored game variable", mapOf("VarName" to "ComboCount", "Amount" to "1")),

    // ACTIONS
    ACT_PLAY_SOUND("Play Sound FX", NodeCategory.ACTION, "Plays synthetic audio sound effect", mapOf("SoundType" to "COIN")),
    ACT_SPAWN_PARTICLES("Spawn Particle Burst", NodeCategory.ACTION, "Explodes colored particles at player position", mapOf("ParticleColor" to "CYAN", "Count" to "12")),
    ACT_ADD_SCORE("Award Score Points", NodeCategory.ACTION, "Adds bonus score to the player total", mapOf("Points" to "250")),
    ACT_HEAL_PLAYER("Restore Player HP", NodeCategory.ACTION, "Heals player health points", mapOf("HealAmount" to "1")),
    ACT_SCREEN_SHAKE("Trigger Screen Shake", NodeCategory.ACTION, "Applies tactile screen camera rumble", mapOf("Intensity" to "Medium", "DurationMs" to "250")),
    ACT_SPEED_BOOST("Apply Speed Boost", NodeCategory.ACTION, "Temporarily multiplies movement velocity", mapOf("Multiplier" to "1.5", "DurationSec" to "4")),
    ACT_DAMAGE_ENEMIES("Shockwave Screen Blast", NodeCategory.ACTION, "Damages nearby enemies in radius", mapOf("Damage" to "2")),
    ACT_UNLOCK_DOOR("Open Locked Gate", NodeCategory.ACTION, "Disables laser barrier or unlocks door", mapOf("DoorType" to "GOLD_DOOR")),
    ACT_SHOW_MESSAGE("Display Dialogue Popup", NodeCategory.ACTION, "Shows story toast or NPC speech message", mapOf("Message" to "Secret passage unlocked!")),
    ACT_TRIGGER_VICTORY("Trigger Victory", NodeCategory.ACTION, "Instantly triggers stage completion win", emptyMap())
}

data class NodePort(
    val id: String,
    val name: String,
    val isOutput: Boolean,
    val portColorHex: Long = 0xFFFFFFFF
)

data class ScriptNode(
    val id: String = UUID.randomUUID().toString(),
    val type: ScriptNodeType,
    var posX: Float,
    var posY: Float,
    var params: MutableMap<String, String> = mutableMapOf()
) {
    val inputs: List<NodePort>
        get() = when (type.category) {
            NodeCategory.EVENT -> emptyList()
            NodeCategory.CONDITION -> listOf(NodePort("in_exec", "Execute In", false, 0xFFFFBE0B))
            NodeCategory.ACTION -> listOf(NodePort("in_exec", "Trigger In", false, 0xFF00F0FF))
        }

    val outputs: List<NodePort>
        get() = when (type.category) {
            NodeCategory.EVENT -> listOf(NodePort("out_fired", "On Trigger", true, 0xFFFF0055))
            NodeCategory.CONDITION -> listOf(
                NodePort("out_true", "If True", true, 0xFF00FF88),
                NodePort("out_false", "If False", true, 0xFFFF3838)
            )
            NodeCategory.ACTION -> listOf(NodePort("out_done", "Then", true, 0xFF00F0FF))
        }
}

data class NodeConnection(
    val id: String = UUID.randomUUID().toString(),
    val fromNodeId: String,
    val fromPortId: String,
    val toNodeId: String,
    val toPortId: String
)

data class VisualScriptGraph(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Main Game Logic",
    var nodes: MutableList<ScriptNode> = mutableListOf(),
    var connections: MutableList<NodeConnection> = mutableListOf(),
    var variables: MutableMap<String, Float> = mutableMapOf()
)

object ScriptTemplates {

    fun createCoinJackpotScript(): VisualScriptGraph {
        val n1 = ScriptNode(
            id = "node_event_coin",
            type = ScriptNodeType.EVENT_ON_COLLECT,
            posX = 50f,
            posY = 120f,
            params = mutableMapOf("ItemType" to "COIN")
        )
        val n2 = ScriptNode(
            id = "node_cond_chance",
            type = ScriptNodeType.COND_CHANCE_ROLL,
            posX = 320f,
            posY = 100f,
            params = mutableMapOf("ChancePercent" to "30")
        )
        val n3 = ScriptNode(
            id = "node_act_particles",
            type = ScriptNodeType.ACT_SPAWN_PARTICLES,
            posX = 600f,
            posY = 40f,
            params = mutableMapOf("ParticleColor" to "GOLD", "Count" to "20")
        )
        val n4 = ScriptNode(
            id = "node_act_bonus_score",
            type = ScriptNodeType.ACT_ADD_SCORE,
            posX = 880f,
            posY = 40f,
            params = mutableMapOf("Points" to "500")
        )
        val n5 = ScriptNode(
            id = "node_act_sound",
            type = ScriptNodeType.ACT_PLAY_SOUND,
            posX = 600f,
            posY = 220f,
            params = mutableMapOf("SoundType" to "GEM")
        )

        return VisualScriptGraph(
            id = "template_coin_jackpot",
            title = "Lucky Coin Jackpot (30% Chance Bonus)",
            nodes = mutableListOf(n1, n2, n3, n4, n5),
            connections = mutableListOf(
                NodeConnection(fromNodeId = "node_event_coin", fromPortId = "out_fired", toNodeId = "node_cond_chance", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_cond_chance", fromPortId = "out_true", toNodeId = "node_act_particles", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_act_particles", fromPortId = "out_done", toNodeId = "node_act_bonus_score", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_cond_chance", fromPortId = "out_false", toNodeId = "node_act_sound", toPortId = "in_exec")
            )
        )
    }

    fun createBossEnrageScript(): VisualScriptGraph {
        val n1 = ScriptNode(
            id = "node_boss_enrage",
            type = ScriptNodeType.EVENT_ON_BOSS_ENRAGE,
            posX = 60f,
            posY = 140f
        )
        val n2 = ScriptNode(
            id = "node_shake",
            type = ScriptNodeType.ACT_SCREEN_SHAKE,
            posX = 340f,
            posY = 140f,
            params = mutableMapOf("Intensity" to "Heavy", "DurationMs" to "500")
        )
        val n3 = ScriptNode(
            id = "node_dialogue",
            type = ScriptNodeType.ACT_SHOW_MESSAGE,
            posX = 620f,
            posY = 140f,
            params = mutableMapOf("Message" to "WARNING: Boss has entered Overdrive Mode!")
        )
        val n4 = ScriptNode(
            id = "node_blast",
            type = ScriptNodeType.ACT_DAMAGE_ENEMIES,
            posX = 900f,
            posY = 140f,
            params = mutableMapOf("Damage" to "1")
        )

        return VisualScriptGraph(
            id = "template_boss_enrage",
            title = "Boss Overdrive & Shockwave",
            nodes = mutableListOf(n1, n2, n3, n4),
            connections = mutableListOf(
                NodeConnection(fromNodeId = "node_boss_enrage", fromPortId = "out_fired", toNodeId = "node_shake", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_shake", fromPortId = "out_done", toNodeId = "node_dialogue", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_dialogue", fromPortId = "out_done", toNodeId = "node_blast", toPortId = "in_exec")
            )
        )
    }

    fun createSwitchUnlockScript(): VisualScriptGraph {
        val n1 = ScriptNode(
            id = "node_switch",
            type = ScriptNodeType.EVENT_ON_SWITCH,
            posX = 50f,
            posY = 120f
        )
        val n2 = ScriptNode(
            id = "node_sound",
            type = ScriptNodeType.ACT_PLAY_SOUND,
            posX = 320f,
            posY = 120f,
            params = mutableMapOf("SoundType" to "DOOR_OPEN")
        )
        val n3 = ScriptNode(
            id = "node_unlock",
            type = ScriptNodeType.ACT_UNLOCK_DOOR,
            posX = 600f,
            posY = 120f,
            params = mutableMapOf("DoorType" to "GOLD_DOOR")
        )
        val n4 = ScriptNode(
            id = "node_msg",
            type = ScriptNodeType.ACT_SHOW_MESSAGE,
            posX = 880f,
            posY = 120f,
            params = mutableMapOf("Message" to "Secret vault door opened!")
        )

        return VisualScriptGraph(
            id = "template_switch_unlock",
            title = "Puzzle Switch Door Unlock",
            nodes = mutableListOf(n1, n2, n3, n4),
            connections = mutableListOf(
                NodeConnection(fromNodeId = "node_switch", fromPortId = "out_fired", toNodeId = "node_sound", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_sound", fromPortId = "out_done", toNodeId = "node_unlock", toPortId = "in_exec"),
                NodeConnection(fromNodeId = "node_unlock", fromPortId = "out_done", toNodeId = "node_msg", toPortId = "in_exec")
            )
        )
    }
}
