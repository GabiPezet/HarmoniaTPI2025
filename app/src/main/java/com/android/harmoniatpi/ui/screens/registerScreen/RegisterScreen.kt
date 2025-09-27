package com.android.harmoniatpi.ui.screens.registerScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.ui.components.BackGroundHeader
import com.android.harmoniatpi.ui.components.HarmoniaTextField
import com.android.harmoniatpi.ui.screens.registerScreen.viewmodel.RegisterScreenViewModel


@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    viewModel: RegisterScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        BackGroundHeader()

        // titulo
        ScreenTitle("Únete", modifier = Modifier.padding(start = 24.dp, top = 2.dp, end = 8.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            HarmoniaTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Nombre",
                placeholder = "Ingresa tu nombre",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
            )

            HarmoniaTextField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = "Apellido",
                placeholder = "Ingresa tu apellido",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
            )


            HarmoniaTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "ejemplo@gmail.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                isError = uiState.email.isNotBlank() && !uiState.isEmailValid,
                supportingText = if (uiState.email.isNotBlank() && !uiState.isEmailValid) "Ingresa un email válido" else null
            )

            // pass
            var passwordVisible by remember { mutableStateOf(false) }
            HarmoniaTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                isError = uiState.password.isNotBlank() && !uiState.isPasswordValid,
                supportingText = if (uiState.password.isNotBlank() && !uiState.isPasswordValid) "Mínimo 6 caracteres" else null
            )

            // confirmar
            var confirmVisible by remember { mutableStateOf(false) }
            HarmoniaTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirma tu contraseña",
                placeholder = "Ingresa tu contraseña",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { confirmVisible = !confirmVisible },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                isError = uiState.confirmPassword.isNotBlank() && !uiState.doPasswordsMatch,
                supportingText = if (uiState.confirmPassword.isNotBlank() && !uiState.doPasswordsMatch) "Las contraseñas no coinciden" else null
            )
        }

        Spacer(modifier = Modifier.size(16.dp))


        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.registerUser(
                    onSuccess = {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onBackToLogin()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = uiState.isFormValid && !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (uiState.registerEnabled) "REGISTRARCE" else "BIENVENIDO A HARMONIA",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes una cuenta? ",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
            )
            TextButton(onClick = onBackToLogin) {
                Text(
                    "Inicia sesión",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(2.dp))
        )
    }
}


