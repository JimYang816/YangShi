package com.yang.yangshi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yang.yangshi.data.local.DietDatabase
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.domain.model.MealType
import com.yang.yangshi.ui.dashboard.DashboardScreen
import com.yang.yangshi.ui.dashboard.DashboardViewModel
import com.yang.yangshi.ui.history.HistoryScreen
import com.yang.yangshi.ui.history.HistoryViewModel
import com.yang.yangshi.ui.mealportion.MealPortionScreen
import com.yang.yangshi.ui.mealportion.MealPortionViewModel
import com.yang.yangshi.ui.profile.ProfileScreen
import com.yang.yangshi.ui.profile.ProfileViewModel

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "大盘")
    object MealPortion : Screen("meal_portion", "食物计算")
    object History : Screen("history", "历史日志")
    object Profile : Screen("profile", "身体配置")
}

@Composable
fun MainAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val repository = remember {
        DietRepository(DietDatabase.getInstance(context))
    }

    val items = listOf(
        Screen.Dashboard,
        Screen.MealPortion,
        Screen.History,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(screen.title.take(2)) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return DashboardViewModel(repository) as T
                        }
                    }
                )
                val mealPortionViewModel: MealPortionViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return MealPortionViewModel(repository) as T
                        }
                    }
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMealPortion = { mealType ->
                        mealPortionViewModel.setMealType(mealType)
                        navController.navigate(Screen.MealPortion.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.MealPortion.route) {
                val mealPortionViewModel: MealPortionViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return MealPortionViewModel(repository) as T
                        }
                    }
                )
                MealPortionScreen(viewModel = mealPortionViewModel)
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return HistoryViewModel(repository) as T
                        }
                    }
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(repository) as T
                        }
                    }
                )
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
