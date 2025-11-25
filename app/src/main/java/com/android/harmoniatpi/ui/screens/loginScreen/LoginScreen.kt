package com.android.harmoniatpi.ui.screens.loginScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.components.HoloTextField
import com.android.harmoniatpi.ui.components.InternetDisableScreen
import com.android.harmoniatpi.ui.components.LoginBackGroundHeader
import com.android.harmoniatpi.ui.screens.loginScreen.components.PreviewScreen
import com.android.harmoniatpi.ui.screens.loginScreen.viewModel.LoginScreenViewModel
import com.android.harmoniatpi.ui.screens.registerScreen.ScreenTitle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val username = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            navigateToHome()
            username.value = ""
            password.value = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("LOGIN_SCREEN")) {
        if (uiState.previewScreen) {
            PreviewScreen(goToLogin = { viewModel.navigateToLogin() })
        } else if (uiState.showNoInternetScreen) {
            InternetDisableScreen(
                colorText = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("InternetDisableScreen")
            ) {
                viewModel.checkInternetAvailable()
                if (!uiState.isInternetAvailable) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.login_screen_offlineMessage), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {

            Box(modifier = Modifier.fillMaxSize()) {

                LoginBackGroundHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.40f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Column(modifier = Modifier.weight(2f)) {
                        Box(modifier = Modifier.weight(0.1f)) {
                            ScreenTitle("Inicia Sesión")
                            uiState.errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.CenterStart).padding(top = 24.dp)
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(0.5f)) {
                            LoginForm(
                                username = username,
                                password = password,
                                isLoading = uiState.isLoading,
                                onLogin = { u, p -> viewModel.onLogin(u, p) },
                                onGoogleLogin = { idToken -> viewModel.onGoogleLogin(idToken) }
                            )
                        }
                        Box(modifier = Modifier.weight(0.1f)) {
                            NoAccountSection(onRegisterClick = navigateToRegister)
                        }


                    }
                }
            }
        }
    }
}


@Composable
private fun LoginForm(
    username: MutableState<String>,
    password: MutableState<String>,
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: (String) -> Unit
) {
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isFormValid = remember(username.value, password.value) {
        username.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }
    val credentialManager = remember { CredentialManager.create(context) }
    val googleSignInRequest = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
    }
    val credentialRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleSignInRequest)
            .build()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(0.3f)) {
            UsernameInput(username)
        }
        Box(modifier = Modifier.weight(0.4f)) {
            PasswordInput(password, passwordVisible)
        }
        Box(modifier = Modifier.weight(0.3f)) {
            LoginButton(
                label = stringResource(R.string.login_screen_EnterApp),
                enabled = isFormValid,
                isLoading = isLoading,
            ) {
                onLogin(username.value.trim(), password.value.trim())
                keyboardController?.hide()
            }
        }
        Box(modifier = Modifier.weight(0.3f)) {
            GoogleSignInButton(
                onClick = {
                    scope.launch {
                        try {
                            Log.d("GoogleLogin", "Iniciando flujo con Credential Manager")
                            val result = credentialManager.getCredential(
                                request = credentialRequest,
                                context = context
                            )

                            val credential = result.credential
                            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                try {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken

                                    Log.d("GoogleLogin", "Token obtenido exitosamente")
                                    onGoogleLogin(idToken)

                                } catch (e: Exception) {
                                    Log.e("GoogleLogin", "Error al extraer el token de los datos", e)
                                    Toast.makeText(context, "Error procesando credenciales de Google", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Log.e("GoogleLogin", "Tipo de credencial desconocido: ${credential.javaClass.name}")
                                Toast.makeText(context, "Error: Credencial no reconocida", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: GetCredentialException) {
                            Log.e("GoogleLogin", "Error al obtener credencial: ${e.message}", e)
                        } catch (e: Exception) {
                            Log.e("GoogleLogin", "Excepción inesperada: ${e.message}", e)
                        }
                    }
                }
            )
        }


    }
}


@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("GOOGLE_SIGNIN_BUTTON"),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Continuar con Google",
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun NoAccountSection(
    onRegisterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¿No tenés una cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.testTag("REGISTER_TEXT_BUTTON")
        ) {
            Text(
                text = "Registrarse",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


@Composable
private fun LoginButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(vertical = 8.dp)
            .testTag("LOGIN_BUTTON"),
    ) {

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun PasswordInput(
    state: MutableState<String>,
    visible: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    HoloTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = stringResource(R.string.login_screen_userPassword),
        placeholder = "Ingresa tu contraseña",
        leadingIcon = Icons.Default.Lock,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("PASSWORD_INPUT"),
        trailingIcon = if (visible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        onTrailingIconClick = { visible.value = !visible.value },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible.value) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
private fun UsernameInput(
    state: MutableState<String>,
    modifier: Modifier = Modifier
) {
    HoloTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = "Email",
        placeholder = "Ingresa tu usuario o email",
        leadingIcon = Icons.Default.Person,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("USERNAME_INPUT"),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        visualTransformation = VisualTransformation.None,
        isError = false
    )
}