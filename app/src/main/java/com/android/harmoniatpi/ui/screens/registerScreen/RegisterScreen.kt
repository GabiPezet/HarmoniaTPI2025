package com.android.harmoniatpi.ui.screens.registerScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.screens.registerScreen.model.RegisterUiState
import com.android.harmoniatpi.ui.screens.registerScreen.viewmodel.RegisterScreenViewModel

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    viewModel: RegisterScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            RegisterTopBar(onBack = onBackToLogin)
        },
        bottomBar = {
            RegisterButton(
                enabled = uiState.isFormValid && !uiState.isLoading,
                isLoading = uiState.isLoading,
                onClick = {
                    keyboardController?.hide()
                    viewModel.registerUser(
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Registro exitoso",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackToLogin()
                        },
                        onError = { error ->
                            Toast.makeText(
                                context,
                                "Error: $error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        RegisterContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier,
                text = "Registro",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver atrás",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun RegisterContent(
    modifier: Modifier = Modifier,
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Nombre
        RegisterTextField(
            label = "Nombre",
            value = uiState.name,
            onValueChange = onNameChange,
            isError = uiState.name.isNotBlank() && !uiState.isNameValid,
            supportingText = if (uiState.name.isNotBlank() && !uiState.isNameValid) {
                { Text("El nombre es requerido") }
            } else null,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words
            )
        )

        // Apellido
        RegisterTextField(
            label = "Apellido",
            value = uiState.lastName,
            onValueChange = onLastNameChange,
            isError = uiState.lastName.isNotBlank() && !uiState.isLastNameValid,
            supportingText = if (uiState.lastName.isNotBlank() && !uiState.isLastNameValid) {
                { Text("El apellido es requerido") }
            } else null,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words
            )
        )

        // Email
        RegisterTextField(
            label = "Email",
            value = uiState.email,
            onValueChange = onEmailChange,
            isError = uiState.email.isNotBlank() && !uiState.isEmailValid,
            supportingText = if (uiState.email.isNotBlank() && !uiState.isEmailValid) {
                { Text("Ingresa un email válido") }
            } else null,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            )
        )

        // Contraseña
        RegisterPasswordField(
            label = "Contraseña",
            value = uiState.password,
            onValueChange = onPasswordChange,
            isError = uiState.password.isNotBlank() && !uiState.isPasswordValid,
            supportingText = if (uiState.password.isNotBlank() && !uiState.isPasswordValid) {
                { Text("Mínimo 6 caracteres") }
            } else null
        )

        // Confirmar Contraseña
        RegisterPasswordField(
            label = "Confirmar Contraseña",
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            isError = uiState.confirmPassword.isNotBlank() && !uiState.doPasswordsMatch,
            supportingText = if (uiState.confirmPassword.isNotBlank() && !uiState.doPasswordsMatch) {
                { Text("Las contraseñas no coinciden") }
            } else null,
            containerColor = if (uiState.doPasswordsMatch && uiState.confirmPassword.isNotBlank()) {
                Color.Green.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.background
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val textColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = if (isError) errorColor else textColor
            )
        },
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedLabelColor = if (isError) errorColor else textColor,
            unfocusedLabelColor = if (isError) errorColor else textColor
        )
    )
}

@Composable
private fun RegisterPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    supportingText: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        )
    )
}

@Composable
private fun RegisterButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "REGISTRARSE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}