package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import java.io.File


data class UserProfile(
    val name: String,
    val instrument: String,
    val genres: String,
    val location: String,
    val ratingPercent: Int
) // modelo de usuario para la Demo

data class Project(
    val id: String,
    val title: String,
    val description: String
) // modelo de proyecto para la Demo

enum class ProfileTab { WORK, MEDIA } // enum con la lista de pestañas para seleccionar

// colores de referencia para la Demo

private val Yellow = Color(0xFFFFD600)
private val BeigeCard = Color(0xFFF2E8D6)
private val Background = Color(0xFFFDFDFD)
private val GrayText = Color(0xFF6B6B6B)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailProfileDemo(
    viewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    innerPadding: PaddingValues,
    user: UserProfile = sampleUser(),
    projects: List<Project> = sampleProjects()

) {
    val context = LocalContext.current
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val userPhotoPath by viewModel.userPhotoPath.collectAsState()
    var selectedTab by remember { mutableStateOf(ProfileTab.WORK)}

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            val picturesDir = File(context.filesDir, "pictures")
            val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")
            viewModel.saveUserPhoto(imageFile.absolutePath)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val picturesDir = File(context.filesDir, "pictures")
            if (!picturesDir.exists()) picturesDir.mkdirs()
            val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.saveUserPhoto(imageFile.absolutePath)
        }
    }

    fun createImageUri(context: Context): Uri {
        val picturesDir = File(context.filesDir, "pictures")
        if (!picturesDir.exists()) picturesDir.mkdirs()

        val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")
        if (!imageFile.exists()) imageFile.createNewFile()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    BackHandler {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PERFIL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "volver al menú anterior",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        },

    ) { paddingValues -> //El PaddingValues es idea de Cristian supongo, hay que preguntar bien que hace, porque en jerarquía está sobre todos los demás componentes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (userPhotoPath.path.isBlank()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(150.dp)
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(userPhotoPath.path),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                IconButton(
                    onClick = { showSheet = true },
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            CompactSymmetricButtons()
// estos son los botones originales para Compartir y ver seguidores pero son muy sencillos, no quedaron bien
// lo dejo porque los CompactSymmetricButtons tampoco me están convenciendo por ahora
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .widthIn(max = 200.dp), // ajustá este valor según diseño
//                horizontalArrangement = Arrangement.Center
//            ){
//                OutlinedButton(onClick = { /* seguidores */ },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(44.dp)
//                ) {
//                    Text("Seguidores")
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                OutlinedButton(onClick = { /* compartir perfil */ },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(44.dp)) {
//                    Text("Compartir perfil")
//                }
//            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ProfileNavButton(
                    icon = Icons.Default.School,
                    label = "Perfil laboral",
                    selected = selectedTab == ProfileTab.WORK,
                    onClick = { selectedTab = ProfileTab.WORK }
                )
                Spacer(modifier = Modifier.width(24.dp))
                ProfileNavButton(
                    icon = Icons.Default.Movie,
                    label = "Multimedia",
                    selected = selectedTab == ProfileTab.MEDIA,
                    onClick = { selectedTab = ProfileTab.MEDIA }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    ProfileTab.WORK -> WorkProfileCard(user = user)
                    ProfileTab.MEDIA -> MediaProjectList(projects = projects)
                }
            }
//            Este es el MOCK original que Cris dejó en la pantalla para referenciar
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Spacer(modifier = Modifier.height(24.dp))
//            Text(
//                text = "ID: ${uiState.userID}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.secondary
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = uiState.userName.ifEmpty { "Pepe" },
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.secondary
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = uiState.userLastName.ifEmpty { "ArgEnTo" },
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.secondary
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = uiState.userEmail.ifEmpty { "email" },
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.secondary
//            )
            Spacer(modifier = Modifier.width(24.dp))
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        val uri = createImageUri(context)
                        photoUri = uri
                        takePictureLauncher.launch(uri)
                        showSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Tomar foto",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        pickImageLauncher.launch("image/*")
                        showSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Elegir de galería",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

//este composable son los botones de navegación que permiten intercambiar entre la lista de proyectos y el perfil card de usuario
@Composable
fun ProfileNavButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selected) Color(0xFFFFBA2B) else Color(0xFFF6E0BB),
            shadowElevation = if (selected) 6.dp else 0.dp,
            tonalElevation = if (selected) 6.dp else 0.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 13.sp, color = if (selected) Color.Black else GrayText)
    }
}

//Este composable es el que contiene TODA la info personal del usuario
@Composable
fun WorkProfileCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(BeigeCard)
            .padding(16.dp)
        ) {
            Text("Perfil Profesional", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            ProfileRow(label = "Tu Instrumento:", value = user.instrument, leading = Icons.Default.Mic)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileRow(label = "Género Favorito:", value = user.genres, leading = Icons.Default.MusicNote)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileRow(label = "Ubicación:", value = user.location, leading = Icons.Default.Place)
            Spacer(modifier = Modifier.height(8.dp))
            // La fila de Rating, esto es muy mejorable, usé este ejemplo para referencia
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "star", tint = Color(0xFFFFD600))
                Icon(imageVector = Icons.Default.Star, contentDescription = "star", tint = Color(0xFFFFD600))
                Icon(imageVector = Icons.Default.Star, contentDescription = "star", tint = Color(0xFFFFD600))
                Icon(imageVector = Icons.Default.Star, contentDescription = "star", tint = Color(0xFFFFD600))
                Icon(imageVector = Icons.Default.Star, contentDescription = "star_border", tint = Color(0xFFBDBDBD))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${user.ratingPercent}%", color = GrayText)
            }
        }
    }
}

//acá se customiza la Fila de Perfil que aparece en la WorkProfileCard
@Composable
fun ProfileRow(label: String, value: String, leading: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = leading, contentDescription = label, tint = Color.Black)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = GrayText)
            Text(value, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

// Acá se manipula la lista de proyectos que contiene el usuario
@Composable
fun MediaProjectList(projects: List<Project>) {
    if (projects.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no hay proyectos multimedia", color = GrayText)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(projects) { p ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEEEEEE)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = "preview")
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.description, color = GrayText, maxLines = 2)
                    }
                    IconButton(onClick = { /* Reproducir proyecto del usuario */ }) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
            }
        }
    }
}

