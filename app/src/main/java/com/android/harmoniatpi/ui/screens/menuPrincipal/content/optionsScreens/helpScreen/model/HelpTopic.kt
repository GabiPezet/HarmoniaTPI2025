package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.helpScreen.model

data class HelpTopic(
    val title: String,
    val content: String
)

val helpTopicsList = listOf(
    HelpTopic(
        title = "Primeros Pasos y Tu Cuenta",
        content = "¡Bienvenido a HoloJam! Queremos que te sientas como en casa (o en tu estudio).\n\n" +
                "• Registro Sencillo: Puedes crear tu cuenta rápidamente con Google o usando tu correo electrónico a modo de HoloCuenta.\n" +
                "• Tu Identidad Musical: En tu perfil, sube esa foto que te representa y cuéntanos qué tocas o qué géneros te apasionan. Esto ayuda a que otros músicos con tus mismos gustos te encuentren fácilimente.\n" +
                "• Seguridad ante todo: Tu cuenta es tuya y de nadie más. No compartas credenciales ni datos personales."
    ),
    HelpTopic(
        title = "Mi Estudio: Creación sin límites",
        content = "Aquí es donde ocurre la magia.\n\n" +
                "• Nuevo Proyecto: Toca el botón (+) en 'Mis Proyectos' para empezar de cero.\n" +
                "• Herramientas de Ayuda: Antes de grabar, recuerda que tienes un Afinador y un Metrónomo integrados para que tu toma sea perfecta.\n" +
                "• Tipos de Pista: \n" +
                "   - Voz: Activamos automáticamente la cancelación de eco para que suenes nítido.\n" +
                "   - Instrumento: Grabación en alta fidelidad (Hi-Fi) para captar cada detalle.\n" +
                "• Edición Intuitiva: ¿La pista no quedó donde querías? Mantén presionado el círculo central y desliza para moverla. Toca los tres puntos (...) para ajustar volumen, recortar o aplicar efectos como Flanger y Delay."
    ),
    HelpTopic(
        title = "Comunidad y Colaboraciones",
        content = "La música es mejor cuando se comparte. Conecta con otros artistas:\n\n" +
                "• Clonar y Remixar: ¿Te gusta el proyecto de un amigo? Si es público, puedes usar la función 'Clonar' para añadir tu propio instrumento o voz sobre su base. ¡Aparecerá en tu pestaña de Colaboraciones!\n" +
                "• Socializa: Inspira a otros dando 'Like', comentando sus obras o siguiendo a tus artistas favoritos.\n" +
                "• Comparte tu Talento: Al publicar un proyecto, este será visible para toda la comunidad HoloJam. ¡Prepárate para recibir aplausos!"
    ),
    HelpTopic(
        title = "HoloJam Premium",
        content = "Lleva tu producción al siguiente nivel sin compromisos.\n\n" +
                "Con Premium obtienes:\n" +
                "• Pistas Ilimitadas: Olvídate del límite de 4 pistas y crea proyectos increíbles.\n" +
                "• Efectos de Estudio: Acceso a filtros avanzados (Flanger, Paso Alto/Bajo) y herramientas de masterización.\n" +
                "• Sin Interrupciones: Una experiencia fluida y con mayor almacenamiento en la nube.\n\n" +
                "Puedes probarlo y cancelar cuando quieras desde tu perfil. ¡Tú tienes el control!"
    ),
    HelpTopic(
        title = "Solución de dudas frecuentes",
        content = "Todo tiene solución:\n\n" +
                "• ¿No graba el audio? Ve a la Configuración de tu Android y asegúrate de que HoloJam tenga permiso para usar el Micrófono.\n" +
                "• ¿No se escucha nada? Verifica que no tengas el volumen del móvil en silencio o que la pista no esté muteada (icono de altavoz tachado).\n" +
                "• Error al publicar: Generalmente es por una conexión inestable. Intenta conectarte a Wi-Fi y prueba de nuevo.\n" +
                "• Problemas de acceso: Revisa que no tengas las mayúsculas activadas o intenta restablecer tu contraseña."
    ),
    HelpTopic(
        title = "Privacidad y Derechos",
        content = "Tu tranquilidad es nuestra prioridad.\n\n" +
                "Tus obras son 100% tuyas. HoloJam solo obtiene el permiso para reproducirlas dentro de la aplicación para que la comunidad las escuche. Tus datos personales están protegidos y puedes solicitar su eliminación o ejercer tus derechos ARCO contactándonos directamente a soporte."
    )
)