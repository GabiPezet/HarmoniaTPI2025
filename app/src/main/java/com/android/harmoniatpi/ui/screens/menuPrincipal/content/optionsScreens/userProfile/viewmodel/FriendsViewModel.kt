package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.Friend
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.FetchAndSyncUsersUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetAllUserFromDBUseCase
// ✨ 1. AÑADE ESTA IMPORTACIÓN (la que usaba antes)
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
    private val sharedMenuUiState: SharedMenuUiState, // <-- Se queda, pero no para 'currentUser'
    private val fetchAndSyncUsersUseCase: FetchAndSyncUsersUseCase,
    private val getAllUsersUseCase: GetAllUserFromDBUseCase,
    // ✨ 2. AÑADE DE NUEVO EL OBSERVADOR
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState = _uiState.asStateFlow()


    private var currentUser: UserPreferences? = null

    init {
        observeData()
    }

    // ✨ 3. REVIERTE LA FUNCIÓN 'observeData' A SU LÓGICA ORIGINAL
    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combina el Flow del usuario actual (de Firestore)
            // con el Flow de TODOS los usuarios (de Room)
            combine(
                observeCurrentUserUseCase(), // <-- DEBE LEER DE AQUÍ
                getAllUsersUseCase()
            ) { localCurrentUser, allUsersInRoom ->

                this@FriendsViewModel.currentUser = localCurrentUser

                if (localCurrentUser == null) {
                    return@combine null // Aún no estamos listos
                }

                val friends = localCurrentUser.friendsList
                val requestsReceived = localCurrentUser.friendRequestReceived
                val requestIds = requestsReceived.map { it.fromUserID }

                // Busca perfiles de usuario que falten
                val usersToFetch = requestIds.filter { reqId ->
                    allUsersInRoom.none { it.userID == reqId }
                }
                if (usersToFetch.isNotEmpty()) {
                    launch(Dispatchers.IO) {
                        fetchAndSyncUsersUseCase(usersToFetch)
                            .onFailure { Log.e("FriendsViewModel", "Error fetching profiles", it) }
                    }
                }

                // Mapea los perfiles que SÍ tenemos
                val requestUserProfiles = allUsersInRoom.filter { user ->
                    requestIds.contains(user.userID)
                }

                // Retorna los datos listos para la UI
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
                // ✨ 4. AHORA ACTUALIZAMOS EL SHARED STATE MANUALMENTE ✨
                // Esto soluciona el bug original del estado obsoleto
                val updatedUser = result.getOrNull() // Este es el 'updatedCurrentUser'
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

    // ✨ 5. ELIMINA LA FUNCIÓN 'toUserPreferences' (ya no se usa aquí)
}