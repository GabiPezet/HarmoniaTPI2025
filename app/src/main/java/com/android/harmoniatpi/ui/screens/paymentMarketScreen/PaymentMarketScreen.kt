package com.android.harmoniatpi.ui.screens.paymentMarketScreen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.payment.PaymentResult
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.components.MercadoPagoButton
import com.android.harmoniatpi.ui.screens.paymentMarketScreen.viewModel.PaymentMarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMarketScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentMarketViewModel = hiltViewModel()
) {

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
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "HoloJam Premium — $100 ARS",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            //BOTÓN REAL DE MERCADO PAGO
            MercadoPagoButton(
                onPaymentApproved = {
                    viewModel.onPaymentApproved()
                }
            )

        }
    }
}
