package com.android.harmoniatpi.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.harmoniatpi.MainActivity

@SuppressLint("SourceLockedOrientationActivity")
/* Se llama al principio de cualquier composable para definir la orientación
* Recibe 3 parametros , el contexto de la pantalla ,
*  la orientación (true = vertical , false = horizontal)
* y si se quiere ocultar el status bar que por defecto se muestra */
@Composable
fun SetOrientationScreen(
    context: Context,
    orientation: Boolean,
    hideStatusBar: Boolean = false,
) {
    val activity = context as? Activity
    val window = (context as? MainActivity)?.window

    if (activity != null) {
        if (orientation) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    if (hideStatusBar) {
        WindowInsetsControllerCompat(window!!, window.decorView).run {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}