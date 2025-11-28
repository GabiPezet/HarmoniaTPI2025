package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.myPostScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.CommentsBottomSheetContent
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.PostCard
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    viewModel: DrawerContentViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val myFilteredPosts = remember(uiState.myPostsList, uiState.userID) {
        uiState.myPostsList.filter { it.userID == uiState.userID }
    }

    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Acción de retroceso centralizada
    val onBackPressed = {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    BackHandler {
        onBackPressed()
    }

    Scaffold(
        modifier = Modifier.testTag("MY_POSTS_SCREEN"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        text = "Mis publicaciones",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- Usamos la lista filtrada aquí ---
                if (myFilteredPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Todavía no publicaste nada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // --- Iteramos sobre myFilteredPosts ---
                        items(myFilteredPosts) { post ->
                            val isCloningThisPost = uiState.cloningPostId == post.id
                            val friendsList = uiState.currentUserData?.friendsList?.map { it.id } ?: emptyList()
                            val isFriend = post.userID in friendsList

                            PostCard(
                                post = post,
                                userName = uiState.userName,
                                userLastName = uiState.userLastName,
                                onLikeClicked = {
                                },
                                onCommentClicked = { selectedPostForComments = post },
                                onDeleteClicked = { viewModel.deleteMyPost(post.id) },
                                isMyPost = true,
                                isAlreadyCloned = false,
                                onCloneClicked = { /* No relevante */ },
                                viewUserProfile = {},
                                isCloningThisPost = isCloningThisPost,
                                isFriend = isFriend
                            )
                        }
                    }
                }
            }

            if (selectedPostForComments != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedPostForComments = null },
                    sheetState = modalBottomSheetState
                ) {
                    CommentsBottomSheetContent(
                        post = selectedPostForComments!!,
                        onCommentAdded = { comment ->
                            viewModel.updateComments(selectedPostForComments!!, comment)
                        }
                    )
                }
            }
        }
    }
}