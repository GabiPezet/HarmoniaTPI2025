package com.android.harmoniatpi.ui.screens.projectManagementScreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.EffectsAudioDialog
import com.android.harmoniatpi.ui.components.GlobalPlayhead
import com.android.harmoniatpi.ui.components.ProyectControlButtonRow
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.components.TimelineHeader
import com.android.harmoniatpi.ui.components.TrackItem
import com.android.harmoniatpi.ui.components.TrimAudioDialog
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel.ProjectManagementScreenViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    viewModel: ProjectManagementScreenViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val sharedScrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var trackForTrimming by remember { mutableStateOf<TrackUi?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var trackForEffects by remember { mutableStateOf<TrackUi?>(null) }
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showSheet = false
            viewModel.importTrackFromFile(it)
        }
    }

    val density = LocalDensity.current
    LaunchedEffect(state.currentPlaybackMs) {
        if (state.currentPlaybackMs > 0 && sharedScrollState.maxValue > 0 && state.isPlaying) {
            val playbackPx = with(density) { (state.currentPlaybackMs / state.msPerDpScale).dp.toPx() }
            val screenWidthPx = with(density) { 300.dp.toPx() } // Ancho aprox. de la pantalla visible
            val targetScrollPosition = (playbackPx - screenWidthPx / 3).coerceAtLeast(0f).roundToInt()

            if (targetScrollPosition > sharedScrollState.value && (targetScrollPosition - sharedScrollState.value) > 10) {
                sharedScrollState.animateScrollTo(targetScrollPosition)
            }
        }
    }


    BackHandler {
        viewModel.updateCurrentProjectWithTracks()
        onBack()
    }



    if (showDeleteDialog) {
        ShowConfirmationDialog(
            show = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteTrack()
                showDeleteDialog = false
            },
            title = "¿Estas seguro?",
            message = "Vas a perder los cambios si borras la pista",
            confirmText = "Borrar"
        )
    }

    if (state.importAudioLoading){
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                CircularProgressBar(message = "Importando...", importProject = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { //Impl de top bar
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        text = state.currentProjectSelected!!.title,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateCurrentProjectWithTracks()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },

                actions = {
                    IconButton(onClick = { viewModel.zoomOut() }) {
                        Icon(Icons.Default.ZoomOut, "Zoom Out")
                    }
                    IconButton(onClick = { viewModel.zoomIn() }) {
                        Icon(Icons.Default.ZoomIn, "Zoom In")
                    }
                },

                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        },
        //containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF858585)), //Pasar ESTE background al Theme Colors
            //verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(sharedScrollState) // Se sincroniza con el LazyColumn
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                TimelineHeader(
                    timelineWidth = state.timelineWidth,
                    msPerDpScale = state.msPerDpScale
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (state.tracks.isEmpty()) {
                        item {
                            Box(modifier = Modifier.padding(top = 64.dp)) {
                                EmptyProjectMessage()
                            }
                        }
                    }

                    items(state.tracks) { track ->
                        TrackItem(
                            track = track,
                            onClick = { viewModel.selectTrack(track.id) },
                            onDelete = { showDeleteDialog = true },
                            onShowEffects = { trackForEffects = track },
                            onUndo = {
                                viewModel.undoTrim(track.id)
                            },
                            scrollState = sharedScrollState,
                            timelineWidth = state.timelineWidth,
                            isBeingRecorded = state.isRecording && track.selected,
                            onMute = {
                                if (track.isMuted) {
                                    viewModel.unMuteTrack()
                                } else {
                                    viewModel.muteTrack()
                                }
                            },
                            currentPlaybackMs = 0L,
                            onSeekClick = { ms -> viewModel.seekAndPlay(ms) },
                            onOffsetChange = { trackId, newOffset ->
                                viewModel.updateTrackOffset(
                                    trackId,
                                    newOffset
                                )
                            },
                            onSelectionChanged = { startMs, endMs ->
                                viewModel.updateTrackSelection(track.id, startMs, endMs)
                            },
                            onCopy = { viewModel.copySelection() },
                            onCut = { viewModel.cutSelection() },
                            onUndoEffect = { viewModel.undoEffect(track.id) },
                            isUndoEffectAvailable = track.isUndoEffectAvailable,
                            isSelectionActive = track.selectionStartMs != null &&
                                    (track.selectionEndMs == null || track.selectionEndMs > track.selectionStartMs),
                            msPerDpScale = state.msPerDpScale
                        )
                    }
                }

                GlobalPlayhead(
                    currentPlaybackMs = state.currentPlaybackMs,
                    msPerDpScale = state.msPerDpScale,
                    scrollState = sharedScrollState
                )

            }

            IconButton(
                onClick = {
                    showSheet = true
                },
                modifier = Modifier
                    .padding(top = 16.dp, start = 32.dp)
                    .size(50.dp)
                    .align(Alignment.End),

                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            //Spacer(modifier = Modifier.weight(1f))

            ProyectControlButtonRow(
                onSkipPrevious = { viewModel.stopPlaying()
                    scope.launch {
                        sharedScrollState.animateScrollTo(0)
                    }
                                 },
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                startRecording = {
                    Toast.makeText(
                        context,
                        "Para una mejor calidad, usa auriculares.",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.startRecording()
                },
                stopRecording = { viewModel.stopRecording() },
                isRecording = state.isRecording,
                isPlaying = state.isPlaying,
                modifier = Modifier,
            )

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF121212), // Fondo oscuro del MBS
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Añadir pista",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Primera Fila - Pista de Voz y Pista de instrumento
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OptionCard(
                                title = "Grabar Voz\n(Cancelación\n de eco)",
                                icon = Icons.Default.Mic,
                                onClick = {
                                    showSheet = false
                                    viewModel.addNewTrack(AudioSourceType.VOICE)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            OptionCard(
                                title = "Grabar Instrumento\n(Hi-Fi)",
                                icon = Icons.Default.MusicNote,
                                onClick = {
                                    showSheet = false
                                    viewModel.addNewTrack(AudioSourceType.INSTRUMENT)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Segunda fila - Importar desde un archivo
                        OptionCard(
                            title = "Importar desde archivo",
                            icon = Icons.Default.Folder,
                            onClick = { pickAudioLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.isClipboardFull) {
                            OptionCard(
                                title = "Pegar Pista",
                                icon = Icons.Default.ContentPaste,
                                onClick = {
                                    showSheet = false
                                    viewModel.pasteFromClipboard()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }
                }
            }
        }
    }

    trackForTrimming?.let { trackToTrim ->
        TrimAudioDialog(
            track = trackToTrim,
            previewTrackId = state.previewTrackId,
            onDismiss = { trackForTrimming = null },
            onConfirmTrim = { id, start, end ->
                viewModel.trimAudio(id, start, end)
                trackForTrimming = null
            },
            onPreviewTrim = { id, start, end ->
                viewModel.previewTrim(id, start, end)
            },
            onStopPreview = { id ->
                viewModel.stopPreviewTrim(id)
            }
        )
    }
    trackForEffects?.let { trackToEffect ->
        EffectsAudioDialog(
            track = trackToEffect,
            onDismiss = { trackForEffects = null },
            onApplyDelay = { id, delay, decay ->
                viewModel.applyDelayEffect(id, delay, decay)
                trackForEffects = null
            }
        )
    }
}

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

// Composable para cada opción de la BottomSheet
@Composable
fun OptionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Spacer(modifier = Modifier.padding(240.dp))
            // Icono circular flotante
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(Color(0xFFFF8117), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}