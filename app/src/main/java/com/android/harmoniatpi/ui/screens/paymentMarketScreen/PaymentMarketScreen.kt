package com.android.harmoniatpi.ui.screens.paymentMarketScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.viewModel.PaymentMarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMarketScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentMarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pago Mercado Pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (uiState.loading) {
                CircularProgressIndicator()
            }

            Button(
                onClick = { viewModel.createPreference(100.0, "HoloJam Premium") },
                enabled = !uiState.loading && uiState.preference == null
            ) {
                Text("Generar Preference")
            }

            uiState.preference?.let { pref ->
                Text("Plan: ${pref.description}")
                Text("Monto mensual: $${pref.amount}")

                Button(
                    onClick = {
                        // Necesitas pasar el contexto al VM o manejar el intent aquí
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pref.preferenceId))
                        context.startActivity(intent)
                    },
                    enabled = !uiState.loading
                ) {
                    Text("Suscribirse con MercadoPago")
                }
            }

            uiState.paymentResult?.let { result ->
                Text(
                    text = "Resultado del pago: $result",
                    fontWeight = FontWeight.Bold,
                    color = when (result) {
                        PaymentResult.APPROVED -> MaterialTheme.colorScheme.primary
                        PaymentResult.REJECTED -> MaterialTheme.colorScheme.tertiary
                        PaymentResult.PENDING -> MaterialTheme.colorScheme.onError
                    }
                )
            }

            uiState.errorMessage?.let {
                Text(text = "Error: $it", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}
