package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asset.GameAsset
import com.example.community.CommunityManager
import com.example.model.GameProject
import com.example.model.PresetGames
import com.example.ui.screens.AssetLibraryView
import com.example.ui.screens.CommunityView
import com.example.ui.screens.GamePlayView
import com.example.ui.screens.LevelEditorView
import com.example.ui.screens.VisualScriptingView

enum class AppNavigationTab(val title: String, val icon: ImageVector) {
    ARCADE("Arcade", Icons.Default.SportsEsports),
    STUDIO("Studio", Icons.Default.Handyman),
    LOGIC("Logic Graph", Icons.Default.Extension),
    ASSETS("Asset Vault", Icons.Default.Inventory2)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameCraftStudioApp()
        }
    }
}

@Composable
fun GameCraftStudioApp() {
    var currentTab by remember { mutableStateOf(AppNavigationTab.ARCADE) }
    val myProjects by CommunityManager.myProjects.collectAsState()
    var activeProject by remember { mutableStateOf(myProjects.firstOrNull() ?: PresetGames.createDefaultPlatformer()) }
    var activePlayingProject by remember { mutableStateOf<GameProject?>(null) }

    // If in full screen gameplay
    if (activePlayingProject != null) {
        GamePlayView(
            project = activePlayingProject!!,
            onExitPlay = { activePlayingProject = null },
            onOpenEditor = {
                activeProject = activePlayingProject!!
                activePlayingProject = null
                currentTab = AppNavigationTab.STUDIO
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF120E29),
                contentColor = Color.White
            ) {
                AppNavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F0C20),
                            selectedTextColor = Color(0xFF00F0FF),
                            indicatorColor = Color(0xFF00F0FF),
                            unselectedIconColor = Color(0xFF8E8EA8),
                            unselectedTextColor = Color(0xFF8E8EA8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0C091C))
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    AppNavigationTab.ARCADE -> {
                        CommunityView(
                            onPlayGame = { game ->
                                activePlayingProject = game
                            },
                            onRemixGame = { forked ->
                                activeProject = forked
                                currentTab = AppNavigationTab.STUDIO
                            },
                            onPublishProject = {
                                currentTab = AppNavigationTab.STUDIO
                            }
                        )
                    }

                    AppNavigationTab.STUDIO -> {
                        LevelEditorView(
                            activeProject = activeProject,
                            onPlaytest = { project ->
                                activePlayingProject = project
                            },
                            onOpenScripting = { project ->
                                activeProject = project
                                currentTab = AppNavigationTab.LOGIC
                            },
                            onOpenAssets = {
                                currentTab = AppNavigationTab.ASSETS
                            },
                            onPublish = { published ->
                                activeProject = published
                                currentTab = AppNavigationTab.ARCADE
                            }
                        )
                    }

                    AppNavigationTab.LOGIC -> {
                        VisualScriptingView(
                            activeProject = activeProject
                        )
                    }

                    AppNavigationTab.ASSETS -> {
                        AssetLibraryView(
                            activeProject = activeProject,
                            onImportAssetToProject = { asset ->
                                // Tag and save project
                                val updatedTags = (activeProject.tags + asset.title).distinct()
                                activeProject.tags = updatedTags
                                CommunityManager.saveLocalProject(activeProject)
                            }
                        )
                    }
                }
            }
        }
    }
}
