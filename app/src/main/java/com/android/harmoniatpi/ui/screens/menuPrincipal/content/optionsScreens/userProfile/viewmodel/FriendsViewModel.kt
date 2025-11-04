package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.FetchAndSyncUsersUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllUserFromDBUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.ObserveCurrentUserUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.model.FriendsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedMenuUiState: SharedMenuUiState,
    private val fetchAndSyncUsersUseCase: FetchAndSyncUsersUseCase,
    private val getAllUsersUseCase: GetAllUserFromDBUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState = _uiState.asStateFlow()


    private var currentUser: UserPreferences? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                observeCurrentUserUseCase(),
                getAllUsersUseCase()
            ) { localCurrentUser, allUsersInRoom ->

                this@FriendsViewModel.currentUser = localCurrentUser

                if (localCurrentUser == null) {
                    return@combine null
                }

                val friends = localCurrentUser.friendsList
                val requestsReceived = localCurrentUser.friendRequestReceived
                val requestIds = requestsReceived.map { it.fromUserID }

                val usersToFetch = requestIds.filter { reqId ->
                    allUsersInRoom.none { it.userID == reqId }
                }
                if (usersToFetch.isNotEmpty()) {
                    launch(Dispatchers.IO) {
                        fetchAndSyncUsersUseCase(usersToFetch)
                            .onFailure { Log.e("FriendsViewModel", "Error fetching profiles", it) }
                    }
                }


                val requestUserProfiles = allUsersInRoom.filter { user ->
                    requestIds.contains(user.userID)
                }


                Triple(friends, requestUserProfiles, requestsReceived)

            }.collect { data ->
                if (data != null) {
                    val (friends, requestProfiles, requestsReceived) = data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            friendsList = friends,
                            requestList = requestProfiles,
                            friendRequestReceived = requestsReceived,
                            loadingActionId = it.loadingActionId?.takeIf { id ->
                                requestsReceived.any { req -> req.fromUserID == id }
                            }
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = true) } // Sigue cargando
                }
            }
        }
    }

    fun handleRequest(request: FriendRequestReceived, accept: Boolean) {
        val user = this@FriendsViewModel.currentUser ?: return

        _uiState.update { it.copy(loadingActionId = request.fromUserID) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (accept) {
                repository.acceptFriendRequest(user, request)
            } else {
                repository.declineFriendRequest(user, request)
            }

            if (result.isSuccess) {
                Log.d("FriendsViewModel", "Solicitud manejada con éxito.")

                val updatedUser = result.getOrNull()
                if (updatedUser != null) {
                    sharedMenuUiState.updateState {
                        it.copy(
                            friendsList = updatedUser.friendsList,
                            friendRequestReceived = updatedUser.friendRequestReceived
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(loadingActionId = null) }
            }
        }
    }

}