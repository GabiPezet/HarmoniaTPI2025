package com.android.harmoniatpi.ui.screens.registerScreen

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Header()

        // titulo
        ScreenTitle("Únete", modifier = Modifier.padding(start = 24.dp, top = 2.dp, end = 8.dp))

        Spacer(modifier = Modifier.height(24.dp))


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
                containerColor = Color(0xFFFFC107), // Color amarillo más vibrante
                disabledContainerColor = Color(0xFFFFC107).copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Text("BIENVENIDO A HARMONIA", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya tienes una cuenta? ", color = Color.Gray)
            TextButton(onClick = onBackToLogin) {
                Text("Inicia sesión", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

//componentes para las pantallas

@Composable
fun Header() {
    Image(
        painter = painterResource(id = R.drawable.ic_register_header_background),
        contentDescription = "Header Background",
        modifier = Modifier.fillMaxWidth(),

        contentScale = ContentScale.FillWidth
    )
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
                .background(Color(0xFFFFC107), shape = RoundedCornerShape(2.dp))
        )
    }
}


@Composable
fun HarmoniaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(imageVector = leadingIcon, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                            Icon(imageVector = trailingIcon, contentDescription = null)
                        }
                    }
                }
            }
        )
        Divider(color = if (isError) MaterialTheme.colorScheme.error else Color.LightGray, thickness = 1.dp)
        if (isError && supportingText != null) {
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}