# 🤖 Sistema de Agentes e Inteligencia Artificial en SIGNIA

Este documento detalla la arquitectura de Inteligencia Artificial, la orquestación de agentes y la estructura técnica actual del proyecto dentro de **SIGNIA**, diseñada para facilitar la comunicación bidireccional mediante el reconocimiento y traducción de la Lengua de Señas Peruana (LSP).

---

## 🏗️ Arquitectura de IA

SIGNIA opera mediante un flujo continuo de agentes y modelos divididos en dos flujos principales:

### 1. Flujo de Señas a Texto y Voz (Reconocimiento)
```
[Cámara del Dispositivo]
         │
         ▼
[Agente de Captura Visio-Espacial] ➔ Extrae Keypoints (Manos, Rostro, Pose)
         │
         ▼
[Modelo de Clasificación LSP]      ➔ Red Neuronal entrenada con señas peruanas
         │
         ▼
[Agente de Post-Procesamiento]    ➔ Conversión de Señas a Texto Natural
         │
         ▼
[Motor Text-to-Speech (TTS)]       ➔ Reproducción por Voz
```

### 2. Flujo de Voz/Texto a Señas (Generación)
```
[Entrada de Voz / Texto]
         │
         ▼
[Agente de Reconocimiento de Voz]  ➔ Whisper / Speech-to-Text integrado
         │
         ▼
[LMM / Procesador NLP]             ➔ Análisis sintáctico y mapeo a glosas LSP
         │
         ▼
[Agente de Animación]              ➔ Renderizado de animaciones en la interfaz
```

---

## 👥 Definición de Agentes y Responsabilidades

### 🛠️ 1. Agente Vision (Captura y Tracking)
*   **Tecnología Base:** Google MediaPipe / TensorFlow Lite optimizado para Android.
*   **Responsabilidad:** Rastrear en tiempo real los puntos clave (keypoints) de ambas manos, articulaciones de los dedos y expresiones faciales de forma local en el dispositivo para garantizar baja latencia.

### 🧠 2. Agente Traductor (Modelo Neuronal LSP)
*   **Tecnología Base:** Arquitectura basada en Transformers para secuencias temporales o redes LSTM.
*   **Responsabilidad:** Tomar las secuencias de coordenadas espaciales entregadas por el Agente Vision e identificar a qué palabra o frase de la Lengua de Señas Peruana corresponde dentro de la biblioteca entrenada.

### 📚 3. Agente Educativo (Aprende LSP)
*   **Tecnología Base:** Motor de Reglas de Progreso y Evaluación Asistida por IA.
*   **Responsabilidad:** 
    *   Gestionar y verificar la correcta ejecución de las señas durante las lecciones prácticas usando la cámara.
    *   Controlar los algoritmos de gamificación (cálculo de XP diario, persistencia de rachas del usuario vinculada a la autenticación).

---

## 🎨 Especificaciones de Interfaz y Diseño de Agentes

Para asegurar una experiencia de usuario fluida, las respuestas e interacciones de los agentes visuales respetan estrictamente la paleta de colores de la aplicación:

*   **Principal (`#2A835F`):** Indicadores de éxito en el reconocimiento, botones de acción de IA y elementos destacados.
*   **Verde Suave (`#E8F5EF`):** Tarjetas de sugerencias del agente, fondos suaves e indicadores de racha/progreso diario.
*   **Blanco (`#FFFFFF`):** Fondo principal y contraste de los componentes visuales.
*   **Gris Oscuro (`#263238`):** Títulos, textos descriptivos y tipografías principales.
*   **Amarillo Racha (`#F4C95D`):** Elementos decorativos y motivacionales de racha de aprendizaje continuo.

---

## 📁 Estructura Actual del Proyecto

El desarrollo del proyecto se ha organizado siguiendo una arquitectura moderna y limpia basada en **Jetpack Compose** para la interfaz de usuario, **Jetpack Navigation** para la orquestación del flujo y **Firebase Auth** para el respaldo de las cuentas.

La estructura actual del código fuente en `app/src/main/java/com/example/app_signia` se compone de:

*   📂 **`MainActivity.kt`**: Punto de entrada de la aplicación móvil. Contiene el contenedor base con el `NavHost` que define e inicializa el flujo de navegación centralizado del sistema (`register` ➔ `login` ➔ `home`).
*   📂 **`RegisterView.kt`**: Pantalla de registro de nuevos usuarios diseñada de acuerdo al manual de marca. Utiliza los colores corporativos (`#2A835F`, `#E8F5EF`, `#263238`), cuenta con validación de contraseñas, checkbox de términos de uso e integración directa con `Firebase Auth` para la creación de cuentas.
*   📂 **`Login.kt`**: Pantalla de inicio de sesión con diseño estilizado que incluye campos de texto con iconos descriptivos, visor/ocultador de contraseña, inicio de sesión mediante credenciales de Firebase, alternativas para inicio de sesión social (Google/Apple) y redirección automática hacia la pantalla principal.
*   📂 **`Home.kt`**: Panel principal que recibe al usuario autenticado. Integra:
    *   **Barra Superior Personalizada:** Muestra el logotipo oficial de SIGNIA y accesos directos de notificación.
    *   **Tarjeta de Progreso General:** Saludo personalizado al usuario registrado ("¡Hola, Ana!") e indicador lineal de XP faltante para subir de nivel.
    *   **Cuadrícula de Funciones Principales:** Acceso directo a los cuatro módulos estipulados de la solución (Reconocimiento de Señas con IA, Traducción de Voz/Texto a LSP, Módulo Educativo "Aprende LSP" y Guía de Uso interactiva).
    *   **Barra de Navegación Inferior (`NavigationBar`):** Menú inferior elegante para saltar entre Inicio, Traductor, Aprende y Perfil.
*   📂 **`ui/theme/`**: Configuración centralizada de estilos visuales, colores (`Color.kt`), tipografías (`Type.kt`) y temas base (`Theme.kt`) de Material 3 aplicados en toda la aplicación.

---

## 📈 Futuras Expansiones del Sistema
*   **Sincronización Cloud-Edge:** Descarga dinámica de sub-modelos de vocabulario según la categoría seleccionada en el módulo educativo.
*   **Traducción Contextual:** Incorporación de modelos de lenguaje pequeños (SLM) locales para corregir la gramática y el orden de las palabras al pasar de glosas de señas a español hablado estructurado.
