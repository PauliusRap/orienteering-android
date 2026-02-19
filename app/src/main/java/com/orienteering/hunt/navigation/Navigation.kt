package com.orienteering.hunt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.orienteering.hunt.HuntApplication
import com.orienteering.hunt.data.repository.HuntRepository
import com.orienteering.hunt.services.LocationService
import com.orienteering.hunt.ui.screens.ActiveHuntScreen
import com.orienteering.hunt.ui.screens.CheckInScreen
import com.orienteering.hunt.ui.screens.HomeScreen
import com.orienteering.hunt.ui.screens.HuntMapScreen
import com.orienteering.hunt.ui.screens.HuntSelectionScreen
import com.orienteering.hunt.ui.screens.HuntStartScreen
import com.orienteering.hunt.ui.screens.LeaderboardScreen
import com.orienteering.hunt.ui.screens.LoginScreen
import com.orienteering.hunt.ui.screens.ProfileScreen
import com.orienteering.hunt.ui.screens.RegisterScreen
import com.orienteering.hunt.viewmodel.AuthViewModel
import com.orienteering.hunt.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
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
    locationService: LocationService,
    application: HuntApplication
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    return AuthViewModel(application.apiService, application.authManager) as T
                }
            }
        }
    )
    
    val gameViewModel: GameViewModel = viewModel(
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
    
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val activeProgress by gameViewModel.activeProgress.collectAsStateWithLifecycle()
    
    LaunchedEffect(authState.currentPlayer) {
        authState.currentPlayer?.let { player ->
            gameViewModel.setPlayer(player)
        }
    }
    
    val startDestination = when {
        authState.isCheckingAuth -> Screen.Login.route
        authState.isLoggedIn -> Screen.Home.route
        else -> Screen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = gameViewModel,
                authViewModel = authViewModel,
                onNavigateToHunts = {
                    navController.navigate(Screen.HuntSelection.route)
                },
                onNavigateToActiveHunt = {
                    navController.navigate(Screen.ActiveHunt.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.HuntSelection.route) {
            HuntSelectionScreen(
                viewModel = gameViewModel,
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
            val hunt = gameViewModel.getHuntById(huntId)
            
            hunt?.let {
                HuntStartScreen(
                    hunt = it,
                    onStartHunt = {
                        gameViewModel.startHunt(huntId)
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
                viewModel = gameViewModel,
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
                    gameViewModel.endHunt()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.HuntMap.route) {
            HuntMapScreen(
                viewModel = gameViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CheckIn.route) {
            CheckInScreen(
                viewModel = gameViewModel,
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
                viewModel = gameViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
