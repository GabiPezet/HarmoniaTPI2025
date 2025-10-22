package com.android.harmoniatpi.ui.screens.songVersionsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.user.User
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.core.theme.HarmoniaTPITheme
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.PlaybackState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.model.SongVersionsUiState
import com.android.harmoniatpi.ui.screens.songVersionsScreen.util.formatMillisToTimeString
import com.android.harmoniatpi.ui.screens.songVersionsScreen.viewModel.SongVersionsViewModel

/**
 * Composable **stateful** (con estado) para la pantalla de detalles de canciones.
 *
 * Esta función actúa como el **punto de entrada** a la pantalla. Se encarga de:
 * 1. Obtener la instancia del [SongVersionsViewModel] usando Hilt (`hiltViewModel()`).
 * 2. Observar y recolectar el [SongVersionsUiState] expuesto por el ViewModel.
 * 3. Pasar el estado y las referencias a las funciones del ViewModel al composable
 * stateless [SongVersionsContent], que se encarga del renderizado de la UI.
 *
 * Esta separación permite mantener la lógica de estado y la obtención de datos
 * desacoplada de la lógica de presentación pura.
 *
 * @param viewModel Instancia del ViewModel gestionada por Hilt.
 * @param onNavigateBack Lambda para manejar la acción de navegación hacia atrás.
 */
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

