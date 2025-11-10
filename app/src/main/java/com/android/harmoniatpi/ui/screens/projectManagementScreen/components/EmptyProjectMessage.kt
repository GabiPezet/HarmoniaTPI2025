package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyProjectMessage(modifier: Modifier = Modifier) {
    // Usamos un mapa para definir el contenido del ícono en línea
    val inlineContentMap = mapOf(
        "add_icon" to InlineTextContent(
            Placeholder(
                width = 24.sp,
                height = 24.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )

    // Creamos el texto anotado
    val annotatedText = buildAnnotatedString {
        append("Presione ")
        // Adjuntamos el ícono en línea usando su ID
        appendInlineContent("add_icon", "[icono agregar]")
        append(" para agregar una nueva pista para ")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("grabar, insertar un archivo")
        }
        append(" o buscar en la biblioteca de sonidos.")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = annotatedText,
                inlineContent = inlineContentMap,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                lineHeight = 28.sp
            )
        }
    }
}
