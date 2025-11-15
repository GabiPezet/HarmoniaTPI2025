package com.android.harmoniatpi.ui.screens.paymentMarketScreen.components

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun MercadoPagoButton(
    onPaymentApproved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            scope.launch {
                loading = true
                val url = createPreference()
                loading = false

                if (url != null) {
                    CustomTabsIntent.Builder().build().launchUrl(context, android.net.Uri.parse(url))
                }
            }
        }
    ) {
        Text(if (loading) "Cargando..." else "Pagar $100 ARS")
    }
}