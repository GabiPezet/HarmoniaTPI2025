package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetUsersFromFirestoreUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.ObserveCurrentUserUseCase
import com.android.harmoniatpi.domain.usecases.friendUseCases.AcceptFriendRequestUseCase
import com.android.harmoniatpi.domain.usecases.friendUseCases.DeclineFriendRequestUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.model.FriendsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val sharedMenuUiState: SharedMenuUiState,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getUsersFromFirestoreUseCase: GetUsersFromFirestoreUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val declineFriendRequestUseCase: DeclineFriendRequestUseCase
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

            // Solo observamos al usuario actual (Room), que tiene la lista de IDs de amigos
            observeCurrentUserUseCase().collect { localCurrentUser ->

                this@FriendsViewModel.currentUser = localCurrentUser

                if (localCurrentUser == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                val rawFriends = localCurrentUser.friendsList
                val requestsReceived = localCurrentUser.friendRequestReceived

                // 1. Obtenemos TODOS los IDs necesarios (Amigos + Solicitudes)
                val friendIds = rawFriends.map { it.id }
                val requestIds = requestsReceived.map { it.fromUserID }
                val allRequiredIds = (friendIds + requestIds).distinct()

                if (allRequiredIds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            friendsList = emptyList(),
                            requestList = emptyList()
                        )
                    }
                    return@collect
                }

                // 2. Llamamos a Firestore para obtener datos FRESCOS (Solo lectura)
                // Esto soluciona lo de Juanma, porque siempre traerá su foto actual de la nube.
                launch(Dispatchers.IO) {
                    getUsersFromFirestoreUseCase(allRequiredIds)
                        .onSuccess { remoteUsers ->

                            // 3. Cruzar datos en Memoria (Sin tocar Room)

                            // A) Actualizar lista de amigos con fotos nuevas
                            val updatedFriends = rawFriends.map { friend ->
                                val remoteData = remoteUsers.find { it.userID == friend.id }
                                if (remoteData != null) {
                                    // Usamos la foto y nombre frescos de Firestore
                                    friend.copy(
                                        urlPhoto = remoteData.userPhotoPathRemote,
                                        name = remoteData.userName,
                                        lastName = remoteData.userLastName
                                    )
                                } else {
                                    friend // Si falla, mantenemos los datos viejos que teníamos
                                }
                            }

                            // B) Preparar lista de solicitudes (objetos User completos para la UI)
                            val requestUserProfiles = remoteUsers.filter { user ->
                                requestIds.contains(user.userID)
                            }

                            // 4. Actualizar UI State
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    friendsList = updatedFriends,
                                    requestList = requestUserProfiles,
                                    friendRequestReceived = requestsReceived,
                                    loadingActionId = it.loadingActionId?.takeIf { id ->
                                        requestsReceived.any { req -> req.fromUserID == id }
                                    }
                                )
                            }
                            sharedMenuUiState.updateState { it.copy(totalFriends = updatedFriends.size) }
                        }
                        .onFailure {
                            Log.e("FriendsViewModel", "Error cargando amigos", it)
                            _uiState.update { it.copy(isLoading = false) }
                        }
                }
            }
        }
    }

    fun handleRequest(request: FriendRequestReceived, accept: Boolean) {
        val user = this@FriendsViewModel.currentUser ?: return

        _uiState.update { it.copy(loadingActionId = request.fromUserID) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (accept) {
                acceptFriendRequestUseCase(user, request)
            } else {
                declineFriendRequestUseCase(user, request)
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