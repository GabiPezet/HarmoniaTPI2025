package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.R
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util.rememberMicrophoneLauncher

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommunitySearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    var internalQuery by remember { mutableStateOf(searchQuery) }
    val focusRequester = remember { FocusRequester() }

    val launchVoiceSearch = rememberMicrophoneLauncher { spokenText ->
        val cleanedText =
            if (spokenText.all { it.isDigit() || it.isWhitespace() }) {
                spokenText.replace(" ", "")
            } else {
                spokenText
            }

        internalQuery = cleanedText
        onQueryChange(cleanedText)
    }

    BackHandler {
        if (internalQuery.isNotEmpty()) {
            internalQuery = ""
            onQueryChange("")
        } else {
            onClose()
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(tween(250)) + fadeIn(tween(250)),
        exit = slideOutVertically(tween(250)) + fadeOut(tween(250))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = internalQuery,
                onValueChange = {
                    internalQuery = it
                    onQueryChange(it)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    AnimatedContent(
                        targetState = internalQuery.isNotEmpty(),
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInHorizontally(tween(250)))
                                .togetherWith(
                                    fadeOut(tween(250)) + slideOutHorizontally(tween(250))
                                )
                        }
                    ) { hasText ->

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasText) {
                                IconButton(onClick = {
                                    internalQuery = ""
                                    onQueryChange("")
                                }) {
                                    Icon(
                                        modifier = Modifier.size(26.dp),
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            } else {
                                IconButton(onClick = { launchVoiceSearch() }) {
                                    Icon(
                                        modifier = Modifier.size(30.dp),
                                        painter = painterResource(R.drawable.ic_microphoneicon),
                                        contentDescription = "Buscar por voz"
                                    )
                                }
                            }
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (internalQuery.isEmpty()) 8.dp else 16.dp)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(8.dp)
            )

            AnimatedVisibility(
                visible = internalQuery.isEmpty(),
                enter = fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it },
                exit = fadeOut(tween(250)) + slideOutHorizontally(tween(250)) { it }
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        modifier = Modifier
                            .size(26.dp)
                            .padding(end = 4.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
        }
    }
}

