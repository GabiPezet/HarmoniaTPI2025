package com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CommentBank
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.android.harmoniatpi.ui.core.theme.PrimaryWhite
import kotlin.random.Random

@Composable
fun RehearsalRoomScreen(
    onNavigateToProjectManagement: () -> Unit
) {

    val selectedTab = remember { ProjectTab.MY_PROJECTS }
    val sampleProjects = remember {
        listOf(
            Project(
                title = "Proyecto 001",
                description = "Ideas iniciales ambient y demo.",
                hashtags = listOf("#Rock", "#Piano"),
                audioWaveform = List(150) { Random.nextFloat() * 0.7f + 0.1f}
            ),
            Project(
                title = "Proyecto 002",
                description = "Super patrón ritmico, anotaciones.",
                hashtags = listOf("#Indie", "#Guitarra"),
                audioWaveform = List(150) { Random.nextFloat() * 0.7f + 0.1f}
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToProjectManagement, //onAddProject,
                containerColor = Color(0xFFFBC658), // Amarillo PrimaryHarmonia
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
            }
        },
        containerColor = Color(0xFFF5F5F5) // Fondo gris claro
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            // Selector de pestañas proyecto y colaboraciones
            ProjectTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { /* Hacer Navegación entre pestañas */} )

            // Lista de proyectos
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sampleProjects) { project ->
                    ProjectCard(project)
                }
            }
        }
    }
// Esta es la BOX con el botón de navegación viejo

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        //Text(text = "RehearsalRoomScreen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
//        Button(
//            onClick = onNavigateToProjectManagement
//        ) {
//            Text("Manejo de Proyectos")
//        }
//    }
}


@Composable
fun ProjectTabSelector(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit,
    modifier: Modifier = Modifier,
    tabHeight: Dp = 40.dp,
    cornerRadius: Dp = 15.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabButtonB(
            "Mis Proyectos",
            selectedTab == ProjectTab.MY_PROJECTS,
            onClick = { onTabSelected(ProjectTab.MY_PROJECTS) },
            modifier = Modifier
                .weight(1f)
                .height(tabHeight),
            cornerRadius = cornerRadius
        )
        Spacer(modifier = Modifier.width(8.dp))

        TabButtonB(
            "Colaboraciones",
            selectedTab == ProjectTab.COLLABS,
            onClick = { onTabSelected(ProjectTab.COLLABS) },
            modifier = Modifier
                .weight(1f)
                .height(tabHeight),
            cornerRadius = cornerRadius
        )
    }
}

@Composable
fun TabButtonA(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.Black else Color.Transparent,
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TabButtonB(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        color = if (selected) Color.Black else Color.Transparent,
        tonalElevation = if (selected) 4.dp else 0.dp,
        shadowElevation = if (selected) 4.dp else 0.dp,
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(15.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}


@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(project.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            WaveformPreview(project.audioWaveform)
            Spacer(Modifier.height(8.dp))
            Text(project.description, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(Modifier.height(4.dp))
            Text(project.hashtags.joinToString(" "), fontStyle = FontStyle.Italic, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { /* Likear */ }) { Icon(Icons.Default.Favorite, contentDescription = "Like") }
                IconButton(onClick = { /* Comentar */ }) { Icon(Icons.Default.ModeComment, contentDescription = "Comment") }
                IconButton(onClick = { /* Compartir */ }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                IconButton(onClick = { /* Descargar */ }) { Icon(Icons.Default.Download, contentDescription = "Download") }
                IconButton(onClick = { /* Settings */ }) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
            }
        }
    }
}

// data class local de prueba
data class Project(
    val title: String,
    val description: String,
    val hashtags: List<String>,
    val audioWaveform: List<Float> // Intento de simulación de forma de onda
)

// enum class local de prueba
enum class ProjectTab {
    MY_PROJECTS,
    COLLABS
}

@Composable
fun WaveformPreview(
    waveform: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(64.dp),
    barColor: Color = Color(0xFFFAFAFA),
    backgroundColor: Color = Color.Transparent
) { //En esta parte se crea una onda y si waveform viene vacía la completa con valores aleatorios
    // vas a ver que ahora en este testeo la onda viene rellena con 150 muestras más arriba
    // cuando recibimos la onda, la normalizamos para poder hacer que todas ocupen el mismo espacio y tengan formas similares
    val safeWave = if (waveform.isEmpty()) listOf(0.5f) else waveform
    val normalized = remember(safeWave) {
        // Clamp y normalizar entre 0..1
        val clamped = safeWave.map { it.coerceIn(0f, 1f) }
        val max = clamped.maxOrNull().takeIf { it != 0f } ?: 1f
        clamped.map { it / max }
    }

    Canvas(modifier = modifier.background(backgroundColor)) {
        val w = size.width
        val h = size.height
        val count = normalized.size
        val spacing = (w * 0.03f) / (count.coerceAtLeast(1)) // Este es el valor para un espaciado relativo, hay que ir probandoló.
        val availableWidth = w - spacing * (count - 1)
        val barWidth = (availableWidth / count).coerceAtLeast(1f)

        normalized.forEachIndexed { i, value ->
            val left = i * (barWidth + spacing)
            val barHeight = value * h
            val top = (h - barHeight) / 2f
            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}