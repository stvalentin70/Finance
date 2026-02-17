package com.stvalentin.finance.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stvalentin.finance.R

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    data object Main : BottomNavItem(
        route = "main",
        titleResId = R.string.main_screen,
        icon = Icons.Default.Home
    )
    
    data object Calendar : BottomNavItem(
        route = "payment_calendar",
        titleResId = R.string.calendar_screen,
        icon = Icons.Default.CalendarToday
    )
    
    data object History : BottomNavItem(
        route = "history",
        titleResId = R.string.history_screen,
        icon = Icons.Default.History
    )
    
    data object Statistics : BottomNavItem(
        route = "statistics",
        titleResId = R.string.statistics_screen,
        icon = Icons.Default.PieChart
    )
    
    data object Settings : BottomNavItem(
        route = "settings",
        titleResId = R.string.settings_screen,
        icon = Icons.Default.Settings
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Main,
        BottomNavItem.Calendar,
        BottomNavItem.History,
        BottomNavItem.Statistics,
        BottomNavItem.Settings
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.titleResId),
                        fontSize = 10.sp  // Уменьшено с 11sp до 10sp
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}