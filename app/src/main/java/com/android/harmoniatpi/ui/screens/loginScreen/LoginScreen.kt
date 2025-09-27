package com.android.harmoniatpi.ui.screens.loginScreen

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.data.local.ext.findActivity
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.HarmoniaTextField
import com.android.harmoniatpi.ui.components.InternetDisableScreen
import com.android.harmoniatpi.ui.components.LoginBackGroundHeader
import com.android.harmoniatpi.ui.screens.loginScreen.components.PreviewScreen
import com.android.harmoniatpi.ui.screens.loginScreen.model.LoginUiState
import com.android.harmoniatpi.ui.screens.loginScreen.util.hasPermissions
import com.android.harmoniatpi.ui.screens.loginScreen.util.showPermissionsDeniedMessage
import com.android.harmoniatpi.ui.screens.loginScreen.viewModel.LoginScreenViewModel
import com.android.harmoniatpi.ui.screens.registerScreen.ScreenTitle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val username = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }

    val permissions = listOf(
        permission.RECORD_AUDIO,
        permission.CAMERA,
        permission.ACCESS_FINE_LOCATION,
        permission.CALL_PHONE,
        permission.READ_PHONE_STATE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permission.POST_NOTIFICATIONS else null
    ).filterNotNull()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (!results.values.all { it }) showPermissionsDeniedMessage(context)
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.previewScreen) {

            PreviewScreen(goToLogin = { viewModel.navigateToLogin() })
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                LoginBackGroundHeader()

                ScreenTitle("Inicia Sesión", modifier = Modifier.padding(start = 24.dp, top = 2.dp, end = 8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                LoginForm(
                    username = username,
                    password = password,
                    onLogin = { u, p -> viewModel.onLogin(u, p) },
                    onGoogleLogin = { idToken -> viewModel.onGoogleLogin(idToken) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                NoAccountSection(onRegisterClick = navigateToRegister)

                Spacer(modifier = Modifier.navigationBarsPadding())
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
    val context = LocalContext.current
    val activity = context.findActivity()


    val isFormValid = remember(username.value, password.value) {
        username.value.isNotBlank() && password.value.isNotBlank()
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { onGoogleLogin(it) }
            } catch (e: ApiException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        UsernameInput(username)
        PasswordInput(password, passwordVisible)


        LoginButton(
            label = stringResource(R.string.login_screen_EnterApp),
            enabled = isFormValid
        ) {
            onLogin(username.value.trim(), password.value.trim())
            keyboardController?.hide()
        }

        // Google Sign-In
        GoogleSignInButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(activity, gso)
                launcher.launch(googleSignInClient.signInIntent)
            }
        )
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
            contentColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.onPrimary
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
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
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