package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompactSymmetricButtons(
    modifier: Modifier = Modifier,
    leftLabel: String = "Seguidores",
    rightLabel: String = "Compartir perfil",
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val buttonModifier = Modifier
            .height(35.dp)
            .weight(1f) // AHORA: Usar weight para que ambos botones ocupen el mismo espacio

        OutlinedButton(
            onClick = onLeftClick,
            modifier = buttonModifier,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary // AHORA
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // AHORA: El borde también usa el color del tema
        ) {
            Text(text = leftLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedButton(
            onClick = onRightClick,
            modifier = buttonModifier,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary // AHORA
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // AHORA
        ) {
            Text(text = rightLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}