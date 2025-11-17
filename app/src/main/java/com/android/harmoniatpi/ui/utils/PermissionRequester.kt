package com.android.harmoniatpi.ui.utils

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import com.android.harmoniatpi.ui.screens.loginScreen.util.openAppSettings

@Composable
fun PermissionRequester(
    permission: String,
    @StringRes rationaleRes: Int,
    @StringRes permanentlyDeniedRes: Int,
    showPermanentlyDeclinedDialog: Boolean = true,
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onDialogDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity

    var showRationale by remember { mutableStateOf(false) }
    var showPermanentlyDenied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        } else {
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission
            )

            if (shouldShow) {
                showRationale = true
            } else {
                // Permanently denied (“No volver a preguntar”)
                showPermanentlyDenied = true
                onDenied()
            }
        }
    }

    fun requestPermission() {
        launcher.launch(permission)
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onDialogDismiss()
            },
            title = { Text("Permiso necesario") },
            text = { Text(stringResource(id = rationaleRes)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    requestPermission()
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onDialogDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPermanentlyDenied && showPermanentlyDeclinedDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermanentlyDenied = false
                onDialogDismiss()
            },
            title = { Text("Permiso bloqueado") },
            text = { Text(stringResource(id = permanentlyDeniedRes)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermanentlyDenied = false
                    openAppSettings(context)
                    onDialogDismiss()
                }) {
                    Text("Abrir configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermanentlyDenied = false
                    onDialogDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Se pide el permiso cuando aparece el composable
    LaunchedEffect(Unit) {
        requestPermission()
    }
}

