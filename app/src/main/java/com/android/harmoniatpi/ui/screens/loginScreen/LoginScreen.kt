package com.android.harmoniatpi.ui.screens.loginScreen

import android.Manifest.permission
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import androidx.core.app.ActivityCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.data.local.ext.findActivity
import com.android.harmoniatpi.ui.components.HarmoniaTextField
import com.android.harmoniatpi.ui.components.LoginBackGroundHeader
import com.android.harmoniatpi.ui.screens.loginScreen.components.PreviewScreen
import com.android.harmoniatpi.ui.screens.loginScreen.util.hasPermissions
import com.android.harmoniatpi.ui.screens.loginScreen.util.showPermissionsDeniedMessage
import com.android.harmoniatpi.ui.screens.loginScreen.viewModel.LoginScreenViewModel
import com.android.harmoniatpi.ui.screens.registerScreen.ScreenTitle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val username = rememberSaveable { mutableStateOf("pepeArgento@gmail.com") }
    val password = rememberSaveable { mutableStateOf("123456") }
    val permissions = buildList {
        add(permission.RECORD_AUDIO)
        add(permission.CAMERA)
        add(permission.ACCESS_FINE_LOCATION)
        add(permission.CALL_PHONE)
        add(permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(permission.POST_NOTIFICATIONS)
            add(permission.READ_MEDIA_IMAGES)
            add(permission.READ_MEDIA_VIDEO)
            add(permission.READ_MEDIA_AUDIO)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(permission.READ_EXTERNAL_STORAGE)
            add(permission.WRITE_EXTERNAL_STORAGE)
        } else {
            add(permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allGranted = permissionsResult.values.all { it }
        if (!allGranted) {
            val showSettings = permissionsResult.any { (perm, granted) ->
                !granted && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context.findActivity(),
                    perm
                )
            }
            if (showSettings) {
                showPermissionsDeniedMessage(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!context.hasPermissions(permissions)) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            navigateToHome()
            username.value = ""
            password.value = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.previewScreen) {
            PreviewScreen(goToLogin = { viewModel.navigateToLogin() })
        } else {

            Box(modifier = Modifier.fillMaxSize()) {
                // Imagen de fondo que cubre toda la pantalla
                LoginBackGroundHeader()

                // Column con fondo semi-transparente o gradiente para mejor legibilidad
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Column(modifier = Modifier.weight(2f)) {
                        Box(modifier = Modifier.weight(0.1f)) {
                            ScreenTitle("Inicia Sesión")
                        }
                        Box(modifier = Modifier.weight(0.6f)) {
                            LoginForm(
                                username = username,
                                password = password,
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
                enabled = isFormValid
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
                            Log.d("GoogleLogin", "🚀 Iniciando flujo con Credential Manager...")
                            val result = credentialManager.getCredential(
                                request = credentialRequest,
                                context = context
                            )

                            val credential = result.credential
                            if (credential is GoogleIdTokenCredential) {
                                val idToken = credential.idToken
                                if (idToken.isNotBlank()) {
                                    onGoogleLogin(idToken)
                                } else {
                                    Log.e("GoogleLogin", "idToken vacío o nulo")
                                }
                            } else {
                                Log.e("GoogleLogin", "❌ Credencial no es GoogleIdTokenCredential")
                            }
                        } catch (e: GetCredentialException) {
                            Log.e("GoogleLogin", "❌ Error al obtener credencial: ${e.message}", e)
                        } catch (e: Exception) {
                            Log.e("GoogleLogin", "❌ Excepción inesperada: ${e.message}", e)
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
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
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        )

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.testTag("REGISTER_TEXT_BUTTON")
        ) {
            Text(
                text = "Registrarse",
                color = MaterialTheme.colorScheme.primary,
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
    onClick: () -> Unit
) {
    val containerColor =
        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
            alpha = 0.6f
        )
    val contentColor =
        if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(vertical = 8.dp)
            .testTag("LOGIN_BUTTON"),
        colors = ButtonColors(
            contentColor = contentColor,
            containerColor = containerColor,
            disabledContentColor = contentColor,
            disabledContainerColor = containerColor
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun PasswordInput(
    state: MutableState<String>,
    visible: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    HarmoniaTextField(
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
    HarmoniaTextField(
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