// Datos de prueba para ver cómo queda la pantalla
fun sampleUser() = UserProfile(
    name = "Alambre González",
    instrument = "Guitarra, voz",
    genres = "Rock, Country, Shuffle",
    location = "Gregorio de Laferrere, La Matanza",
    ratingPercent = 85
)
// Idem anterior
fun sampleProjects(): List<Project> = List(8) {
    Project(
        id = "p$it",
        title = "Proyecto ${it + 1}",
        description = listOf(
            "Backing Track Shuffle",
            "Backing Track Bateria 4/4",
            "Base Ritmica Funky Only Guitar",
            "Demo Shoegaze sin Bajo",
            "Patrón Ritmico Bateria Soul",
            "Backing Track 3/4 Bateria Stoner",
            "Yeite melódico en Si bemol - Guitarra",
            "Demo Sintetizador Ambient + Guitarra"
        ).random()
    )
}

@Composable
fun CompactSymmetricButtons(
    modifier: Modifier = Modifier,
    leftLabel: String = "Seguidores",
    rightLabel: String = "Compartir perfil",
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 360.dp) // Este ancho de contenedor es para controlar el comportamiento en pantallas grandotas
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val buttonModifier = Modifier
                .height(35.dp)
                .widthIn(min = 155.dp) // Se supone que asegura el mismo tamaño MINIMO para ambos botones...
            OutlinedButton(
                onClick = onLeftClick,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFB8C00) // Hay que ver el tema del MaterialTheme, estos colores no están bien
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(text = leftLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onRightClick,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFB8C00)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(text = rightLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


