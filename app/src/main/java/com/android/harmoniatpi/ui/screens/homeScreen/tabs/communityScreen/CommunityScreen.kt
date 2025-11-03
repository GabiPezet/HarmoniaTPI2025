package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.CommentsBottomSheetContent
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.CreatePostDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.PostCard
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.UserProfileDialog
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel.CommunityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onExitApp: () -> Unit,
    drawerState: DrawerState,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) } // Cambiar a ID

    // Encontrar el post seleccionado actualizado
    val selectedPostForComments = remember(selectedPostIdForComments, uiState.posts) {
        uiState.posts.find { it.id == selectedPostIdForComments }
    }

    // 1. Obtenemos el contexto actual
    val context = LocalContext.current

    var postToClone by remember { mutableStateOf<Post?>(null) }
    // 2. Escuchamos el flow de eventos del ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onExitApp()
        }
    }

    if (uiState.showUserProfile) {
        val userProfile = uiState.userSelected
        if (userProfile != null) {
            UserProfileDialog(
                userPreferences = userProfile,
                onDismiss = { viewModel.onDismissUserProfile() }
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.posts) { post ->

                        val projectData = uiState.localProjects.find {
                            it.originalProjectId == post.idProject && it.ownerId == uiState.userID
                        }
                        val isAlreadyCloned = projectData != null
                        val isCloningThisPost = uiState.cloningPostId == post.id
                        PostCard(
                            post = post,
                            onLikeClicked = { viewModel.updateLikes(post) },
                            onCommentClicked = {
                                selectedPostIdForComments = post.id // Guardar solo el ID
                            },
                            onDeleteClicked = { viewModel.deleteMyPost(post) },
                            isMyPost = post.userID == uiState.userID,
                            isAlreadyCloned = isAlreadyCloned,
                            isCloningThisPost = isCloningThisPost,
                            onCloneClicked = {
                                postToClone = post
                            },
                            viewUserProfile = { id ->
                                viewModel.onClickUserProfile(id)
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { viewModel.onNewPostClicked() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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

            if (postToClone != null) {
                ShowConfirmationDialog(
                    show = true,
                    onDismiss = { postToClone = null },
                    onConfirm = {
                        postToClone?.let {
                            viewModel.cloneProject(it)
                            // --- QUITA ESTA LÍNEA ---
                            // viewModel.updateCloned(it)
                        }
                        postToClone = null
                    },
                    title = "Clonar Proyecto",
                    message = "¿Estás seguro de que quieres clonar este proyecto a tu pestaña de 'Colaboraciones'?",
                    confirmText = "Clonar"
                )
            }
        }
    }




    // ModalBottomSheet
    if (selectedPostForComments != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPostIdForComments = null },
            sheetState = modalBottomSheetState
        ) {
            CommentsBottomSheetContent(
                post = selectedPostForComments, // Este se actualiza automáticamente
                onCommentAdded = { comment ->
                    viewModel.updateComments(selectedPostForComments, comment)
                }
            )
        }
    }
}








