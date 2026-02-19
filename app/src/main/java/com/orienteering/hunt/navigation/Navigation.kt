package com.orienteering.hunt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.orienteering.hunt.data.repository.HuntRepository
import com.orienteering.hunt.services.LocationService
import com.orienteering.hunt.ui.screens.ActiveHuntScreen
import com.orienteering.hunt.ui.screens.CheckInScreen
import com.orienteering.hunt.ui.screens.HomeScreen
import com.orienteering.hunt.ui.screens.HuntMapScreen
import com.orienteering.hunt.ui.screens.HuntSelectionScreen
import com.orienteering.hunt.ui.screens.HuntStartScreen
import com.orienteering.hunt.ui.screens.LeaderboardScreen
import com.orienteering.hunt.ui.screens.OnboardingScreen
import com.orienteering.hunt.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object HuntSelection : Screen("hunt_selection")
    object HuntStart : Screen("hunt_start/{huntId}") {
        fun createRoute(huntId: String) = "hunt_start/$huntId"
    }
    object ActiveHunt : Screen("active_hunt")
    object HuntMap : Screen("hunt_map")
    object CheckIn : Screen("check_in")
    object Leaderboard : Screen("leaderboard")
}

@Composable
fun HuntNavGraph(
    navController: NavHostController,
    repository: HuntRepository,
    locationService: LocationService
) {
    val viewModel: GameViewModel = viewModel(
        factory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    return GameViewModel(repository, locationService) as T
                }
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()
    
    val startDestination = if (uiState.isOnboarding || uiState.currentPlayer == null) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = viewModel,
                onProfileCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToHunts = {
                    navController.navigate(Screen.HuntSelection.route)
                },
                onNavigateToActiveHunt = {
                    navController.navigate(Screen.ActiveHunt.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                }
            )
        }
        
        composable(Screen.HuntSelection.route) {
            HuntSelectionScreen(
                viewModel = viewModel,
                onHuntSelected = { huntId ->
                    navController.navigate(Screen.HuntStart.createRoute(huntId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.HuntStart.route,
            arguments = listOf(
                navArgument("huntId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val huntId = backStackEntry.arguments?.getString("huntId") ?: ""
            val hunt = viewModel.getHuntById(huntId)
            
            hunt?.let {
                HuntStartScreen(
                    hunt = it,
                    onStartHunt = {
                        viewModel.startHunt(huntId)
                        navController.navigate(Screen.ActiveHunt.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(Screen.ActiveHunt.route) {
            ActiveHuntScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.HuntMap.route)
                },
                onNavigateToCheckIn = {
                    navController.navigate(Screen.CheckIn.route)
                },
                onHuntCompleted = {
                    viewModel.endHunt()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.HuntMap.route) {
            HuntMapScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CheckIn.route) {
            CheckInScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCheckInSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
