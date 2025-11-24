package com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.domain.model.userPreferences.Comment
import com.android.harmoniatpi.domain.model.userPreferences.ContactData
import com.android.harmoniatpi.domain.model.userPreferences.Post
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeletePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.LogOutFirebaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UpdatePostFirebaseDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.UploadLocalFileToFirebaseStorage
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetAllProjectsFromDBUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetMyPostFromDataBaseUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetUserPreferencesUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.SetUserPreferencesUseCase
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.ProfileImageUser
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.SharedMenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DrawerContentViewModel @Inject constructor(
    private val logOutUseCase: LogOutFirebaseUseCase,
    private val sharedMenuUiState: SharedMenuUiState,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setUserPreferencesUseCase: SetUserPreferencesUseCase,
    private val uploadLocalFileToFirebaseStorage: UploadLocalFileToFirebaseStorage,
    private val getMyPostFromDataBaseUseCase: GetMyPostFromDataBaseUseCase,
    private val updatePostFirebaseDataBaseUseCase: UpdatePostFirebaseDataBaseUseCase,
    private val deletePostFirebaseDataBaseUseCase: DeletePostFirebaseDataBaseUseCase,
    private val getAllProjectsFromDBUseCase: GetAllProjectsFromDBUseCase,
    private val repository: Repository,
) : ViewModel() {

    val uiState = sharedMenuUiState.uiState

    private val _userPhotoPath = MutableStateFlow(ProfileImageUser())
    val userPhotoPath = _userPhotoPath.asStateFlow()

    private val _contactData = MutableStateFlow(ContactData())
    val contactData: StateFlow<ContactData> = _contactData.asStateFlow()
    //TODO: Hablar con Facu para ver si podemos persistir esta info en Firestore o en RTDatabase

    fun initUserPreferences() {
        viewModelScope.launch {
            repository.observeCurrentUserFromFirestore().collectLatest { currentUser ->
                if (currentUser != null) {
                    Log.d("KlyxDevs", "DrawerViewModel: Recibido update de usuario. Premium: ${currentUser.isPremium}")
                    _userPhotoPath.update {
                        it.copy(
                            path = currentUser.userPhotoPath,
                            version = it.version + 1
                        )
                    }
                    sharedMenuUiState.updateState {
                        it.copy(
                            userEmail = currentUser.userEmail,
                            userName = currentUser.userName,
                            userLastName = currentUser.userLastName,
                            userPhotoPath = currentUser.userPhotoPath,
                            userPhotoPathRemote = currentUser.userPhotoPathRemote,
                            userID = currentUser.userID,
                            appTheme = currentUser.appTheme,
                            notificationsList = currentUser.notificationList,
                            newNotification = currentUser.newNotification,
                            instrument = currentUser.instrument,
                            genres = currentUser.genres,
                            location = currentUser.location,
                            rating = currentUser.rating,
                            friendsList = currentUser.friendsList,
                            projectsList = currentUser.projectsList,
                            myPostsList = currentUser.myPostsList,
                            friendRequestReceived = currentUser.friendRequestReceived,
                            friendRequestSent = currentUser.friendRequestSent,
                            subscriptionId = currentUser.subscriptionId,
                            isPremium = currentUser.isPremium
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            delay(2000L) // Mantén el delay si es necesario para la carga inicial
            initMyPostCollect()
        }
    }
    private fun initMyPostCollect() {
        viewModelScope.launch {
            getMyPostFromDataBaseUseCase().collect { posts ->
                sharedMenuUiState.updateState { it.copy(myPostsList = posts) }
                Log.i("KlyxDevs", "Posts: $posts")
            }
        }

        viewModelScope.launch {
            getAllProjectsFromDBUseCase().collect { projects ->
                sharedMenuUiState.updateState { it.copy(projectsList = projects) }
                Log.i("KlyxDevs", "Projects: $projects")
            }
        }
    }

    fun start() {
        initUserPreferences()
    }

    fun resetLogOutSuccess() {
        sharedMenuUiState.updateState { it.copy(logOutSuccess = false) }
    }

    fun toggleTheme(theme: AppTheme) {
        sharedMenuUiState.updateState {
            it.copy(
                appTheme = theme
            )
        }
    }

    fun updateUserPreferences() {

        val preferences = UserPreferences(
            userID = uiState.value.userID,
            userEmail = uiState.value.userEmail,
            userPhotoPath = uiState.value.userPhotoPath,
            userPhotoPathRemote = uiState.value.userPhotoPathRemote,
            userName = uiState.value.userName,
            userLastName = uiState.value.userLastName,
            appTheme = uiState.value.appTheme,
            notificationList = uiState.value.notificationsList,
            newNotification = uiState.value.newNotification,
            instrument = uiState.value.instrument,
            genres = uiState.value.genres,
            location = uiState.value.location,
            rating = uiState.value.rating,
            friendsList = uiState.value.friendsList,
            projectsList = uiState.value.projectsList,
            myPostsList = uiState.value.myPostsList,
            friendRequestReceived = uiState.value.friendRequestReceived,
            friendRequestSent = uiState.value.friendRequestSent,
            subscriptionId = uiState.value.subscriptionId,
            isPremium = uiState.value.isPremium
        )
        viewModelScope.launch(Dispatchers.IO) {
            setUserPreferencesUseCase(preferences)
        }
    }

    fun updateUserName(newName: String) {
        sharedMenuUiState.updateState { it.copy(userName = newName) }
    }

    fun updateWorkProfile(instrument: String, genres: String, location: String) {
        sharedMenuUiState.updateState {
            it.copy(
                instrument = instrument,
                genres = genres,
                location = location
            )
        }
    }

    fun updateRating(newRating: Float) {
        // Aseguramos que el valor siempre esté entre 0 y 5
        val clampedRating = newRating.coerceIn(0f, 5f)
        sharedMenuUiState.updateState {
            it.copy(rating = clampedRating)
        }
    }

    fun logOutUser() {
        viewModelScope.launch(Dispatchers.IO) {
            logOutUseCase()
            sharedMenuUiState.updateState {
                it.copy(
                    logOutSuccess = true
                )
            }
        }
    }

    fun changeOptionsMenu(option: OptionsMenu) {
        sharedMenuUiState.updateState { it.copy(optionsMenu = option) }
    }

    fun saveUserPhoto(path: String) {
        Log.i("FirebaseStorage", "Entrando en saveUserPhoto")
        _userPhotoPath.update {
            it.copy(
                path = path,
                version = it.version + 1
            )
        }
        sharedMenuUiState.updateState {
            it.copy(userPhotoPath = path)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("FirebaseStorage", "Entrando en viewModelScope")
                val localPath = path
                val remotePath = "profile_pictures/${uiState.value.userID}.jpg"
                Log.i("FirebaseStorage", "LocalPath: $localPath, RemotePath: $remotePath")

                val result = uploadLocalFileToFirebaseStorage(localPath, remotePath)
                result.onSuccess { url ->
                    sharedMenuUiState.updateState {
                        it.copy(userPhotoPathRemote = url)
                    }
                    Log.i("FirebaseStorage", "URL: $url")
                }.onFailure { e ->
                    Log.i("FirebaseStorage", "Error subiendo imagen", e)
                }
            }
        }
    }

    fun updateComments(post: Post, comment: String) {
        val newComment = Comment(
            id = System.currentTimeMillis().toString(),
            name = uiState.value.userName,
            lastName = uiState.value.userLastName,
            comment = comment,
            photoUrlUser = uiState.value.userPhotoPathRemote
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

    fun updateContactInfo(newData: ContactData) {
        _contactData.value = newData
        // TODO: Hablar con Facu para ver si podemos persistir esta info en Firestore o en RTDatabase
    }

}