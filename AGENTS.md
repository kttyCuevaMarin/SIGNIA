# 🤖 Sistema de Agentes e Inteligencia Artificial en SIGNIA

Este documento detalla la arquitectura de Inteligencia Artificial y la orquestación de agentes dentro de **SIGNIA**, diseñada para facilitar la comunicación bidireccional mediante el reconocimiento y traducción de la Lengua de Señas Peruana (LSP).

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
    *   Controlar los algoritmos de gamificación (cálculo de XP diario, persistencia de rachas del usuario en Firebase Firestore).

---

## 🎨 Especificaciones de Interfaz y Diseño de Agentes

Para asegurar una experiencia de usuario fluida, las respuestas e interacciones de los agentes visuales respetan estrictamente la paleta de colores de la aplicación:

*   **Principal (`#2A835F`):** Indicadores de éxito en el reconocimiento y botones de acción de IA.
*   **Verde Suave (`#E8F5EF`):** Tarjetas de sugerencias del agente y áreas de feedback.
*   **Amarillo Racha (`#F4C95D`):** Elementos relacionados con el progreso continuo y alertas motivacionales.

---

## 📈 Futuras Expansiones del Sistema
*   **Sincronización Cloud-Edge:** Descarga dinámica de sub-modelos de vocabulario según la categoría seleccionada en el módulo educativo.
*   **Traducción Contextual:** Incorporación de modelos de lenguaje pequeños (SLM) locales para corregir la gramática y el orden de las palabras al pasar de glosas de señas a español hablado estructurado.
