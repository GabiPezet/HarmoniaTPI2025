package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.android.harmoniatpi.R

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun ShowExitConfirmationPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ShowConfirmationDialog(
                show = true,
                onDismiss = {},
                onConfirm = {},
                title = "¿Estás seguro?",
                message = "Vas a perder los cambios si salís ahora."
            )

        }
    }
}

@Composable
fun ShowConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.show_confirm_dialog_confirm),
    dismissText: String = stringResource(R.string.show_confirm_dialog_cancel),
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true
) {

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            properties = DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside,
            ),
            confirmButton = {

                Button(
                    onClick = onConfirm,
                    shape = MaterialTheme.shapes.small,

                    ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

            },
            dismissButton = {

                Button(
                    onClick = onDismiss,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            title = {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,

                    )

            },
            text = {
                Text(
                    text = message,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )

            }
        )
    }
}
