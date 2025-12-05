# HoloJam 🎵

¡Buenas\! Bienvenidos al repositorio de **HoloJam**.

Esta es una aplicación nativa para Android que nace con una idea simple pero ambiciosa: combinar un estudio de grabación de bolsillo (DAW) con una red social. Queremos que no solo puedas grabar y mezclar tus ideas musicales en el celu, sino también compartirlas al toque con una comunidad, recibir feedback y colaborar con otros músicos.

> **Aclaración técnica:** Si bien el producto se llama **HoloJam**, van a ver que en el código (namespace y package) todavía figura como `com.android.harmoniatpi`. Es el nombre original del proyecto ("Harmonia") y quedó por cuestiones de legado.

## ¿De qué se trata el proyecto?

Básicamente, unimos dos mundos:

1.  **Audio Engine:** Un set de herramientas para grabar voces o instrumentos, editar pistas (cortar, mutear, cambiar volumen), aplicar efectos en tiempo real (distorsión, delay, etc) y exportar la mezcla.
2.  **Social Layer:** Un feed donde podés publicar tus proyectos, escuchar los de otros, comentar, dar "me gusta" y seguir a tus artistas favoritos.

## Un vistazo a la App

| Inicio / Feed | Estudio de Grabación | Perfil de Usuario | Efectos de Audio |
|:---:|:---:|:---:|:---:|
| <img width="112" height="305" alt="image" src="https://github.com/user-attachments/assets/71b37d10-548c-4396-bf60-f706410f6387" /> | <img width="112" height="305" alt="image" src="https://github.com/user-attachments/assets/913cc1b3-4fe9-4000-977c-448a87f47778" />  |<img width="112" height="305" alt="image" src="https://github.com/user-attachments/assets/8be1f848-6963-4431-b53b-8f72474dd1ed" /> | <img width="112" height="305" alt="image" src="https://github.com/user-attachments/assets/19bd0626-9f4e-4d0f-8022-53c245e9252b" />|


## Funcionalidades Clave

Acá les contamos qué es lo que ya está funcionando:

### Estudio de Audio

  * **Multitrack:** Grabación y mezcla de múltiples pistas.
  * **Edición:** Herramientas básicas para recortar audio, ajustar ganancias y silenciar canales.
  * **Efectos (DSP):** Procesamiento de señal usando TarsosDSP para efectos como Distorsión, Tremolo, Delay, Flanger y filtros de ecualización.
  * **Utilidades:** Incluye un afinador cromático y un metrónomo para no perder el tempo.
  * **Motor de Audio:** Usamos `ExoPlayer` para el streaming y reproducción, y `LAME` para convertir todo a MP3 cuando hace falta.

### Comunidad

  * **Feed Social:** Scroll vertical para descubrir música nueva.
  * **Interacción:** Sistema de comentarios y likes en tiempo real.
  * **Colaboración:** La idea es que si un proyecto es público, puedas "forkearlo" o tomarlo de base para hacer tu propia versión (remix).

### Gestión y Monetización

  * **Suscripción Premium:** Implementamos una pasarela de pagos con **MercadoPago** para gestionar suscripciones.
  * **Modo Offline/Online:** Usamos Room para guardar datos locales y que la app no se rompa si te quedás sin internet, sincronizando con Firebase cuando volvés a conectar.

## Tecnologías (El músculo)

El proyecto está escrito 100% en **Kotlin** y tratamos de usar lo último de Android para mantener el código limpio y moderno:

  * **Arquitectura:** Clean Architecture + MVVM. Separamos bien la lógica de negocio de la UI.
  * **Interfaz de Usuario:** Jetpack Compose (Material 3). Nada de XML viejos.
  * **Inyección de Dependencias:** Dagger Hilt.
  * **Manejo de Datos:**
      * **Local:** Room Database & DataStore.
      * **Nube:** Firebase (Firestore, Auth, Storage, Crashlytics).
      * **Red:** Retrofit + Gson.
  * **Imágenes:** Coil.
  * **Hardware:** CameraX y ML Kit.

## ¿Cómo levantar el proyecto?

Si querés clonar el repo y probarlo en tu Android Studio, seguí estos pasos:

1.  **Prerrequisitos:** Necesitás Android Studio (recomendado Ladybug o superior) y JDK 17.

2.  **Configuración de Firebase:**

      * El proyecto necesita el archivo `google-services.json`. Como es un archivo sensible, no está en el repo. Vas a tener que crear tu propio proyecto en Firebase, descargarlo y ponerlo en la carpeta `/app/`.

3.  **Variables de Entorno (Importante):**

      * Para que compile la parte de pagos, necesitás un token de MercadoPago.
      * Creá un archivo `local.properties` en la raíz del proyecto (si no lo tenés) y agregá esto:
        ```properties
        sdk.dir=/Ruta/a/tu/Android/Sdk
        MP_ACCESS_TOKEN=tu_token_de_acceso_de_mercado_pago
        ```
      * Sin esa línea del token, Gradle te va a tirar error o te va a poner un string vacío.

4.  **Compilar:** Dale al botón de *Run* o ejecutá `./gradlew assembleDebug` en la terminal.

## Estructura de Carpetas

Tratamos de ser ordenados para que sea fácil navegar el código:

  * `core/`: Utilidades generales y servicios que usa toda la app.
  * `data/`: Acá está la implementación de los repositorios, las llamadas a la API y la base de datos Room.
  * `domain/`: Las reglas de negocio y los Casos de Uso. Es la parte "pura" de la app.
  * `ui/`: Todo lo visual. Pantallas, componentes reutilizables y los ViewModels.
  * `di/`: Los módulos de Hilt para inyectar dependencias.

## ¿Querés colaborar?

¡Buenísimo\! Si encontrás un bug o se te ocurre una mejora, sentite libre de abrir un **Issue** o mandar un **Pull Request**. La idea es aprender entre todos, así que cualquier corrección o sugerencia de código es bienvenida (¡nos sirve mucho para crecer\!).

## Licencia

Este proyecto se distribuye bajo la licencia MIT.

-----

*Hecho con dedicación desde Argentina.*
