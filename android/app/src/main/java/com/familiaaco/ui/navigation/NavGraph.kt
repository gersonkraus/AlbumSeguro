package com.familiaaco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.familiaaco.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "child_token"
    ) {
        // Admin flow
        composable("login") { LoginScreen(navController) }
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("children_list") { ChildrenListScreen(navController) }
        composable("create_child") { CreateChildScreen(navController) }
        composable(
            route = "child_detail/{childId}?nome={nome}",
            arguments = listOf(
                androidx.navigation.navArgument("childId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("nome") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId")
            val nome = backStackEntry.arguments?.getString("nome")?.let {
                try { java.net.URLDecoder.decode(it, "UTF-8") } catch (_: Exception) { it }
            } ?: ""
            ChildDetailScreen(navController, childId, nome.ifBlank { null })
        }
        composable("media_upload/{criancaId}") { backStackEntry ->
            val criancaId = backStackEntry.arguments?.getString("criancaId")
            MediaUploadScreen(navController, criancaId)
        }
        composable("media_upload") { MediaUploadScreen(navController, criancaId = null) }
        composable("admin_list") { AdminListScreen(navController) }
        composable("app_config") { AppConfigScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("logs") { LogsScreen(navController) }
        composable("video_player/{url}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("url")
            val url = encoded?.let {
                try {
                    String(android.util.Base64.decode(it, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP), Charsets.UTF_8)
                } catch (_: Exception) { null }
            }
            VideoPlayerScreen(navController, url)
        }

        // Child flow
        composable("child_token") { ChildTokenInputScreen(navController) }
        composable(
            route = "child_album/{token}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://{host}/album/{token}" },
                navDeepLink { uriPattern = "http://{host}/album/{token}" }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ChildAlbumScreen(navController, token)
        }
        composable("qr_scanner") { QRScannerScreen(navController) }
        composable("nfc_scanner") { NFCScannerScreen(navController) }
    }
}
