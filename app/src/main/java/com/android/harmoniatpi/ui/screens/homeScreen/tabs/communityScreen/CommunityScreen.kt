package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel.CommunityViewModel
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(
    onExitApp: () -> Unit,
    drawerState: DrawerState,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onExitApp()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.posts) { post ->
                    PostCard(post)
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onNewPostClicked() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo post")
        }

        if (uiState.showCreateDialog) {
            CreatePostDialog(
                userName = uiState.userName,
                userLastName = uiState.userLastName,
                onDismiss = { viewModel.dismissDialog() },
                onPostCreated = { title, description, hashtags ->
                    viewModel.addPost(title, description, hashtags)
                }
            )
        }
    }
}


@Composable
fun CreatePostDialog(
    userName: String,
    userLastName: String,
    onDismiss: () -> Unit,
    onPostCreated: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hashtagsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo Post de $userName $userLastName")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = hashtagsText,
                    onValueChange = { hashtagsText = it },
                    label = { Text("Hashtags (separados por comas)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val hashtags = hashtagsText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                onPostCreated(title, description, hashtags)
                onDismiss()
            }) {
                Text("Publicar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("${post.name} ${post.lasName}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(post.description)
            if (post.hashtags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.hashtags.forEach {
                        AssistChip(onClick = {}, label = { Text("#$it") })
                    }
                }
            }
        }
    }
}

