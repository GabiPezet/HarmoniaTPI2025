package com.android.harmoniatpi.ui.screens.projectManagementScreen.utils

/**
 * Formatea milisegundos a un string en formato MM:SS.t
 * (Minutos:Segundos.Décimas)
 *
 * @param millis Milisegundos a formatear.
 * @return El string formateado.
 */
fun formatTimeMillis(millis: Long): String {
    // Math.max(0, millis) asegura que no mostremos tiempo negativo si hay algún bug.
    val safeMillis = Math.max(0, millis)

    val totalSeconds = safeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    
    // Obtenemos el dígito de las décimas de segundo
    val tenths = (safeMillis % 1000) / 100 

    // String.format es genial para esto. 
    // %02d = "un número (d) de 2 dígitos, rellena con 0 si es necesario"
    // %d = "un número (d) normal"
    return String.format("%02d:%02d.%d", minutes, seconds, tenths)
}