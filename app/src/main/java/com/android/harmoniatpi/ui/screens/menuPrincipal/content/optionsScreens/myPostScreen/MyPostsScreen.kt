package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.myPostScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components.CommentItem
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
        topBar = {
            // --- CAMBIO 1: TopAppBar actualizada ---
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis publicaciones",
                        style = MaterialTheme.typography.titleLarge,
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
                // --- CAMBIO 2: Color consistente con HomeScreen ---
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        // --- CAMBIO 3: Fondo consistente con CommunityScreen ---
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- CAMBIO 4: Se quita el padding de la Columna ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                // .padding(16.dp) // <-- Eliminado
            ) {
                if (uiState.myPostsList.isEmpty()) {
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
                    // --- CAMBIO 5: LazyColumn limpia ---
                    LazyColumn(modifier = Modifier.fillMaxSize()) { // <-- Arrangement eliminado
                        items(uiState.myPostsList) { post ->
                            // --- CAMBIO 6: Se llama al NUEVO PostCard ---
                            PostCard(
                                post = post,
                                onLikeClicked = {
                                    // El DrawerContentViewModel no tiene updateLikes
                                    // Si lo añades, pon la llamada aquí.
                                },
                                onCommentClicked = { selectedPostForComments = post },
                                onDeleteClicked = { viewModel.deleteMyPost(post.id) },
                                isMyPost = true, // En esta pantalla, siempre es tu post
                                isAlreadyCloned = false, // No relevante
                                onCloneClicked = { /* No relevante */ }
                            )
                        }
                    }
                }
            }

            // --- CAMBIO 7: Se llama al NUEVO BottomSheet ---
            if (selectedPostForComments != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedPostForComments = null },
                    sheetState = modalBottomSheetState
                ) {
                    // Se reutiliza el Composable de CommunityScreen
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