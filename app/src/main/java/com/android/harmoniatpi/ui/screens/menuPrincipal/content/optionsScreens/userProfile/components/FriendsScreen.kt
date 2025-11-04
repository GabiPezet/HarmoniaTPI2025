package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.viewmodel.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    uiState: MenuUiState,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Seguidores", "Solicitudes")

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    val count = if (index == 0) {
                        uiState.friendsList.size
                    } else {
                        uiState.requestList.size
                    }

                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text("$title ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    // Pestaña Seguidores
                    0 -> FriendsList(
                        friends = uiState.friendsList,
                        onUnfollowClick = { /* TODO: Implementar lógica unfollow */ }
                    )
                    // Pestaña Solicitudes
                    1 -> FriendsList(
                        requests = uiState.requestList, // Lista de perfiles de usuario
                        // --- 1. PASA LA LISTA DE SOLICITUDES ---
                        // Esta lista contiene los 'FriendRequestReceived'
                        requestsReceived = uiState.friendRequestReceived,
                        loadingActionId = uiState.loadingActionId,

                        // --- 2. MODIFICA onAccept y onDecline ---
                        onAccept = { requestToHandle ->
                            // 'requestToHandle' ya es el objeto FriendRequestReceived
                            viewModel.handleRequest(requestToHandle, accept = true)
                        },
                        onDecline = { requestToHandle ->
                            // 'requestToHandle' ya es el objeto FriendRequestReceived
                            viewModel.handleRequest(requestToHandle, accept = false)
                        }
                    )
                }
            }
        }
    }


@Composable
fun FriendsList(
    friends: List<Friend>,
    onUnfollowClick: (Friend) -> Unit
) {
    if (friends.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes seguidores aún.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(friends) { friend ->
                FriendsItem(
                    friend = friend,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun FriendsList(
    requests: List<UserPreferences>,
    requestsReceived: List<FriendRequestReceived>,
    loadingActionId: String?,
    onAccept: (FriendRequestReceived) -> Unit,
    onDecline: (FriendRequestReceived) -> Unit
) {
    if (requests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes solicitudes pendientes.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(requests) { user ->
                val request = remember(user.userID, requestsReceived) {
                    requestsReceived.find { it.fromUserID == user.userID }
                }

                RequestItem(
                    user = user,
                    isLoading = loadingActionId == user.userID,
                    onAccept = { request?.let { onAccept(it) } }, // Pasa el objeto 'request'
                    onDecline = { request?.let { onDecline(it) } } // Pasa el objeto 'request'
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun FriendsItem(
    friend: Friend,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = friend.urlPhoto,
            contentDescription = "Foto de ${friend.name}",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${friend.name} ${friend.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RequestItem(
    user: UserPreferences,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.userPhotoPathRemote,
            contentDescription = "Foto de ${user.userName}",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${user.userName} ${user.userLastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onDecline,
                ) {
                    Icon(Icons.Default.Close, "Rechazar")
                }
                IconButton(
                    onClick = onAccept,
                ) {
                    Icon(Icons.Default.Check, "Aceptar")
                }
            }
        }
    }
}