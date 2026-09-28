package com.rblxinsider.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.rblxinsider.app.ui.auth.AuthScreen
import com.rblxinsider.app.ui.create.CreatePostScreen
import com.rblxinsider.app.ui.feed.FeedScreen
import com.rblxinsider.app.ui.postdetail.PostDetailScreen
import com.rblxinsider.app.ui.profile.ProfileScreen
import com.rblxinsider.app.ui.saved.SavedScreen
import com.rblxinsider.app.ui.updatetracker.UpdateTrackerScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Feed : Screen("feed")
    object UpdateTracker : Screen("update_tracker")
    object CreatePost : Screen("create_post")
    object Saved : Screen("saved")
    object Profile : Screen("profile")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Feed.route else Screen.Auth.route
    ) {
        composable(route = Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Feed.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }
        composable(route = Screen.Feed.route) {
            FeedScreen(onPostClick = { postId ->
                navController.navigate(Screen.PostDetail.createRoute(postId))
            })
        }
        composable(route = Screen.UpdateTracker.route) {
            UpdateTrackerScreen()
        }
        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(onPostSuccess = {
                navController.navigate(Screen.Feed.route) {
                    popUpTo(Screen.CreatePost.route) { inclusive = true }
                }
            })
        }
        composable(route = Screen.Saved.route) {
            SavedScreen(onPostClick = { postId ->
                navController.navigate(Screen.PostDetail.createRoute(postId))
            })
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                }
            )
        }
        composable(route = Screen.PostDetail.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}