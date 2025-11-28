package com.android.harmoniatpi.ui.screens.projectManagementScreen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.EffectsAudioDialog
import com.android.harmoniatpi.ui.components.GlobalPlayhead
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.components.TrimAudioDialog
import com.android.harmoniatpi.ui.components.TunerDialog
import com.android.harmoniatpi.ui.core.utils.PermissionRequester
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.AddTrackSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.EffectsSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.EmptyProjectMessage
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.InDevelopmentSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.MetronomeSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.PrecountOverlay
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.ProjectControlButtonRow
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.RenameTrackSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.TimeDisplayPanel
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.TimelineHeader
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.TrackItem
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.VolumeSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.BottomSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel.ProjectManagementScreenViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    viewModel: ProjectManagementScreenViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sharedScrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var trackForTrimming by remember { mutableStateOf<TrackUi?>(null) }
    val scope = rememberCoroutineScope()
    var trackForEffects by remember { mutableStateOf<TrackUi?>(null) }
    val showTuner by viewModel.showTunerDialog.collectAsState()
    val tunerNote by viewModel.tunerNote.collectAsState()
    var requestRecordVoiceAudioPermission by remember { mutableStateOf(false) }
    var requestRecordInstrumentAudioPermission by remember { mutableStateOf(false) }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importTrackFromFile(it)
        }
    }

    val density = LocalDensity.current

    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()

    LaunchedEffect(state.currentPlaybackMs) {
        if (state.currentPlaybackMs > 0 && sharedScrollState.maxValue > 0 && state.isPlaying) {
            val playbackPx =
                with(density) { (state.currentPlaybackMs / state.msPerDpScale).dp.toPx() }
            val screenWidthPx =
                with(density) { 300.dp.toPx() } // Ancho aprox. de la pantalla visible
            val targetScrollPosition =
                (playbackPx - screenWidthPx / 3).coerceAtLeast(0f).roundToInt()

            if (targetScrollPosition > sharedScrollState.value && (targetScrollPosition - sharedScrollState.value) > 10) {
                sharedScrollState.animateScrollTo(targetScrollPosition)
            }
        }
    }

    BackHandler {
        viewModel.updateCurrentProjectWithTracks()
        viewModel.stopRecording()
        viewModel.stopPlaying()
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

    if (state.importAudioLoading) {
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

    //  bottomSheet inicio
    val activeSheet = state.activeSheetContent
    if (activeSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.stopEffectPreview()
                viewModel.hideBottomSheet()
            },
            sheetState = sheetState
        ) {
            when (activeSheet) {
                is BottomSheetContent.AddTrackMenu -> {

                    AddTrackSheetContent(
                        onImportFromFile = {
                            viewModel.hideBottomSheet()
                            pickAudioLauncher.launch("audio/*")
                        },
                        onRecordVoice = {
                            requestRecordVoiceAudioPermission = true
                            viewModel.hideBottomSheet()
                        },
                        onRecordInstrument = {
                            requestRecordInstrumentAudioPermission = true
                            viewModel.hideBottomSheet()
                        },
                        onPasteTrack = {
                            viewModel.hideBottomSheet()
                            viewModel.pasteFromClipboard()
                        },
                        isClipboardFull = state.isClipboardFull,
                        isPremium = state.isPremium, // USANDO EL UI STATE
                        currentTrackCount = state.tracks.size // USANDO EL UI STATE
                    )
                }

                is BottomSheetContent.EditVolume -> {
                    VolumeSheetContent(
                        track = activeSheet.track,
                        onVolumeChange = { trackId, newVolume ->
                            viewModel.setTrackVolume(trackId, newVolume)
                        },
                        onDismiss = {
                            viewModel.hideBottomSheet()
                        }

                    )
                }

                is BottomSheetContent.RenameTrack -> {
                    RenameTrackSheetContent(
                        track = activeSheet.track,
                        onRename = { trackId, newName ->
                            viewModel.renameTrack(trackId, newName)
                            viewModel.hideBottomSheet()
                        },
                        onDismiss = {
                            viewModel.hideBottomSheet()
                        }
                    )
                }

                is BottomSheetContent.InDevelopment -> {
                    // Composable para "En desarrollo"
                    InDevelopmentSheetContent()
                }

                is BottomSheetContent.TrackEffects -> {
                    EffectsSheetContent(
                        track = activeSheet.track,
                        isPreviewing = isPreviewPlaying,
                        onPreviewToggle = { config ->
                            viewModel.toggleEffectPreview(activeSheet.track.id, config)
                        },
                        onParamChange = { config ->
                            viewModel.updatePreviewParams(activeSheet.track.id, config)
                        },
                        onApplyPreset = { id, type ->
                            viewModel.stopEffectPreview()
                            viewModel.applyPreset(id, type)
                            viewModel.hideBottomSheet()
                        },
                        onApplyDelay = { id, delay, decay ->
                            viewModel.stopEffectPreview() // Detener preview al aplicar
                            viewModel.applyDelayEffect(id, delay, decay)
                            viewModel.hideBottomSheet()
                        },
                        onApplyHighPass = { id, freq ->
                            viewModel.stopEffectPreview()
                            viewModel.applyHighPassFilter(id, freq)
                            viewModel.hideBottomSheet()
                        },
                        onApplyFlanger = { id, rate, wet ->
                            viewModel.stopEffectPreview()
                            viewModel.applyFlangerEffect(id, rate, wet)
                            viewModel.hideBottomSheet()
                        },
                        onApplyLowPass = { id, frequency ->
                            viewModel.stopEffectPreview()
                            viewModel.applyLowPassFilter(id, frequency)
                            viewModel.hideBottomSheet()
                        },
                        onApplyTelephone = { id ->
                            viewModel.stopEffectPreview()
                            viewModel.applyTelephoneEffect(id)
                            viewModel.hideBottomSheet()
                        },
                        onApplyFadeIn = { id, duration ->
                            viewModel.stopEffectPreview()
                            viewModel.applyFadeIn(id, duration)
                            viewModel.hideBottomSheet()
                        },
                        onApplyFadeOut = { id, duration ->
                            viewModel.stopEffectPreview()
                            viewModel.applyFadeOut(id, duration)
                            viewModel.hideBottomSheet()
                        },
                        onApplyDistortion = { id, drive ->
                            viewModel.stopEffectPreview()
                            viewModel.applyDistortion(id, drive)
                            viewModel.hideBottomSheet()
                        },
                        onApplyTremolo = { id, frequency, depth ->
                            viewModel.stopEffectPreview()
                            viewModel.applyTremolo(id, frequency, depth)
                            viewModel.hideBottomSheet()
                        },
                        onNormalize = { id ->
                            viewModel.stopEffectPreview()
                            viewModel.normalizeTrack(id)
                            viewModel.hideBottomSheet()
                        },
                        onDismiss = {
                            viewModel.stopEffectPreview() // Detener al cancelar
                            viewModel.hideBottomSheet()
                        },
                        isPremium = state.isPremium,
                        onShowUpsell = onNavigateToPremium
                    )
                }

                is BottomSheetContent.MetronomeSettings -> {
                    MetronomeSheetContent(
                        currentBpm = state.bpm,
                        isMetronomeEnabled = state.isMetronomeEnabled,
                        currentVolume = state.metronomeVolume,
                        onBpmChange = viewModel::setBpm,
                        onMetronomeEnabledChange = viewModel::setMetronomeEnabled,
                        onVolumeChange = viewModel::setMetronomeVolume,
                        onDismiss = viewModel::hideBottomSheet
                    )
                }
            }
        }
    }
    // BottomSheet final

    // SnackBar inicio

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    //Muestra el snackbar para ProjectControlButtonRow al cambiar mensaje
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                snackbarMessage = null
            }
        }
    }
    // Escucha mensajes del viewmodel
    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
    // SnackBar fin


    val errorPulseScale = remember { Animatable(1f) }
    LaunchedEffect(state.fabPulseTrigger) {
        if (state.fabPulseTrigger > 0) {
            scope.launch {
                errorPulseScale.animateTo(
                    targetValue = 1.3f,
                    animationSpec = tween(150, easing = LinearOutSlowInEasing)
                )
                errorPulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(200)
                )
            }
        }
    }


    val infiniteTransition = rememberInfiniteTransition(label = "FAB Empty Pulse")

    val ctaPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabCtaScale"
    )
    val baseScale = if (state.tracks.isEmpty()) {
        ctaPulseScale
    } else {
        1f
    }

    val finalFabScale = baseScale * errorPulseScale.value




    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ProjectManagementScreen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.currentProjectSelected!!.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                            textAlign = TextAlign.End,

                            )

                        Spacer(Modifier.width(8.dp))

                        val statusText = if (state.isPremium) "PRO" else "FREE"

                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.updateCurrentProjectWithTracks()
                            onBack()
                        },
                        enabled = !state.isRecording
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },

                actions = {
                    IconButton(
                        onClick = { viewModel.onShowTuner() },
                        enabled = !state.isRecording
                    ) {
                        Icon(Icons.Default.Tune, "Afinador")
                    }
                    IconButton(
                        onClick = { viewModel.zoomOut() },
                        enabled = !state.isRecording
                    ) {
                        Icon(Icons.Default.ZoomOut, "Zoom Out")
                    }
                    IconButton(
                        onClick = { viewModel.zoomIn() },
                        enabled = !state.isRecording
                    ) {
                        Icon(Icons.Default.ZoomIn, "Zoom In")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.background(Color(0xFF858585))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeDisplayPanel(
                        currentMillis = state.currentPlaybackMs,
                        totalMillis = state.totalProjectMs,
                        onMetronomeClick = {
                            viewModel.showMetronomeSheet()
                        },
                        isBeingRecorded = state.isRecording,
                        isPlaying = state.isPlaying,
                        bpm = state.bpm,
                        isMetronomeEnabled = state.isMetronomeEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    if (!state.isRecording) {
                        IconButton(
                            onClick = {
                                viewModel.showBottomSheet(BottomSheetContent.AddTrackMenu)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .graphicsLayer {
                                    scaleX = finalFabScale
                                    scaleY = finalFabScale
                                },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir Pista",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }

                ProjectControlButtonRow(
                    onSkipPrevious = {
                        viewModel.stopPlaying()
                        scope.launch {
                            sharedScrollState.animateScrollTo(0)
                        }
                    },
                    onPlay = { viewModel.play() },
                    onPause = { viewModel.pause() },
                    startRecording = {
                        snackbarMessage = "Para una mejor experiencia, usa auriculares."
                        viewModel.startRecording()
                    },
                    stopRecording = { viewModel.stopRecording() },
                    isRecording = state.isRecording,
                    isPlaying = state.isPlaying,
                    onError = { message ->
                        snackbarMessage = message
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF858585)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(sharedScrollState)
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

                // 1. LA LISTA DE PISTAS (SOLO SE VE SI NO ESTÁ CARGANDO)
                if (!state.isLoadingProject) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
                                onCopy = {
                                    scope.launch { viewModel.copySelection() }
                                },
                                onCut = {
                                    scope.launch { viewModel.cutSelection() }
                                },
                                onUndoEffect = { viewModel.undoEffect(track.id) },
                                isUndoEffectAvailable = track.isUndoEffectAvailable,
                                isSelectionActive = track.selectionStartMs != null &&
                                        (track.selectionEndMs == null || track.selectionEndMs > track.selectionStartMs),
                                msPerDpScale = state.msPerDpScale,
                                areControlsEnabled = !state.isRecording,
                                onShowBottomSheet = viewModel::showBottomSheet
                            )
                        }
                    }
                }
                if (state.isLoadingProject) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)), // Fondo semi-oscuro opcional
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = "Restaurando pistas...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. GlobalPlayhead (Siempre visible o condicional, según prefieras)
                if (!state.isLoadingProject) {
                    GlobalPlayhead(
                        currentPlaybackMs = state.currentPlaybackMs,
                        msPerDpScale = state.msPerDpScale,
                        scrollState = sharedScrollState
                    )
                }

            }
        }
    }
    if (state.precountMessage != null) {
        PrecountOverlay(message = state.precountMessage!!)
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
            isPremium = state.isPremium,
            onGoToPremium = onNavigateToPremium,
            onDismiss = { trackForEffects = null },
            onApplyDelay = { id, delay, decay ->
                viewModel.applyDelayEffect(id, delay, decay)
                trackForEffects = null
            },
            onApplyHighPass = { id, freq ->
                viewModel.applyHighPassFilter(id, freq)
                trackForEffects = null
            },
            onApplyFlanger = { id, rate, wet ->
                viewModel.applyFlangerEffect(id, rate, wet)
                trackForEffects = null
            }
        )
    }

    if (showTuner) {
        TunerDialog(
            note = tunerNote,
            onDismiss = { viewModel.onDismissTuner() },
            onStart = { viewModel.startTuner() },
            onStop = { viewModel.stopTuner() }
        )
    }

    if (requestRecordVoiceAudioPermission) {
        PermissionRequester(
            permission = Manifest.permission.RECORD_AUDIO,
            rationaleRes = R.string.record_audio_rationale,
            permanentlyDeniedRes = R.string.record_audio_denied_msg,
            onGranted = {
                viewModel.addNewTrack(AudioSourceType.VOICE)
                requestRecordVoiceAudioPermission = false
            },
            onDialogDismiss = { requestRecordVoiceAudioPermission = false }
        )
    }
    if (requestRecordInstrumentAudioPermission) {
        PermissionRequester(
            permission = Manifest.permission.RECORD_AUDIO,
            rationaleRes = R.string.record_audio_rationale,
            permanentlyDeniedRes = R.string.record_audio_denied_msg,
            onGranted = {
                viewModel.addNewTrack(AudioSourceType.INSTRUMENT)
                requestRecordInstrumentAudioPermission = false
            },
            onDialogDismiss = { requestRecordInstrumentAudioPermission = false }
        )
    }
}



