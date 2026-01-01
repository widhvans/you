package com.cinemax.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cinemax.app.ui.components.BottomNavBar
import com.cinemax.app.ui.components.BottomNavDestination
import com.cinemax.app.ui.screens.detail.DetailScreen
import com.cinemax.app.ui.screens.downloads.DownloadsScreen
import com.cinemax.app.ui.screens.home.HomeScreen
import com.cinemax.app.ui.screens.player.PlayerActivity
import com.cinemax.app.ui.screens.search.SearchScreen
import com.cinemax.app.ui.theme.BackgroundPrimary

/**
 * Navigation routes
 */
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DOWNLOADS = "downloads"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{movieId}"
    const val PLAYER = "player/{movieId}?url={url}"
    const val CATEGORY = "category/{slug}?title={title}"
    
    fun detail(movieId: String) = "detail/$movieId"
    fun player(movieId: String, url: String = "") = "player/$movieId?url=$url"
    fun category(slug: String, title: String) = "category/$slug?title=$title"
}

/**
 * Main Navigation Graph
 */
@Composable
fun CineMaxNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Determine if we should show bottom nav
    val showBottomNav = currentRoute in listOf(
        Routes.HOME,
        Routes.SEARCH,
        Routes.DOWNLOADS,
        Routes.PROFILE
    )
    
    // Map route to destination
    val currentDestination = when (currentRoute) {
        Routes.HOME -> BottomNavDestination.HOME
        Routes.SEARCH -> BottomNavDestination.SEARCH
        Routes.DOWNLOADS -> BottomNavDestination.DOWNLOADS
        Routes.PROFILE -> BottomNavDestination.PROFILE
        else -> BottomNavDestination.HOME
    }
    
    Scaffold(
        containerColor = BackgroundPrimary,
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            // Pop up to start destination
                            popUpTo(Routes.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(
                bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.dp(0)
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            // Home Screen
            composable(Routes.HOME) {
                HomeScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.detail(movieId))
                    },
                    onSearchClick = {
                        navController.navigate(Routes.SEARCH)
                    },
                    onCategoryClick = { slug, title ->
                        navController.navigate(Routes.category(slug, title))
                    }
                )
            }
            
            // Search Screen
            composable(Routes.SEARCH) {
                SearchScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.detail(movieId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Downloads Screen
            composable(Routes.DOWNLOADS) {
                DownloadsScreen()
            }
            
            // Profile Screen
            composable(Routes.PROFILE) {
                // TODO: Implement ProfileScreen
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material3.Text(
                        text = "Profile",
                        modifier = Modifier.padding(androidx.compose.ui.unit.dp(16))
                    )
                }
            }
            
            // Detail Screen
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType }
                )
            ) {
                DetailScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onPlayClick = { movieId, streamUrl ->
                        // Launch player activity
                        context.startActivity(
                            PlayerActivity.createIntent(
                                context = context,
                                movieId = movieId,
                                streamUrl = streamUrl,
                                title = "Movie"
                            )
                        )
                    }
                )
            }
            
            // Category Screen
            composable(
                route = Routes.CATEGORY,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType; defaultValue = "Category" }
                )
            ) {
                // TODO: Implement CategoryScreen
                val slug = it.arguments?.getString("slug") ?: ""
                val title = it.arguments?.getString("title") ?: "Category"
                
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material3.Text(
                        text = "$title ($slug)",
                        modifier = Modifier.padding(androidx.compose.ui.unit.dp(16))
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.unit.dp(value: Int) = androidx.compose.ui.unit.Dp(value.toFloat())
