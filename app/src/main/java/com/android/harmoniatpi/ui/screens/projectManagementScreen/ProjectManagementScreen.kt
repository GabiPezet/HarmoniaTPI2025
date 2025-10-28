package com.android.harmoniatpi.ui.screens.projectManagementScreen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.ui.components.ProyectControlButtonRow
import com.android.harmoniatpi.ui.components.TrackItem
import com.android.harmoniatpi.ui.components.TrimAudioDialog
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel.ProjectManagementScreenViewModel

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
    var trackForTrimming by remember { mutableStateOf<TrackUi?>(null) }
    val context = LocalContext.current
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showSheet = false
            viewModel.importTrackFromFile(it)
        }
    }

    BackHandler {
        viewModel.updateCurrentProjectWithTracks()
        onBack()
        viewModel.clearAllTracks()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { //Impl de top bar
            TopAppBar(
                title = { Text("Gestión de Proyectos") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateCurrentProjectWithTracks()
                        onBack()
                        viewModel.clearAllTracks()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary,
                    actionIconContentColor = MaterialTheme.colorScheme.secondary,
                    scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF858585)),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.tracks.isEmpty()) {
                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    EmptyProjectMessage()
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(state.tracks) { track ->
                    TrackItem(
                        track = track,
                        onClick = { viewModel.selectTrack(track.id) },
                        onDelete = { viewModel.deleteTrack() },
                        onTrim = {
                            if (track.waveForm.isNullOrEmpty() || track.durationMs < 50L) {
                                Toast.makeText(context, "La pista no tiene audio para recortar", Toast.LENGTH_SHORT).show()
                                Log.d("Trim", "Pista sin audio o muy corta para recortar.")
                            } else {
                                trackForTrimming = track
                            }
                        },
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
                        currentPlaybackMs = state.currentPlaybackMs,
                        onSeekClick = { ms -> viewModel.seekAndPlay(ms) },
                        onOffsetChange = { trackId, newOffset ->
                            viewModel.updateTrackOffset(
                                trackId,
                                newOffset
                            )
                        }
                    )
                }
            }

            IconButton(
                onClick = {
                    showSheet = true
                },
                modifier = Modifier
                    .padding(top = 8.dp, start = 8.dp)
                    .size(36.dp)
                    .align(Alignment.Start),

                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            ProyectControlButtonRow(
                onSkipPrevious = { viewModel.stopPlaying() },
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                startRecording = {
                    Toast.makeText(context, "Para una mejor calidad, usa auriculares.", Toast.LENGTH_LONG).show()
                    viewModel.startRecording() },
                stopRecording = { viewModel.stopRecording() },
                isRecording = state.isRecording,
                isPlaying = state.isPlaying,
                modifier = Modifier,
            )

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Añadir Pista", style = MaterialTheme.typography.titleLarge)

                        Button(onClick = {
                            showSheet = false
                            viewModel.addNewTrack(AudioSourceType.VOICE)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("🎤 Grabar Voz (con cancelación de eco)")
                        }

                        Button(onClick = {
                            showSheet = false
                            viewModel.addNewTrack(AudioSourceType.INSTRUMENT)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("🎸 Grabar Instrumento (alta fidelidad)")
                        }

                        Button(onClick = { pickAudioLauncher.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text("📁 Importar desde archivo")
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