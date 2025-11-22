package com.android.harmoniatpi.ui.screens.projectManagementScreen.model

/**
 * Define los diferentes tipos de contenido que puede mostrar
 * el ModalBottomSheet en la pantalla de gestión del proyecto.
 */
sealed class BottomSheetContent {
    /**
     * Para el menú principal de "Añadir pista"
     */
    object AddTrackMenu : BottomSheetContent()

    /**
     * Para las opciones de una pista específica
     */
    data class EditVolume(val track: TrackUi) : BottomSheetContent()
    data class RenameTrack(val track: TrackUi) : BottomSheetContent()
    data class TrackEffects(val track: TrackUi) : BottomSheetContent()

    /**
     * Para el diálogo de "En desarrollo"
     */
    object InDevelopment : BottomSheetContent()

    /**
     * Para las configuraciones del metrónomo (BPM, encendido/apagado)
     */
    object MetronomeSettings : BottomSheetContent()
}