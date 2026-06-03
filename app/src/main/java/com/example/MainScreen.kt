package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class ScreenDestination(val route: String, val title: String, val icon: ImageVector, val contentDesc: String) {
    object Quiz : ScreenDestination(
        route = "quiz",
        title = "Sınav",
        icon = Icons.Default.Quiz,
        contentDesc = "Sınav sekmesi, 3 sekmeden 1.si. Geçmek için çift dokunun"
    )
    object Flashcard : ScreenDestination(
        route = "flashcard",
        title = "Tekrar",
        icon = Icons.Default.MenuBook,
        contentDesc = "Tekrar sekmesi, 3 sekmeden 2.si. Geçmek için çift dokunun"
    )
    object Assistant : ScreenDestination(
        route = "assistant",
        title = "Asistan",
        icon = Icons.Default.Chat,
        contentDesc = "Asistan sekmesi, 3 sekmeden 3.sü. Geçmek için çift dokunun"
    )
}

val bottomNavItems = listOf(
    ScreenDestination.Quiz,
    ScreenDestination.Flashcard,
    ScreenDestination.Assistant
)

@Composable
fun MainScreen(
    viewModel: QuestionViewModel,
    voiceSynthesizer: VoiceSynthesizer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = screen.contentDesc
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenDestination.Quiz.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenDestination.Quiz.route) {
                QuestionScreen(viewModel = viewModel, voiceSynthesizer = voiceSynthesizer)
            }
            composable(ScreenDestination.Flashcard.route) {
                FlashcardScreen(viewModel = viewModel)
            }
            composable(ScreenDestination.Assistant.route) {
                AssistantScreen()
            }
        }
    }
}

@Composable
fun AssistantScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(
            text = "Asistan",
            modifier = Modifier
                .padding(16.dp)
                .semantics { heading() }
        )
    }
}
