package com.nonsense.chat.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nonsense.chat.data.AuthState
import com.nonsense.chat.ui.auth.AuthScreen
import com.nonsense.chat.ui.chat.ChatScreen
import com.nonsense.chat.ui.chatlist.ChatListScreen
import com.nonsense.chat.ui.friends.FriendsScreen
import com.nonsense.chat.ui.group.GroupInfoScreen
import com.nonsense.chat.ui.group.NewGroupScreen
import com.nonsense.chat.ui.profile.EditProfileScreen
import com.nonsense.chat.ui.profile.ProfileScreen
import com.nonsense.chat.ui.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    deepLinkChatId: String?,
    onDeepLinkConsumed: () -> Unit,
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    val authState by rootViewModel.authState.collectAsState()

    if (authState == AuthState.LOADING) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val start = if (authState == AuthState.AUTHENTICATED) Routes.CHATS else Routes.AUTH

    // Route notification taps to the target chat once authenticated.
    LaunchedEffect(authState, deepLinkChatId) {
        if (authState == AuthState.AUTHENTICATED && deepLinkChatId != null) {
            navController.navigate(Routes.chat(deepLinkChatId))
            onDeepLinkConsumed()
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.AUTH) { AuthScreen() }

        composable(Routes.CHATS) {
            ChatListScreen(
                onOpenChat = { navController.navigate(Routes.chat(it)) },
                onNewChat = { navController.navigate(Routes.NEW_CHAT) },
                onNewGroup = { navController.navigate(Routes.NEW_GROUP) },
                onOpenFriends = { navController.navigate(Routes.FRIENDS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
            )
        }

        composable(
            Routes.CHAT,
            arguments = listOf(navArgument(Routes.ARG_CHAT_ID) { type = NavType.StringType }),
        ) {
            ChatScreen(
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
                onOpenGroupInfo = { navController.navigate(Routes.groupInfo(it)) },
                onOpenCall = { navController.navigate(Routes.CALL) },
            )
        }

        composable(Routes.CALL) {
            com.nonsense.chat.call.CallScreen(onClose = { navController.popBackStack() })
        }

        composable(
            Routes.PROFILE,
            arguments = listOf(navArgument(Routes.ARG_UID) { type = NavType.StringType }),
        ) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenDm = { navController.navigate(Routes.chat(it)) { popUpTo(Routes.CHATS) } },
            )
        }

        composable(Routes.EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
            )
        }
        composable(Routes.FRIENDS) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { navController.navigate(Routes.chat(it)) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
            )
        }
        composable(Routes.NEW_CHAT) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { navController.navigate(Routes.chat(it)) { popUpTo(Routes.CHATS) } },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
            )
        }
        composable(Routes.NEW_GROUP) {
            NewGroupScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.navigate(Routes.chat(it)) { popUpTo(Routes.CHATS) } },
            )
        }
        composable(
            Routes.GROUP_INFO,
            arguments = listOf(navArgument(Routes.ARG_CHAT_ID) { type = NavType.StringType }),
        ) {
            GroupInfoScreen(
                onBack = { navController.popBackStack() },
                onLeft = { navController.popBackStack(Routes.CHATS, inclusive = false) },
                onOpenProfile = { navController.navigate(Routes.profile(it)) },
            )
        }
    }
}
