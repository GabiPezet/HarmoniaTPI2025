package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components.*
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import com.android.harmoniatpi.ui.core.utils.PermissionRequester
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailProfile(
    viewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    innerPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    onGoToStudio: () -> Unit
) {
    val context = LocalContext.current
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val userPhotoPath by viewModel.userPhotoPath.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(ProfileTab.WORK.ordinal) }
    val tabs = ProfileTab.entries
    var isEditingName by remember { mutableStateOf(false) }
    var name by remember(uiState.userName) { mutableStateOf(uiState.userName) }
    var requestCameraPermission by remember { mutableStateOf(false) }
    val contactData by viewModel.contactData.collectAsState()


    val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            val picturesDir = File(context.filesDir, "pictures")
            val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")
            viewModel.saveUserPhoto(imageFile.absolutePath)
        }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val picturesDir = File(context.filesDir, "pictures")
            if (!picturesDir.exists()) picturesDir.mkdirs()
            val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                imageFile.outputStream().use { output -> input.copyTo(output) }
            }
            viewModel.saveUserPhoto(imageFile.absolutePath)
        }
    }
    fun createImageUri(context: Context): Uri {
        val picturesDir = File(context.filesDir, "pictures")
        if (!picturesDir.exists()) picturesDir.mkdirs()
        val imageFile = File(picturesDir, "profile_photo_${userPhotoPath.version}.jpg")
        if (!imageFile.exists()) imageFile.createNewFile()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    BackHandler {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    Scaffold(
        modifier = Modifier.testTag("UserDetailProfile"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding)
        ) {

            ProfileHeader(
                uiState = uiState,
                userPhotoPath = userPhotoPath.path,
                isEditingName = isEditingName,
                nameValue = name,
                onNameChange = { name = it },
                onEditPhotoClick = { showSheet = true },
                onEditNameToggle = { isEditingName = !isEditingName },
                onSaveName = {
                    if (name.isNotBlank()) {
                        viewModel.updateUserName(name)
                        viewModel.updateUserPreferences()
                        isEditingName = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))


            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)) }
            ) {
                tabs.forEachIndexed { index, tab ->

                    val (icon, label) = when (tab) {
                        ProfileTab.FRIENDS -> Icons.Outlined.People to "Red"
                        ProfileTab.MEDIA -> Icons.Outlined.VideoLibrary to "Media"
                        ProfileTab.WORK -> Icons.Outlined.Person to "Datos"
                        ProfileTab.CONTACT -> Icons.Outlined.ContactMail to "Contacto"
                    }

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {

                val contentModifier = Modifier.padding(horizontal = 16.dp)

                AnimatedContent(
                    targetState = tabs[selectedTabIndex],
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        ProfileTab.WORK -> Box(contentModifier) { WorkProfileCard(uiState, viewModel) }
                        ProfileTab.MEDIA -> MediaProjectList(
                            projects = uiState.projectsList.filter { it.isPublished },
                            currentlyPlayingId = uiState.currentlyPlayingProjectId,
                            isAudioPlaying = uiState.isAudioPlaying,
                            onPlayClick = { project ->
                                viewModel.onPlayProjectClicked(project)
                            },
                            onGoToStudio = onGoToStudio
                        )
                        ProfileTab.CONTACT -> Box(contentModifier) {
                            ContactProfileCard(contactData, { viewModel.updateContactInfo(it) })
                        }
                        ProfileTab.FRIENDS -> FriendsScreen(uiState)
                    }
                }
            }
        }
    }


    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Actualizar foto de perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.width(40.dp).padding(bottom = 8.dp))

                Button(
                    onClick = {
                        requestCameraPermission = true
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar foto")
                }

                OutlinedButton(
                    onClick = {
                        pickImageLauncher.launch("image/*")
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
            }
        }
    }

    if (requestCameraPermission) {
        PermissionRequester(
            permission = Manifest.permission.CAMERA,
            rationaleRes = R.string.camera_rationale,
            permanentlyDeniedRes = R.string.camera_denied_msg,
            onGranted = {
                val uri = createImageUri(context)
                photoUri = uri
                takePictureLauncher.launch(uri)
                requestCameraPermission = false
            },
            onDialogDismiss = { requestCameraPermission = false }
        )
    }
}

@Composable
fun ProfileHeader(
    uiState: MenuUiState,
    userPhotoPath: String,
    isEditingName: Boolean,
    nameValue: String,
    onNameChange: (String) -> Unit,
    onEditPhotoClick: () -> Unit,
    onEditNameToggle: () -> Unit,
    onSaveName: () -> Unit
) {

    val imageModel = remember(userPhotoPath, uiState.userPhotoPathRemote) {
        when {
            userPhotoPath.isNotBlank() && File(userPhotoPath).exists() -> File(userPhotoPath)
            uiState.userPhotoPathRemote.isNotBlank() -> uiState.userPhotoPathRemote
            else -> null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageModifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)

            if (imageModel != null) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Foto de perfil",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.holojamperfildefaultblackmode),
                    placeholder = painterResource(id = R.drawable.holojamperfildefaultblackmode)
                )
            } else {
                Box(
                    modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onEditPhotoClick,
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = 4.dp, y = 4.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isEditingName) {
            OutlinedTextField(
                value = nameValue,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = onSaveName) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSaveName() })
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onEditNameToggle() }
                    .padding(8.dp)
            ) {
                Text(
                    text = uiState.userName.ifEmpty { "Usuario" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Text(
                text = uiState.userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

