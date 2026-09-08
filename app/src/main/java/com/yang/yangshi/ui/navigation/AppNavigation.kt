package com.yang.yangshi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yang.yangshi.data.local.DietDatabase
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.ui.dashboard.DashboardScreen
import com.yang.yangshi.ui.dashboard.DashboardViewModel
import com.yang.yangshi.ui.mealportion.MealPortionScreen
import com.yang.yangshi.ui.mealportion.MealPortionViewModel
import com.yang.yangshi.ui.profile.ProfileScreen
import com.yang.yangshi.ui.profile.ProfileViewModel

sealed class Screen(val route: String) {
    object Diet : Screen("diet")
    object Profile : Screen("profile")
    object MealPortion : Screen("meal_portion")
}

@Composable
fun MainAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val repository = remember {
        DietRepository(DietDatabase.getInstance(context))
    }

    val mealPortionViewModel: MealPortionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MealPortionViewModel(repository) as T
            }
        }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.MealPortion.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 1. 左侧: 饮食
                NavigationBarItem(
                    selected = currentRoute == Screen.Diet.route,
                    onClick = {
                        navController.navigate(Screen.Diet.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Text("🥗", fontSize = 18.sp) },
                    label = { Text("饮食", fontWeight = FontWeight.Bold) }
                )

                // 2. 右侧: 我的
                NavigationBarItem(
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Text("👤", fontSize = 18.sp) },
                    label = { Text("我的", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Diet.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Diet.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return DashboardViewModel(repository) as T
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
                MealPortionScreen(viewModel = mealPortionViewModel)
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
