package com.android.harmoniatpi.ui.screens.songVersionsScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.DerivedVersion
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.Song
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel.SongVersionsViewModel


@Composable
fun SongVersionsScreen(
    viewModel: SongVersionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SongVersionsContent(
        uiState = uiState,
        onPlayOriginal = viewModel::onPlayPauseOriginal,
        onOpenOriginalProject = viewModel::onOpenProject,
        onPlayDerived = viewModel::onPlayPauseDerived,
        onSliderChange = viewModel::onSliderChange,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongVersionsContent(
    uiState: SongVersionsUiState,
    onPlayOriginal: () -> Unit,
    onOpenOriginalProject: (String?) -> Unit,
    onPlayDerived: (String) -> Unit,
    onSliderChange: (Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HARMONIA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.originalSong != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val originalSong = uiState.originalSong

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SongHeader(songTitle = originalSong.title, artistName = originalSong.artistName)
                    Spacer(modifier = Modifier.height(16.dp))
                    OriginalSongPlayer(
                        song = originalSong,
                        isPlaying = uiState.isOriginalPlaying,
                        currentProgress = uiState.currentPlaybackProgress,
                        onPlayClick = onPlayOriginal,
                        onOpenProjectClick = { onOpenOriginalProject(originalSong.projectId) },
                        onSliderValueChange = onSliderChange
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

                items(uiState.derivedVersions) { version ->
                    DerivedVersionItem(
                        version = version,
                        isPlaying = uiState.playingDerivedVersionId == version.id,
                        onPlayClick = { onPlayDerived(version.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalSongPlayer(
    song: Song,
    isPlaying: Boolean,
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
                onPlay = onPlayClick,
                onPause = onPlayClick,
                isPlaying = isPlaying,
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
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
                        .border(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = CircleShape
                        ),
                    tint = MaterialTheme.colorScheme.primary,
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
    version: DerivedVersion,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
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
                onPlay = onPlayClick,
                onPause = onPlayClick,
                isPlaying = isPlaying,
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

@Preview(showBackground = true, showSystemUi = false, name = "Light Mode")
@Composable
fun SongVersionsScreenPreview() {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }

    val sampleDerivedVersions = listOf(
        DerivedVersion("v1", "Lucas Martínez", "url/v1", "projectA"),
        DerivedVersion("v2", "Sofía González", "url/v2", "projectB"),
        DerivedVersion("v3", "Mateo López", "url/v3", "projectC"),
        DerivedVersion("v4", "Valentina Torres", "url/v4", "projectD"),
        DerivedVersion("v5", "Julián Fernández", "url/v5", "projectE"),
        DerivedVersion("v6", "Emma Herrera", "url/v6", "projectF"),
        DerivedVersion("v7", "Tomás Ramírez", "url/v7", "projectG"),
        DerivedVersion("v8", "Camila Díaz", "url/v8", "projectH"),
        DerivedVersion("v9", "Nicolás Castro", "url/v9", "projectI"),
        DerivedVersion("v10", "Martina Vidal", "url/v10", "projectJ"),
        DerivedVersion("v11", "Samuel Reyes", "url/v11", "projectK"),
        DerivedVersion("v12", "Isabella Cruz", "url/v12", "projectL")
    )

    val previewState = SongVersionsUiState(
        originalSong = Song(
            id = "1",
            title = "El paso del tiempo",
            artistName = "Jane Smith",
            versionType = "Original",
            projectId = "proj1",
            durationMillis = (8 * 60 + 36) * 1000L,
            artistImageUrl = "",
            audioUrl = ""

        ),
        derivedVersions = sampleDerivedVersions,
        currentPlaybackProgress = currentProgress,
        isOriginalPlaying = isPlaying,
        isLoading = false
    )

    // Tu tema de la app
    HarmoniaTPITheme(true) {
        SongVersionsContent(
            uiState = previewState,
            onPlayOriginal = { isPlaying = !isPlaying },
            onOpenOriginalProject = {},
            onPlayDerived = {},
            onSliderChange = { newProgress -> currentProgress = newProgress },
            onNavigateBack = {}
        )
    }
}