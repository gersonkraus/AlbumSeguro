package com.familiaaco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.familiaaco.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Admin flow
        composable("login") { LoginScreen(navController) }
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("children_list") { ChildrenListScreen(navController) }
        composable("create_child") { CreateChildScreen(navController) }
        composable("child_detail/{childId}") { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId")
            ChildDetailScreen(navController, childId)
        }
        composable("media_upload/{criancaId}") { backStackEntry ->
            val criancaId = backStackEntry.arguments?.getString("criancaId")
            MediaUploadScreen(navController, criancaId)
        }
        composable("media_upload") { MediaUploadScreen(navController, criancaId = null) }
        composable("admin_list") { AdminListScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("logs") { LogsScreen(navController) }
        composable("video_player/{url}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")
            VideoPlayerScreen(navController, url?.let { java.net.URLDecoder.decode(it, "UTF-8") })
        }

        // Child flow
        composable("child_token") { ChildTokenInputScreen(navController) }
        composable("child_album/{token}") { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ChildAlbumScreen(navController, token)
        }
        composable("qr_scanner") { QRScannerScreen(navController) }
    }
}
