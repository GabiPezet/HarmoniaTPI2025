package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.OptionsMenu
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen.model.helpTopicsList
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen.components.HelpTopicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: DrawerContentViewModel
) {
    val context = LocalContext.current

    BackHandler {
        viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AYUDA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.changeOptionsMenu(OptionsMenu.MAIN_CONTENT_SCREEN) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "¿Necesitas ayuda personalizada?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:holojamtpi@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Soporte HoloJam App")
                            }
                            context.startActivity(intent)
                        },
                        ) {
                            Text("Contactar a soporte",
                                color = Color.Yellow,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            items(helpTopicsList) { topic ->
                HelpTopicItem(title = topic.title, content = topic.content)
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}