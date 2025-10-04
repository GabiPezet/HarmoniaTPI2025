package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.components.PreferenceCategory
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.components.PreferenceDialog
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.components.PreferenceItem
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.model.OptionTypePreference
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userPreferencesScreen.util.getStringResource
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    viewModel: DrawerContentViewModel,
    uiState: MenuUiState
) {
    var showDialogOption by remember { mutableStateOf<OptionTypePreference?>(null) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    BackHandler {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "CONFIGURACIÓN",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    fontSize = 24.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
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
    }, containerColor = MaterialTheme.colorScheme.onSecondaryContainer) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp // sombra
                ),
                shape = RoundedCornerShape(12.dp)
            ) {


                PreferenceCategory(title = "Preferencias generales") {
                    PreferenceItem(
                        title = getStringResource(OptionTypePreference.THEME.title),
                        subtitle = if (uiState.appTheme == AppTheme.DARK)
                            getStringResource(OptionTypePreference.THEME.items[1])
                        else getStringResource(OptionTypePreference.THEME.items[0]),
                        onClick = {
                            selectedOption =
                                if (uiState.appTheme == AppTheme.DARK)
                                    OptionTypePreference.THEME.items[1]
                                else OptionTypePreference.THEME.items[0]
                            showDialogOption = OptionTypePreference.THEME
                        }
                    )

                }
            }
        }
    }

    // Mostrar diálogo dinámico
    showDialogOption?.let { optionType ->
        PreferenceDialog(
            optionTypePreference = optionType,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it },
            onDismiss = { showDialogOption = null },
            onConfirm = {
                when (optionType) {

                    OptionTypePreference.THEME -> {
                        if (selectedOption == OptionTypePreference.THEME.items[0]) {
                            viewModel.toggleTheme(AppTheme.LIGHT)
                        } else {
                            viewModel.toggleTheme(AppTheme.DARK)
                        }
                    }

                }
                showDialogOption = null
            }
        )
    }
}