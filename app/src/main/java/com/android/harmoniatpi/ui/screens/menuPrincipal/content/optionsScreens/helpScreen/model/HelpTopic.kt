package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen.model

data class HelpTopic(
    val title: String,
    val content: String
)

val helpTopicsList = listOf(
    HelpTopic(
        title = "Primeros Pasos y Cuenta",
        content = "• Registro: Puedes usar HoloCuenta o Google Sign In.\n" +
                "• Perfil: Cambia tu foto desde la cámara o galería y completa tus datos (Instrumento, Género) para que otros te encuentren.\n" +
                "• Seguridad: Tu cuenta es personal e intransferible. Protege tus credenciales."
    ),
    HelpTopic(
        title = "Mi Estudio: Grabación y Pistas",
        content = "• Crear Proyecto: Usa el botón (+) en 'Mis Proyectos'.\n" +
                "• Tipos de Pista: 'Grabar Voz' usa cancelación de eco. 'Grabar Instrumento' es audio Hi-Fi sin filtros.\n" +
                "• Edición: Para mover una pista, simplemente toca en el círculo del medio y desliza tu dedo. Usa el menú (3 puntos) para renombrar pista, volumen, efectos o borrar."
    ),
    HelpTopic(
        title = "Comunidad y Colaboraciones",
        content = "• Clonar/Fork: Si un proyecto es público o lo ha subido un amigo, puedes clonarlo para agregar tu versión. Aparecerá en la pestaña 'Colaboraciones'.\n" +
                "• Social: Puedes dar Like, Comentar y Seguir a otros artistas.\n" +
                "• Publicar: Al publicar un proyecto, se genera un post visible para todos."
    ),
    HelpTopic(
        title = "HoloJam Premium",
        content = "Desbloquea pistas ilimitadas (más de 5), efectos exclusivos (Filtros avanzados) y mayor capacidad en la nube. Cancela cuando quieras desde la sección Premium."
    ),
    HelpTopic(
        title = "Solución de Problemas Frecuentes",
        content = "• No puedo grabar: Verifica permisos de micrófono en Configuración de Android.\n" +
                "• Sin audio: Revisa si el volumen del celular está bajo o la pista está silenciada (Mute).\n" +
                "• Error al publicar: Verifica tu conexión a internet.\n" +
                "• Login fallido: Revisa mayúsculas o intenta recuperar contraseña."
    ),
    HelpTopic(
        title = "Privacidad y Derechos",
        content = "Tus obras están protegidas. HoloJam solo tiene licencia para reproducir en la app. Para ejercer derechos ARCO o borrar datos, contáctanos."
    )
)