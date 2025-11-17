package com.android.harmoniatpi.ui.screens.projectManagementScreen

import android.Manifest
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.R
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.components.CircularProgressBar
import com.android.harmoniatpi.ui.components.EffectsAudioDialog
import com.android.harmoniatpi.ui.components.GlobalPlayhead
import com.android.harmoniatpi.ui.components.ProyectControlButtonRow
import com.android.harmoniatpi.ui.components.ShowConfirmationDialog
import com.android.harmoniatpi.ui.components.TimelineHeader
import com.android.harmoniatpi.ui.components.TrackItem
import com.android.harmoniatpi.ui.components.TrimAudioDialog
import com.android.harmoniatpi.ui.components.TunerDialog
import com.android.harmoniatpi.ui.components.VolumeSliderDialog
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.AddTrackSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.EmptyProjectMessage
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.InDevelopmentSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.components.RenameTrackSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.BottomSheetContent
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel.ProjectManagementScreenViewModel
import com.android.harmoniatpi.ui.utils.PermissionRequester
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    viewModel: ProjectManagementScreenViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sharedScrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var trackForTrimming by remember { mutableStateOf<TrackUi?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var trackForEffects by remember { mutableStateOf<TrackUi?>(null) }
    val trackForVolume by viewModel.trackForVolume.collectAsState()
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

    //  NUEVO BOTTOMSHEET
    val activeSheet = state.activeSheetContent
    if (activeSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.hideBottomSheet() },
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
                        isClipboardFull = state.isClipboardFull
                    )
                }

                is BottomSheetContent.EditVolume -> {
                    /*// Nuevo Composable para el volumen
                    VolumeSheetContent(
                        track = activeSheet.track,
                        onVolumeChange = { trackId, newVolume ->
                            viewModel.setTrackVolume(trackId, newVolume)
                        }
                    )*/
                    InDevelopmentSheetContent()

                }

                is BottomSheetContent.RenameTrack -> {
                    RenameTrackSheetContent (
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
                    // ... el contenido para los efectos
                }
            }
        }
    }
    // ----> FIN  BOTTOMSHEET <----

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
                    IconButton(onClick = { viewModel.onShowTuner() }) {
                        Icon(Icons.Default.Tune, "Afinador")
                    }
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
                            msPerDpScale = state.msPerDpScale,
                            onShowVolumeSlider = { viewModel.onShowVolumeSlider(track) },
                            onShowBottomSheet = viewModel::showBottomSheet
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
                    viewModel.showBottomSheet(BottomSheetContent.AddTrackMenu)
                },
                modifier = Modifier
                    .padding(top = 16.dp, end = 32.dp)
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
                onSkipPrevious = {
                    viewModel.stopPlaying()
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


    trackForVolume?.let { track ->
        VolumeSliderDialog(
            track = track,
            onDismiss = { viewModel.onDismissVolumeSlider() },
            onConfirm = { newVolume ->
                viewModel.setTrackVolume(newVolume)
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



