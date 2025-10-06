package com.android.harmoniatpi.ui.screens.songVersionsScreen

import android.view.RoundedCorner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme


// --- Modelos de Datos (Ejemplo) ---
data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val versionType: String, // "Versión Original", "Versión Derivada"
    val artistImageUrl: String?, // URL o placeholder
    val audioUrl: String, // Para reproducción
    val durationMillis: Long,
    val projectId: String? = null // Para "Abrir proyecto"
)

data class UserVersion(
    val id: String,
    val userName: String,
    val userImageUrl: String?,
    val songTitle: String, // Podría ser el mismo o una variación
    val audioUrl: String,
    val projectId: String?
)

// --- Composable Principal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongVersionsScreen(
    // Deberías pasar un ViewModel o lambdas para manejar eventos y datos
    originalSong: Song,
    derivedVersions: List<UserVersion>,
    onPlayOriginal: (Song) -> Unit,
    onOpenOriginalProject: (Song) -> Unit,
    onPlayDerived: (UserVersion) -> Unit,
    onOpenDerivedProject: (UserVersion) -> Unit,
    onNavigateBack: () -> Unit, // Para el botón de menú/atrás del TopAppBar
    onSearchClick: () -> Unit // Para el botón de búsqueda del TopAppBar
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("HARMONIA", fontWeight = FontWeight.Bold) // O tu logo
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menú"
                        )
                    }
                },

                )
        },

        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplicar padding del Scaffold
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SongHeader(songTitle = originalSong.title, artistName = originalSong.artistName)
                Spacer(modifier = Modifier.height(24.dp))
                OriginalSongPlayer(
                    song = originalSong,
                    onPlayClick = { onPlayOriginal(originalSong) },
                    onOpenProjectClick = { onOpenOriginalProject(originalSong) },
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "VERSIONES DERIVADAS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(derivedVersions) { version ->
                DerivedVersionItem(
                    version = version,
                    onPlayClick = { onPlayDerived(version) },
                    onOpenProjectClick = { onOpenDerivedProject(version) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp)) // Espacio al final de la lista
            }
        }
    }
}

@Composable
fun SongHeader(songTitle: String, artistName: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.image_song_default),
            contentDescription = "Imagen de song",
            modifier = Modifier
                .size(80.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = songTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = artistName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )

        }
    }
}

@Composable
fun OriginalSongPlayer(
    song: Song,
    onPlayClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Deberías añadir el progreso actual y un callback para el cambio del slider
    currentProgress: Float = 0.9f, // Ejemplo, debería venir del estado
    onSliderValueChange: (Float) -> Unit = {}// Ejemplo
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.DarkGray
                )
                Text(
                    text = song.versionType,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Reemplaza con Coil o Glide para cargar imágenes desde URL
                // Reemplazar con Coil o Glide para cargar imágenes desde URL y borrar background
                Image(
                    painter = painterResource(id = R.drawable.outline_account_circle_24),
                    contentDescription = "Imagen de artista: ${song.artistName}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color = Color.LightGray)
                        .border(BorderStroke(2.dp, Color.Black), CircleShape),
                    contentScale = ContentScale.Crop
                )
                TextButton(onClick = { if (song.projectId != null) onOpenProjectClick() }) {
                    Text(text = "Abrir proyecto", color = Color.DarkGray)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularPlay(onPlayClick, modifier = Modifier.size(50.dp).padding(8.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Slider(
                    value = currentProgress, // Debería ser (currentTimeMillis / song.durationMillis).toFloat()
                    onValueChange = onSliderValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    // colors = SliderDefaults.colors(...) // Personaliza colores si es necesario
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Formatea estos tiempos correctamente (ej. 0:00)
                    Text(
                        text = formatMillisToTimeString((currentProgress * song.durationMillis).toLong()),
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = formatMillisToTimeString(song.durationMillis),
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

        }
    }
}

@Composable
private fun CircularPlay(
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Color.Black,
    iconColor: Color = Color.White
) {
    IconButton(
        onClick = onPlayClick,
        modifier = modifier
            .background(color = background, shape = CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Reproducir/Pausar",
            modifier = Modifier.fillMaxSize(),
            tint = iconColor
        )
    }
}

@Composable
fun DerivedVersionItem(
    version: UserVersion,
    onPlayClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Podrías hacer algo al clickar el item, como navegar a detalles */ }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reemplazar con Coil o Glide para cargar imágenes desde URL y borrar background
            Image(
                painter = painterResource(id = R.drawable.outline_account_circle_24),
                contentDescription = "Artista: ${version.userName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = version.userName, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Abrir proyecto",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.clickable { if (version.projectId != null) onOpenProjectClick() }
                )
            }
            CircularPlay(onPlayClick, modifier = Modifier.size(30.dp))
        }
    }
}

// --- Función Helper para formatear tiempo (ejemplo básico) ---
fun formatMillisToTimeString(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}


// --- Preview ---
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SongVersionsScreenPreview() {
    val sampleOriginalSong = Song(
        id = "1",
        title = "El paso del tiempo",
        artistName = "Jane Smith",
        versionType = "Versión Original",
        artistImageUrl = null,
        audioUrl = "",
        durationMillis = (8 * 60 + 36) * 1000L, // 8:36
        projectId = "proj1"
    )
    val sampleDerivedVersions = listOf(
        UserVersion("v1", "Brian Perez", null, "El paso del tiempo (Brian's cover)", "", "projV1"),
        UserVersion(
            "v2",
            "Adelaida Rojas",
            null,
            "El paso del tiempo (Adelaida's version)",
            "",
            "projV2"
        ),
        UserVersion("v3", "Nico Rizzo", null, "El paso del tiempo - Remix", "", "projV3"),
        UserVersion("v4", "Charly Giménez", null, "El paso del tiempo Acústico", "", null),
        UserVersion("v5", "Sebastián Prato", null, "Mi versión de El paso...", "", "projV5")
    )

    // Necesitas un tema Material3 wrapper para la preview
    HarmoniaTPITheme(false) {
        SongVersionsScreen(
            originalSong = sampleOriginalSong,
            derivedVersions = sampleDerivedVersions,
            onPlayOriginal = {},
            onOpenOriginalProject = {},
            onPlayDerived = {},
            onOpenDerivedProject = {},
            onNavigateBack = {},
            onSearchClick = {}
        )
    }
}