package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    viewModel: DrawerContentViewModel,
    uiState: MenuUiState,
    innerPadding: PaddingValues
) {
    BackHandler {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Preferencias de Usuario",
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
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
                .verticalScroll(rememberScrollState())
        ) {

            // Sección de Preferencias Generales
            PreferenceCategory(title = stringResource(R.string.preferencias_generales)) {

                PreferenceItem(
                    icon = Icons.Default.Brightness6,
                    title = stringResource(R.string.theme),
                    subtitle = if (uiState.appTheme.value) {
                        stringResource(R.string.oscuro)
                    } else {
                        stringResource(R.string.claro)
                    },
                    onClick = { viewModel.toggleTheme() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreferenceCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        /* Card(
             modifier = Modifier.fillMaxWidth(),
             shape = MaterialTheme.shapes.medium,
             colors = CardDefaults.cardColors(
                 containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
             ),
             elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
         ) {
             Column {
                 content()
             }
         }*/
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: Any,
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono
        Box(
            modifier = Modifier
                .size(40.dp)
            /* .background(
                 color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                 shape = CircleShape
             )*/,
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                is Painter ->
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
            )
        }

        // Contenido adicional (opcional)
        trailingContent?.invoke()

        if (trailingContent == null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}