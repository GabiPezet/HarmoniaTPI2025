package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.userPreferences.Post
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
    private val sharedMenuUiState: SharedMenuUiState
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sharedMenuUiState.uiState.collect { uiState ->
                _uiState.update {
                    it.copy(
                        userName = uiState.userName,
                        userLastName = uiState.userLastName
                    )
                }

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
            title = title,
            description = description,
            name = _uiState.value.userName,
            lasName = _uiState.value.userLastName,
            hashtags = hashtags,
            urlCompleteAudio = "",
            urlAudioTracks = emptyList(),
            createdAt = LocalDateTime.now().toString(),
            likes = 0,
            totalShared = 0,
            comments = emptyList(),
            clonedOption = false
        )

        _uiState.update {
            it.copy(
                posts = it.posts + newPost,
                showCreateDialog = false
            )
        }
    }
}
