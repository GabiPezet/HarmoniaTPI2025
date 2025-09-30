
package com.android.harmoniatpi.ui.screens.createProject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.model.Project
import com.android.harmoniatpi.ui.screens.createProjectScreen.model.CreateProjectUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    // Si luego usás un caso de uso o repo, lo inyectás aquí
    // private val saveProjectUseCase: SaveProjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
        validateForm()
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
        validateForm()
    }

    fun onHashtagsChange(hashtags: String) {
        _uiState.update { it.copy(hashtags = hashtags) }
        validateForm()
    }

    fun saveProject(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!_uiState.value.isFormValid) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val project = Project(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    hashtags = _uiState.value.hashtags
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }.toString(),
                )

                // Aquí guardarías el proyecto, por ejemplo:
                // saveProjectUseCase(project)

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    private fun validateForm() {
        val currentState = _uiState.value
        val isTitleValid = currentState.title.isNotBlank()
        val isFormValid = isTitleValid

        _uiState.update {
            it.copy(
                isTitleValid = isTitleValid,
                isFormValid = isFormValid
            )
        }
    }
}





