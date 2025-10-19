package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.userPreferences.Comment
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeletePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllPostFromFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.InsertNewPostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UpdatePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.model.CommunityUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val sharedMenuUiState: SharedMenuUiState,
    private val insertNewPostFirebaseDataBaseUseCase: InsertNewPostFirebaseDataBaseUseCase,
    private val getAllPostFromFirebaseDataBaseUseCase: GetAllPostFromFirebaseDataBaseUseCase,
    private val updatePostFirebaseDataBaseUseCase: UpdatePostFirebaseDataBaseUseCase,
    private val deletePostFirebaseDataBaseUseCase: DeletePostFirebaseDataBaseUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sharedMenuUiState.uiState.collect { uiState ->
                _uiState.update {
                    it.copy(
                        userName = uiState.userName,
                        userLastName = uiState.userLastName,
                        userID = uiState.userID,
                        userPhotoPathRemote = uiState.userPhotoPathRemote
                    )
                }
            }
        }

        viewModelScope.launch {
            getAllPostFromFirebaseDataBaseUseCase(_uiState.value.userID).collect { posts ->
                _uiState.update { it.copy(posts = posts) }
            }
        }
    }

    fun onNewPostClicked() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun addPost(title: String, description: String, hashtags: List<String>) {
        val newPost = Post(
            id = System.currentTimeMillis().toString(),
            userID = _uiState.value.userID,
            userImagePathURL = _uiState.value.userPhotoPathRemote,
            title = title,
            description = description,
            name = _uiState.value.userName,
            lasName = _uiState.value.userLastName,
            hashtags = hashtags,
            idProject = "",
            urlCompleteAudio = "",
            urlAudioTracks = emptyList(),
            imageUrl = "",
            createdAt = LocalDateTime.now().toString(),
            likes = 0,
            totalShared = 0,
            comments = emptyList(),
            clonedOption = false
        )
        viewModelScope.launch {
            insertNewPostFirebaseDataBaseUseCase(newPost)
        }

    }

    fun updateLikes(post: Post) {
        val newPost = post.copy(likes = post.likes + 1)
        viewModelScope.launch {
            updatePostFirebaseDataBaseUseCase(newPost)
        }
    }

    fun updateComments(post: Post, comment: String) {
        val newComment = Comment(
            id = System.currentTimeMillis().toString(),
            name = _uiState.value.userName,
            lastName = _uiState.value.userLastName,
            comment = comment,
            photoUrlUser = _uiState.value.userPhotoPathRemote
        )
        val newPost = post.copy(comments = post.comments + newComment)
        viewModelScope.launch {
            updatePostFirebaseDataBaseUseCase(newPost)
        }

    }

    fun deleteMyPost(postID: String) {
        viewModelScope.launch {
            deletePostFirebaseDataBaseUseCase(postID)
        }
    }
}