/**
 * Composable **stateless** (sin estado) para el contenido de la pantalla de detalles de canciones.
 *
 * Esta función se encarga exclusivamente de **dibujar la interfaz de usuario** basándose en el [uiState]
 * proporcionado y de **notificar las interacciones del usuario** a través de las funciones lambda
 * (ej: [onPlayOriginal], [onSliderChange]).
 *
 * **No contiene lógica de negocio ni gestiona su propio estado.**
 *
 * **¿Por qué dos Composable (`SongVersionsScreen` y `SongVersionsContent`)?**
 * Esta separación sigue el patrón **Stateful vs. Stateless**.
 * - `SongVersionsScreen` (Stateful): Es el composable "inteligente". Obtiene el `ViewModel`
 * (usando `hiltViewModel()`), recolecta el `UiState` y conecta los eventos de la UI
 * con las funciones del `ViewModel`.
 * - `SongVersionsContent` (Stateless): Es el composable "tonto". Solo recibe datos y lambdas.
 * Esto lo hace **altamente reutilizable y fácil de previsualizar y testear**
 * en aislamiento, ya que no depende de `ViewModel` ni de Hilt.
 *
 * @param uiState El estado actual de la pantalla, que contiene toda la información a mostrar.
 * @param onPlayOriginal Lambda que se invoca cuando se presiona el botón de play/pausa de la canción original.
 * @param onOpenOriginalProject Lambda que se invoca al presionar "Abrir proyecto" en la canción original.
 * @param onPlayDerived Lambda que se invoca cuando se presiona el botón de play/pausa de una versión derivada.
 * @param onSliderChange Lambda que se invoca cuando el usuario interactúa con el slider de progreso.
 * @param onNavigateBack Lambda que se invoca al presionar el botón de navegación hacia atrás.
 * @param modifier Modificador de Compose para personalizar la apariencia o comportamiento.
 */

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
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressBar("Cargando...")
        }
    } else {
        Scaffold(

            modifier = modifier,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Icon(
                            painter = painterResource(R.drawable.ic_harmonyicon),
                            contentDescription = "Slider Thumb",
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            if (uiState.song != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val originalSong = uiState.song

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SongHeader(song = originalSong)
                        Spacer(modifier = Modifier.height(16.dp))
                        PrincipalSongPlayer(
                            song = originalSong,
                            isPlaying = uiState.playingSongId == originalSong.id && uiState.playbackState.isPlaying,
                            playbackState = uiState.playbackState,
                            onPlayClick = onPlayOriginal,
                            onOpenProjectClick = { onOpenOriginalProject(originalSong.projectId) },
                            onSliderValueChange = onSliderChange
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "VERSIONES DERIVADAS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.derivedVersions) { version ->
                        val isThisPlaying =
                            uiState.playingSongId == version.id && uiState.playbackState.isPlaying
                        DerivedVersionItem(
                            version = version,
                            isPlaying = isThisPlaying,
                            playbackState = uiState.playbackState,
                            onPlayClick = { onPlayDerived(version.id) },
                            onSliderChange = onSliderChange,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

    }
}

/**
 * Composable para mostrar la información de una canción.[Song]
 */
@Composable
fun SongHeader(song: Song, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = song.imageUrl,
            contentDescription = "Carátula de la canción ${song.title}",
            placeholder = painterResource(id = R.drawable.holojamdefaultsonglightmode),
            error = painterResource(id = R.drawable.holojamdefaultsonglightmode),
            modifier = Modifier
                .size(80.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.creator.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}

/**
 * Composable para mostrar la información de una canción base [Song]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalSongPlayer(
    song: Song,
    isPlaying: Boolean,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    onSliderValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
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
                    text = song.creator.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = song.versionType.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Reemplazar con Coil o Glide para cargar imágenes desde URL y borrar background
                AsyncImage(
                    model = song.creator.avatarUrl,
                    placeholder = painterResource(id = R.drawable.holojamperfildefaultblackmode),
                    error = painterResource(id = R.drawable.holojamperfildefaultblackmode),
                    contentDescription = "Imagen de artista: ${song.creator.name}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), CircleShape),
                    contentScale = ContentScale.Crop
                )
                TextButton(
                    onClick = { if (song.projectId != null) onOpenProjectClick() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(
                        text = "Abrir proyecto",
                        style = MaterialTheme.typography.titleSmall,
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

            val displayDurationMs =
                if (isPlaying) playbackState.totalDurationMs else song.durationMillis
            val displayPositionMs = if (isPlaying) playbackState.currentPositionMs else 0L
            val currentProgress = if (displayDurationMs > 0) {
                displayPositionMs.toFloat() / displayDurationMs.toFloat()
            } else 0f

            PlayerSliderControls(
                durationMillis = displayDurationMs,
                currentProgress = currentProgress,
                currentPositionMs = displayPositionMs,
                onSliderValueChange = onSliderValueChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Composable para mostrar la información de una canción base [Song]. Duración, progreso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSliderControls(
    durationMillis: Long,
    currentProgress: Float,
    currentPositionMs: Long,
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
                text = formatMillisToTimeString(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
            )
            //Text(
            //text = formatMillisToTimeString(durationMillis - (currentProgress * durationMillis).toLong()),
            Text(
                text = formatMillisToTimeString(durationMillis),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/**
 * Composable para mostrar un botón de play/pause.
 */
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

/**
 * Composable para mostrar un track personalizado.
 */
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

/**
 * Composable para mostrar un track personalizado. Una versión derivada-
 */
@Composable
fun DerivedVersionItem(
    version: DerivedVersion,
    isPlaying: Boolean,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onSliderChange: (Float) -> Unit,
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = version.creator.avatarUrl,
                    placeholder = painterResource(id = R.drawable.holojamperfildefaultblackmode),
                    contentDescription = "Avatar de artista: ${version.creator.name}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), CircleShape),
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
                            text = version.creator.name,
                            maxLines = 2,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                CircularPlay(
                    onPlay = onPlayClick,
                    onPause = onPlayClick,
                    isPlaying = isPlaying,
                    modifier = Modifier.size(30.dp),
                    background = MaterialTheme.colorScheme.tertiary,
                    iconColor = MaterialTheme.colorScheme.onTertiary
                )
            }
            AnimatedVisibility(visible = isPlaying) {
                val displayDurationMs =
                    if (isPlaying) playbackState.totalDurationMs else version.durationMillis
                        ?: 0L
                val displayPositionMs = if (isPlaying) playbackState.currentPositionMs else 0L
                val currentProgress = if (displayDurationMs > 0) {
                    displayPositionMs.toFloat() / displayDurationMs.toFloat()
                } else 0f

                PlayerSliderControls(
                    durationMillis = displayDurationMs,
                    currentProgress = currentProgress,
                    currentPositionMs = displayPositionMs,
                    onSliderValueChange = onSliderChange,
                    modifier = Modifier.padding()
                )
            }
        }
    }
}

/**
 * Composable para mostrar la información de una canción base [Song]. Duración, progreso.
 */
@Preview(showBackground = true, showSystemUi = false, name = "Light Mode")
@Composable
fun SongVersionsScreenPreview() {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }


    val previewState = SongVersionsUiState(
        song = createMockSong(),
        derivedVersions = createMockDerivedVersions(),
        playbackState = PlaybackState(),
        isLoading = false
    )

    HarmoniaTPITheme(false) {
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

@Preview(showBackground = true)
@Composable
fun SongHeaderPreview() {
    val sampleCreator = User(id = "1", name = "Luna Beats", avatarUrl = null)
    val sampleSong = createMockSong()
    MaterialTheme {
        SongHeader(song = sampleSong, modifier = Modifier.padding(16.dp))
    }
}