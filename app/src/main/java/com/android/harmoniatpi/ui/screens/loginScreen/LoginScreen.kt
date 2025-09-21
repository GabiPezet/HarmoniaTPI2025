package com.android.harmoniatpi.ui.screens.loginScreen

import android.Manifest.permission
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.data.local.ext.findActivity
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.InternetDisableScreen
import com.android.harmoniatpi.ui.components.isScreenInPortrait
import com.android.harmoniatpi.ui.screens.loginScreen.model.LoginUiState
import com.android.harmoniatpi.ui.screens.loginScreen.util.hasPermissions
import com.android.harmoniatpi.ui.screens.loginScreen.util.showPermissionsDeniedMessage
import com.android.harmoniatpi.ui.screens.loginScreen.viewModel.LoginScreenViewModel


@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPortrait = isScreenInPortrait()
    val uiState by viewModel.uiState.collectAsState()
    val username = rememberSaveable { mutableStateOf("pepeArgento@gmail.com") }
    val password = rememberSaveable { mutableStateOf("123456") }
    val hasNavigated = remember { mutableStateOf(false) }
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
        if (uiState.loginSuccess && !hasNavigated.value) {
            hasNavigated.value = true
            navigateToHome()
            username.value = ""
            password.value = ""
        }
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onErrorShown()
        }

    }

    if (uiState.isLoading) {
        CircularProgressBar(stringResource(R.string.circular_progress_bar_loadingMessage))
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("login_screen")
        ) {
            if (isPortrait) {
                LoginPortraitScreen(
                    uiState,
                    innerPadding,
                    username,
                    password,
                    context,
                    permissions,
                    viewModel,
                    navigateToRegister
                )
            } else {
                LoginLandscapeScreen(
                    uiState,
                    innerPadding,
                    username,
                    password,
                    context,
                    permissions,
                    viewModel,
                    navigateToRegister
                )
            }

        }

    }


}

@Composable
private fun LoginPortraitScreen(
    uiState: LoginUiState,
    innerPadding: PaddingValues,
    username: MutableState<String>,
    password: MutableState<String>,
    context: Context,
    permissions: List<String>,
    viewModel: LoginScreenViewModel,
    navigateToRegister: () -> Unit
) {
    when {
        !uiState.isInitialized || uiState.isLoading -> {
            CircularProgressBar(stringResource(R.string.circular_progress_bar_loadingMessage))
        }

        uiState.showLoginScreen -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.iv_harmonia_logo),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(
                                R.string.login_screen_version_app,
                                uiState.versionName
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    UserLogin(
                        username = username,
                        password = password,
                        onLogin = { username, password ->
                            if (context.hasPermissions(permissions)) {
                                viewModel.checkInternetAvailable()
                                if (uiState.isInternetAvailable) {
                                    viewModel.onLogin(username, password)
                                }
                            } else {
                                showPermissionsDeniedMessage(context)
                            }
                        },
                        navigateToRegister = navigateToRegister
                    )
                }
            }

        }

        else -> {
            InternetDisableScreen(
                colorText = MaterialTheme.colorScheme.secondary,
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
        }
    }
}

@Composable
private fun LoginLandscapeScreen(
    uiState: LoginUiState,
    innerPadding: PaddingValues,
    username: MutableState<String>,
    password: MutableState<String>,
    context: Context,
    permissions: List<String>,
    viewModel: LoginScreenViewModel,
    navigateToRegister: () -> Unit
) {
    when {
        !uiState.isInitialized || uiState.isLoading -> {
            CircularProgressBar(stringResource(R.string.circular_progress_bar_loadingMessage))
        }

        uiState.showLoginScreen -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .height(50.dp)
                                .width(200.dp),
                            painter = painterResource(id = R.drawable.iv_harmonia_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Inside
                        )
                        Text(
                            text = stringResource(
                                R.string.login_screen_version_app,
                                uiState.versionName
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp
                        )
                    }

                    UserLogin(
                        username = username,
                        password = password,
                        onLogin = { username, password ->
                            if (context.hasPermissions(permissions)) {
                                viewModel.checkInternetAvailable()
                                if (uiState.isInternetAvailable) {
                                    viewModel.onLogin(username, password)
                                }
                            } else {
                                showPermissionsDeniedMessage(context)
                            }
                        },
                        navigateToRegister = navigateToRegister
                    )
                }
            }

        }

        else -> {
            InternetDisableScreen(
                colorText = MaterialTheme.colorScheme.secondary,
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
        }
    }
}

@Composable
private fun UserLogin(
    username: MutableState<String>,
    password: MutableState<String>,
    onLogin: (String, String) -> Unit,
    navigateToRegister: () -> Unit
) {
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isFormValid = remember(username.value, password.value) {
        username.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
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
        RegisterButton(
            label = stringResource(R.string.registrarce),
            onClick = { navigateToRegister() })

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
            alpha = 0.30f
        )
    val contentColor =
        if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(
            alpha = 0.40f
        )
    Button(
        onClick = onClick,
        shape = CircleShape,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("LOGIN_BUTTON"),
        colors = ButtonColors(
            contentColor = contentColor,
            containerColor = containerColor,
            disabledContentColor = contentColor,
            disabledContainerColor = containerColor
        )
    ) {
        Text(label, modifier = Modifier.padding(5.dp))
    }
}

@Composable
private fun RegisterButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        enabled = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("REGISTER_BUTTON"),
        colors = ButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(label, modifier = Modifier.padding(5.dp))
    }
}

@Composable
private fun PasswordInput(
    state: MutableState<String>,
    visible: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val transformation =
        if (visible.value) VisualTransformation.None else PasswordVisualTransformation()

    TextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = { Text(stringResource(R.string.login_screen_userPassword)) },
        singleLine = true,
        visualTransformation = transformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val icon = if (visible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { visible.value = !visible.value }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("PASSWORD_INPUT"),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.DarkGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black
        )
    )
}

@Composable
private fun UsernameInput(
    state: MutableState<String>,
    modifier: Modifier = Modifier
) {
    TextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = {
            Text(
                stringResource(R.string.login_screen_userName),
                style = MaterialTheme.typography.labelMedium
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("USERNAME_INPUT"),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.DarkGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black
        )
    )
}