package com.android.harmoniatpi.ui.screens.songVersionsScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
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
    onNavigateBack: () -> Unit,
) {
    var sliderProgress by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("HARMONIA", fontWeight = FontWeight.Bold)
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SongHeader(songTitle = originalSong.title, artistName = originalSong.artistName)
                Spacer(modifier = Modifier.height(16.dp))
                OriginalSongPlayer(
                    song = originalSong,
                    onPlayClick = { onPlayOriginal(originalSong) },
                    onOpenProjectClick = { onOpenOriginalProject(originalSong) },
                    currentProgress = sliderProgress,
                    onSliderValueChange = { newProgress ->
                        sliderProgress = newProgress
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "VERSIONES DERIVADAS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(derivedVersions) { version ->
                DerivedVersionItem(
                    version = version,
                    onPlayClick = { onPlayDerived(version) },
                    onOpenProjectClick = { onOpenDerivedProject(version) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalSongPlayer(
    song: Song,
    onPlayClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentProgress: Float,
    onSliderValueChange: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = song.versionType,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Reemplazar con Coil o Glide para cargar imágenes desde URL y borrar background
                Image(
                    painter = painterResource(id = R.drawable.outline_account_circle_24),
                    contentDescription = "Imagen de artista: ${song.artistName}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), CircleShape),
                    contentScale = ContentScale.Crop
                )
                TextButton(onClick = { if (song.projectId != null) onOpenProjectClick() }) {
                    Text(
                        text = "Abrir proyecto",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 0.dp, bottom = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {

            CircularPlay(
                onPlay = {/*llamar vm.play()*/ },
                onPause = {/*llamar vm.pause()*/ },
                isPlaying = false /*pasar el state*/,
                modifier = Modifier
                    .size(48.dp),
                background = MaterialTheme.colorScheme.secondary,
                iconColor = MaterialTheme.colorScheme.onSecondary
            )


            CustomPlayerControls(
                song = song,
                currentProgress = currentProgress,
                onSliderValueChange = onSliderValueChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPlayerControls(
    song: Song,
    currentProgress: Float,
    onSliderValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy((-12).dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val sliderColors = SliderDefaults.colors(

            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            thumbColor = MaterialTheme.colorScheme.primary
        )

        Slider(
            value = currentProgress,
            onValueChange = onSliderValueChange,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            thumb = {
                Icon(
                    painter = painterResource(R.drawable.ic_harmonyicon),
                    contentDescription = "Slider Thumb",
                    modifier = Modifier
                        // 1. Aumentamos el tamaño del thumb para que sea más prominente
                        .size(20.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(
                            border = BorderStroke(1.dp, Color.Black),
                            shape = CircleShape
                        ),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },

            track = { sliderState ->
                CustomTrack(
                    sliderState = sliderState,
                    trackHeight = 4.dp,
                    activeTrackColor = sliderColors.activeTrackColor,
                    inactiveTrackColor = sliderColors.inactiveTrackColor
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMillisToTimeString((currentProgress * song.durationMillis).toLong()),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                // Simulo el tiempo restante como en tu imagen
                text = formatMillisToTimeString(song.durationMillis - (currentProgress * song.durationMillis).toLong()),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun CircularPlay(
    onPlay: () -> Unit,
    onPause: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary
) {

    IconButton(
        onClick = (if (isPlaying) onPause else onPlay),
        modifier = modifier
            .background(color = background, shape = CircleShape)
    ) {
        Icon(
            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pausa" else "Play",
            modifier = Modifier.fillMaxSize(),
            tint = iconColor
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTrack(
    sliderState: SliderState,
    trackHeight: Dp,
    activeTrackColor: Color,
    inactiveTrackColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
    ) {
        val sliderStart = 0f
        val sliderEnd = size.width
        val trackYCenter = center.y

        // Calcula la fracción manualmente usando las propiedades públicas
        val valueRange = sliderState.valueRange.endInclusive - sliderState.valueRange.start
        val valueFraction = if (valueRange == 0f) 0f else {
            (sliderState.value - sliderState.valueRange.start) / valueRange
        }

        // Usa la nueva fracción para calcular la posición del pulgar
        val thumbPx = valueFraction * sliderEnd

        // Línea inactiva
        drawLine(
            color = inactiveTrackColor,
            start = Offset(sliderStart, trackYCenter),
            end = Offset(sliderEnd, trackYCenter),
            strokeWidth = trackHeight.toPx(),
            cap = StrokeCap.Round
        )

        // Línea activa
        drawLine(
            color = activeTrackColor,
            start = Offset(sliderStart, trackYCenter),
            end = Offset(thumbPx, trackYCenter),
            strokeWidth = trackHeight.toPx(),
            cap = StrokeCap.Round
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //"Reemplazar con Coil o Glide para cargar imágenes desde URL y borrar background")
            Image(
                painter = painterResource(id = R.drawable.outline_account_circle_24),
                contentDescription = "Artista: ${version.userName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape),

                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                TextButton(
                    onClick = { TODO("ir a perfil del artista seleccionado") },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = version.userName,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            //Habilitar para el siguiente mvp si es necesario
            /*TextButton(
                onClick = { if (version.projectId != null) onOpenProjectClick() },
            ) {
                Text(
                    text = "Abrir proyecto",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            */
            CircularPlay(
                onPlay = {/*llamar vm.play()*/ },
                onPause = {/*llamar vm.pause()*/ },
                isPlaying = false /*pasar el state*/,
                modifier = Modifier.size(30.dp),
                background = MaterialTheme.colorScheme.tertiary,
                iconColor = MaterialTheme.colorScheme.onTertiary
            )
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
        UserVersion(
            "v1",
            "Brian Perez",
            null,
            "El paso del tiempo (Brian's cover)",
            "",
            "projV1"
        ),
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

    HarmoniaTPITheme(true) {
        SongVersionsScreen(
            originalSong = sampleOriginalSong,
            derivedVersions = sampleDerivedVersions,
            onPlayOriginal = {},
            onOpenOriginalProject = {},
            onPlayDerived = {},
            onOpenDerivedProject = {},
            onNavigateBack = {},
        )
    }
}