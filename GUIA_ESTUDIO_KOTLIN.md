# 📖 Guía de Estudio: Kotlin para SIGNIA

> **Enfoque:** Aprender Kotlin usando ejemplos reales del proyecto SIGNIA.
> **Nivel:** Principiante a Intermedio
> **Tiempo estimado:** 2-3 semanas (1-2 horas diarias)

---

## 📋 Índice

1. [Sintaxis Básica y Variables](#1-sintaxis-básica-y-variables)
2. [Funciones](#2-funciones)
3. [Clases y POO](#3-clases-y-poo)
4. [Colecciones](#4-colecciones)
5. [Corrutinas (Coroutines)](#5-corrutinas-coroutines)
6. [Flows y StateFlow](#6-flows-y-stateflow)
7. [Clases Selladas (Sealed Classes)](#7-clases-selladas-sealed-classes)
8. [Funciones de Alto Orden](#8-funciones-de-alto-orden)
9. [Ejercicios Prácticos](#9-ejercicios-prácticos)

---

## 1. Sintaxis Básica y Variables

### `val` vs `var`

```kotlin
// val = inmutable (como const)
val nombre = "SIGNIA"        // No se puede reasignar
val colorPrincipal = "#2A835F"

// var = mutable (puede cambiar)
var nivel = 1
var xp = 0
xp = 100                      // ✅ Sí se puede reasignar
```

**En el proyecto:** En `PracticeModel.kt` verás:
```kotlin
object MockProgressData {
    var totalSignsCompleted = 0   // var: cambia con el progreso
    var currentLevel = 1
    var streakDays = 0
}
```

### Tipos de Datos

```kotlin
val texto: String = "Hola"        // Texto
val entero: Int = 42              // Número entero
val decimal: Double = 9.99        // Decimal
val booleano: Boolean = true      // true/false
val lista: List<String> = listOf("a", "b", "c")  // Lista inmutable
```

### String Templates

```kotlin
val usuario = "Ana"
val nivel = 3
val mensaje = "¡Hola, $usuario! Estás en el nivel $nivel"
// Resultado: "¡Hola, Ana! Estás en el nivel 3"

// Con expresiones:
val xpFaltante = 100 - 40
val progreso = "Te faltan ${100 - 40} XP para subir de nivel"
```

**En el proyecto:** En `Home.kt`:
```kotlin
Text("¡Hola, $userName!")
Text("Te faltan ${xpToNextLevel} XP para subir de nivel")
```

### Null Safety

```kotlin
// Kotlin evita NullPointerException con tipos nullable
var nombre: String? = null    // Puede ser null
var apellido: String = "Pérez" // No puede ser null

// Safe call (?.)
val longitud = nombre?.length   // null si nombre es null

// Elvis operator (?:)
val mostrar = nombre ?: "Invitado"  // "Invitado" si nombre es null

// Non-null assertion (!!) - ¡CUIDADO! Solo si estás seguro
val forzado = nombre!!.length   // Lanza excepción si es null
```

---

## 2. Funciones

### Función Básica

```kotlin
// Sintaxis: fun nombre(parámetro: Tipo): tipoRetorno { ... }
fun saludar(nombre: String): String {
    return "¡Hola, $nombre!"
}

// Función de una sola expresión (sin llaves)
fun cuadrado(x: Int): Int = x * x

// Función sin retorno (Unit = void)
fun imprimirMensaje(msg: String) {
    println(msg)
}
```

### Parámetros con Valor por Defecto

```kotlin
fun crearUsuario(
    nombre: String,
    email: String,
    esAdmin: Boolean = false   // Valor por defecto
) {
    // ...
}

// Uso:
crearUsuario("Ana", "ana@email.com")           // esAdmin = false
crearUsuario("Ana", "ana@email.com", true)     // esAdmin = true
```

### Named Arguments (Argumentos Nombrados)

```kotlin
crearUsuario(
    email = "ana@email.com",
    nombre = "Ana",
    esAdmin = true
)
// El orden no importa cuando usas nombres
```

**En el proyecto:** En `RegisterView.kt`:
```kotlin
fun validatePassword(password: String): Boolean {
    return password.length >= 8
}
```

---

## 3. Clases y POO

### Clase Básica

```kotlin
class Usuario(
    val nombre: String,      // Propiedad inmutable
    var nivel: Int = 1       // Propiedad mutable con valor por defecto
) {
    // Propiedad calculada
    val esNivelAlto: Boolean
        get() = nivel >= 5

    // Método
    fun subirNivel() {
        nivel++
        println("$nombre subió al nivel $nivel")
    }
}

// Uso:
val usuario = Usuario("Ana")
usuario.subirNivel()  // "Ana subió al nivel 2"
```

### Data Class

```kotlin
// Data class: automáticamente genera equals(), hashCode(), toString(), copy()
data class Ejercicio(
    val id: Int,
    val palabra: String,
    val imagen: String,
    val completado: Boolean = false
)

// Uso:
val ejercicio = Ejercicio(1, "Hola", "hola.png")
val copia = ejercicio.copy(completado = true)  // Copia con modificación
println(ejercicio)  // Ejercicio(id=1, palabra=Hola, imagen=hola.png, completado=false)
```

**En el proyecto:** En `PracticeModel.kt`:
```kotlin
data class PracticeExercise(
    val id: Int,
    val word: String,
    val imageRes: Int,
    val isCompleted: Boolean = false
)
```

### Object (Singleton)

```kotlin
// Object: existe una única instancia en toda la app
object MockProgressData {
    var totalSignsCompleted = 0
    var currentLevel = 1
    var streakDays = 0

    fun resetProgress() {
        totalSignsCompleted = 0
        currentLevel = 1
        streakDays = 0
    }
}

// Uso (no necesitas crear instancia):
MockProgressData.totalSignsCompleted = 5
```

### Enum Class

```kotlin
enum class Modulo {
    RECONOCIMIENTO,
    TRADUCCION,
    APRENDE,
    GUIA
}

// Uso:
val modulo = Modulo.RECONOCIMIENTO
when (modulo) {
    Modulo.RECONOCIMIENTO -> println("Reconocimiento de señas")
    Modulo.TRADUCCION -> println("Traducción de voz")
    // ...
}
```

---

## 4. Colecciones

### Listas

```kotlin
val modulos = listOf("Reconocimiento", "Traducción", "Aprende", "Guía")

// Acceso por índice
modulos[0]           // "Reconocimiento"
modulos.first()      // "Reconocimiento"
modulos.last()       // "Guía"
modulos.size         // 4

// Recorrido
for (modulo in modulos) {
    println(modulo)
}

// Funciones útiles
modulos.filter { it.startsWith("A") }     // ["Aprende"]
modulos.map { it.uppercase() }            // ["RECONOCIMIENTO", ...]
modulos.contains("Aprende")               // true
```

### MutableList

```kotlin
val ejercicios = mutableListOf<String>()
ejercicios.add("Hola")
ejercicios.add("Adiós")
ejercicios.remove("Hola")
```

### Mapas

```kotlin
val progreso = mapOf(
    "nivel" to 3,
    "xp" to 150,
    "racha" to 7
)

progreso["nivel"]     // 3
progreso.getOrDefault("monedas", 0)  // 0 (si no existe)
```

**En el proyecto:** En `PracticeModel.kt`:
```kotlin
val exercises = listOf(
    PracticeExercise(1, "Hola", R.drawable.hola),
    PracticeExercise(2, "Adiós", R.drawable.adios),
    PracticeExercise(3, "Gracias", R.drawable.gracias)
)
```

---

## 5. Corrutinas (Coroutines)

Las corrutinas permiten ejecutar código de forma asíncrona sin bloquear la interfaz.

### Conceptos Básicos

```kotlin
import kotlinx.coroutines.*

// launch: ejecuta y no espera resultado
fun cargarDatos() {
    GlobalScope.launch {
        // Código asíncrono
        delay(1000)  // Espera 1 segundo sin bloquear
        println("Datos cargados")
    }
}

// async/await: ejecuta y espera resultado
suspend fun obtenerNivel(): Int {
    val resultado = async {
        delay(500)
        5  // Retorna este valor
    }
    return resultado.await()  // Espera y obtiene el valor
}
```

### ViewModel + Corrutinas

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PracticeViewModel : ViewModel() {
    fun completarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            // Esto se cancela si el ViewModel se destruye
            // Ideal para llamadas a Firebase
            delay(1000)  // Simula guardado en Firebase
            MockProgressData.totalSignsCompleted++
        }
    }
}
```

### suspend fun

```kotlin
// suspend: solo puede ejecutarse dentro de una corrutina
suspend fun guardarProgresoEnFirebase(progreso: Int): Boolean {
    delay(2000)  // Simula llamada a red
    return true  // Éxito
}

// Uso desde una corrutina:
viewModelScope.launch {
    val exito = guardarProgresoEnFirebase(100)
    if (exito) {
        println("Progreso guardado")
    }
}
```

---

## 6. Flows y StateFlow

Flow permite emitir múltiples valores de forma asíncrona (como un stream).

### StateFlow (Estado Reactivo)

```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PracticeViewModel : ViewModel() {
    // Estado privado mutable
    private val _progreso = MutableStateFlow(0)
    
    // Estado público inmutable (la UI observa este)
    val progreso: StateFlow<Int> = _progreso.asStateFlow()

    fun incrementarProgreso() {
        _progreso.value = _progreso.value + 1
    }
}
```

### En Jetpack Compose

```kotlin
@Composable
fun ProgresoCard(viewModel: PracticeViewModel) {
    // collectAsState: observa los cambios del StateFlow
    val progreso by viewModel.progreso.collectAsState()

    // La UI se actualiza automáticamente cuando progreso cambia
    Text("Señas completadas: $progreso")
}
```

**En el proyecto:** En `PracticeScreen.kt`:
```kotlin
@Composable
fun PracticeScreen() {
    val exercises = remember { MockProgressData.exercises }
    // ...
}
```

### remember + mutableStateOf

```kotlin
@Composable
fun Contador() {
    // remember: mantiene el estado entre recomposiciones
    var contador by remember { mutableStateOf(0) }

    Button(onClick = { contador++ }) {
        Text("Clicks: $contador")
    }
}
```

---

## 7. Clases Selladas (Sealed Classes)

Las sealed classes son útiles para representar un conjunto finito de estados.

```kotlin
sealed class PantallaState {
    object Loading : PantallaState()
    data class Success(val datos: List<String>) : PantallaState()
    data class Error(val mensaje: String) : PantallaState()
}

// Uso con when (exhaustivo - debe cubrir todos los casos)
fun mostrarEstado(estado: PantallaState) {
    when (estado) {
        is PantallaState.Loading -> println("Cargando...")
        is PantallaState.Success -> println("Datos: ${estado.datos}")
        is PantallaState.Error -> println("Error: ${estado.mensaje}")
    }
}
```

---

## 8. Funciones de Alto Orden

Funciones que reciben otras funciones como parámetros o las retornan.

```kotlin
// Función que recibe un lambda
fun operar(a: Int, b: Int, operacion: (Int, Int) -> Int): Int {
    return operacion(a, b)
}

// Uso:
val suma = operar(5, 3) { x, y -> x + y }      // 8
val producto = operar(5, 3) { x, y -> x * y }  // 15

// Funciones de extensión
String.esPalindromo(): Boolean {
    return this == this.reversed()
}

"anilina".esPalindromo()  // true
```

### Scope Functions (apply, let, run, also, with)

```kotlin
// apply: configura un objeto y retorna el objeto
val usuario = Usuario("Ana").apply {
    nivel = 5
    // ...
}

// let: ejecuta un bloque y retorna el resultado del bloque
val longitud = nombre?.let {
    it.length
} ?: 0

// also: ejecuta un bloque y retorna el objeto original
val ejercicio = Ejercicio(1, "Hola", "hola.png").also {
    println("Creado: ${it.palabra}")
}

// run: combinación de let y apply
val resultado = usuario.run {
    subirNivel()
    "Nivel actual: $nivel"
}
```

---

## 9. Ejercicios Prácticos

### Ejercicio 1: Variables y Tipos
```kotlin
// Crea variables para representar:
// - Nombre de usuario (no cambia)
// - XP actual (cambia)
// - Nivel (cambia)
// - ¿Es premium? (no cambia)
// - Lista de módulos favoritos
```

### Ejercicio 2: Funciones
```kotlin
// Crea una función que:
// - Reciba una palabra y devuelva su longitud
// - Reciba una contraseña y valide que tenga al menos 8 caracteres
// - Reciba un nivel y devuelva el XP necesario para el siguiente nivel (nivel * 100)
```

### Ejercicio 3: Clases
```kotlin
// Crea una data class "Lección" con:
// - id, título, descripción, completada, duración en minutos
// Crea una lista de 3 lecciones
// Filtra las lecciones no completadas
```

### Ejercicio 4: Colecciones
```kotlin
// Dada la lista de ejercicios del proyecto:
// - Cuenta cuántos están completados
// - Obtén la lista de palabras de los ejercicios pendientes
// - Crea un mapa de "palabra" a "está completado"
```

### Ejercicio 5: Corrutinas
```kotlin
// Crea una función suspend que simule guardar el progreso en Firebase
// (usa delay(2000) y retorna true)
// Luego úsala dentro de viewModelScope.launch
```

### Ejercicio 6: StateFlow
```kotlin
// Crea un ViewModel con un StateFlow que represente el estado de la práctica:
// - NoIniciada
// - EnProgreso(actual: Int, total: Int)
// - Completada
// La UI debe mostrar el estado actual
```

---

## 🎯 Consejos de Estudio

1. **Código primero:** No solo lees — escribe y ejecuta cada ejemplo
2. **Proyecto real:** Relaciona cada concepto con archivos de SIGNIA
3. **Modifica y experimenta:** Cambia los ejemplos y predice el resultado antes de ejecutar
4. **Documentación oficial:** [kotlinlang.org](https://kotlinlang.org/docs/)
5. **Playground:** Usa [play.kotlinlang.org](https://play.kotlinlang.org) para practicar sin Android Studio

---

## 📚 Recursos Adicionales

| Recurso | Enlace |
|---|---|
| Documentación oficial | https://kotlinlang.org/docs/ |
| Kotlin Playground | https://play.kotlinlang.org/ |
| Corrutinas | https://kotlinlang.org/docs/coroutines-guide.html |
| Compose | https://developer.android.com/jetpack/compose |
| Cursos gratuitos | https://developer.android.com/courses |

---

*Guía creada para el proyecto SIGNIA — Lengua de Señas Peruana*
