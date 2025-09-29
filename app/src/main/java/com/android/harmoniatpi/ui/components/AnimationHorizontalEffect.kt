package com.android.harmoniatpi.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimationHorizontalEffect(
    onBackNavigation: () -> Unit = {},
    contentMain: @Composable () -> Unit
) {

    val animationDuration = 300
    var visible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visible = true
    }

    BackHandler {
        visible = false
        coroutineScope.launch {
            delay(animationDuration.toLong())
            onBackNavigation()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(animationSpec = tween(300)),

        exit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(animationSpec = tween(300))
    ) {
        contentMain()
    }


}