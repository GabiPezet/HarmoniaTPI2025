package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón inteligente que adapta su comportamiento y apariencia según el estado de suscripción del usuario.
 *
 * - Si [isPremium] es `true`: Se comporta como un botón normal y ejecuta [onClick].
 * - Si [isPremium] es `false`: Muestra un candado y ejecuta [onShowUpsell] (generalmente abrir un diálogo de compra).
 *
 * @param text Texto a mostrar en el botón.
 * @param isPremium Estado de la suscripción del usuario.
 * @param onClick Acción a ejecutar si el usuario tiene acceso.
 * @param onShowUpsell Acción a ejecutar si el usuario NO tiene acceso (ej. mostrar diálogo de pago).
 * @param modifier Modificador de Compose.
 * @param enabled Si el botón está habilitado (independientemente de si es premium o no).
 */
@Composable
fun PremiumAwareButton(
    text: String,
    isPremium: Boolean,
    onClick: () -> Unit,
    onShowUpsell: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Definimos el color: Opcional, podrías usar un dorado para usuarios Premium o mantener el primario.
    val containerColor = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Button(
        onClick = {
            if (isPremium) {
                onClick()
            } else {
                onShowUpsell()
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        if (!isPremium) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Función Bloqueada",
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
        } else {
            // Opcional: Icono de estrella o nada para usuarios premium
             /* Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp)) */
        }
        Text(text)
    }
}