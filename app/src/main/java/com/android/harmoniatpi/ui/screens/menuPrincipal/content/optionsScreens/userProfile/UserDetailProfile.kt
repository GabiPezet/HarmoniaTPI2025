package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.CompactSymmetricButtons
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.MediaProjectList
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.ProfileNavButton
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.ProfileTab
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.WorkProfileCard
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailProfile(
    viewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    innerPadding: PaddingValues,
) {
    val context = LocalContext.current
    Log.i("FirebaseStorage UserDetailProfile", "URL: $uiState.userPhotoPathRemote")
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val userPhotoPath by viewModel.userPhotoPath.collectAsState()
    var selectedTab by remember { mutableStateOf(ProfileTab.WORK) }
    var isEditing by remember { mutableStateOf(false) }
    var name by remember(uiState.userName) { mutableStateOf(uiState.userName) }

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
            context, "${context.packageName}.fileprovider", imageFile
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
                        text = "PERFIL", style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ), fontSize = 24.sp
                    )
                }, navigationIcon = {
                    IconButton(onClick = {
                        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menú anterior",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                if (uiState.userPhotoPathRemote.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.userPhotoPathRemote),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else
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
                            color = MaterialTheme.colorScheme.primary, shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isEditing) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre de usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (name.isNotBlank()) {
                                        viewModel.updateUserName(name)
                                        viewModel.updateUserPreferences()
                                        isEditing = false
                                    }
                                }
                            )
                        )
                        if (name.isBlank()) {
                            Text(
                                text = "El nombre no puede estar vacío",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.updateUserName(name)
                            viewModel.updateUserPreferences()
                            isEditing = false
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Guardar nombre",
                            tint = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = uiState.userName.ifEmpty { "Sin nombre" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nombre",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            CompactSymmetricButtons()
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
                    onClick = { selectedTab = ProfileTab.WORK })
                Spacer(modifier = Modifier.width(24.dp))
                ProfileNavButton(
                    icon = Icons.Default.Movie,
                    label = "Multimedia",
                    selected = selectedTab == ProfileTab.MEDIA,
                    onClick = { selectedTab = ProfileTab.MEDIA })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    ProfileTab.WORK -> WorkProfileCard(uiState = uiState, viewModel = viewModel)
                    ProfileTab.MEDIA -> MediaProjectList(projects = uiState.listProjects)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
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