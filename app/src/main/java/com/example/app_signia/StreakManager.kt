package com.example.app_signia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * GESTOR DE RACHA DE APRENDIZAJE (RF17)
 * 
 * - Registra la actividad de aprendizaje del usuario.
 * - Calcula la racha a partir de los días consecutivos con lecciones completadas.
 * - Se reinicia a 1 si se rompe la constancia (más de 1 día de inactividad).
 * - Sincroniza y persiste los datos con la cuenta del usuario en Firebase Firestore.
 */
object StreakManager {
    var currentStreak by mutableIntStateOf(5)
    var maxStreak by mutableIntStateOf(5)
    var lastActiveDateStr by mutableStateOf<String?>(null)
    val activeDates = mutableStateListOf<String>()

    private var isInitialized = false

    init {
        // Inicializar con fechas de ejemplo para los últimos días activos
        if (!isInitialized) {
            val today = LocalDate.now()
            val initialDates = listOf(
                today.minusDays(4).toString(),
                today.minusDays(3).toString(),
                today.minusDays(2).toString(),
                today.minusDays(1).toString(),
                today.toString(),
            )
            activeDates.addAll(initialDates)
            lastActiveDateStr = today.toString()
            isInitialized = true
        }
    }

    /**
     * Carga la racha persistida del usuario desde Firebase Firestore
     */
    fun loadStreakForUser(userId: String?) {
        if (userId.isNullOrBlank()) return

        val db = Firebase.firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val streak = document.getLong("currentStreak")?.toInt() ?: currentStreak
                    val max = document.getLong("maxStreak")?.toInt() ?: maxStreak
                    val lastDate = document.getString("lastActiveDate")
                    val datesList = document.get("activeDates") as? List<*>

                    currentStreak = streak
                    maxStreak = max
                    lastActiveDateStr = lastDate

                    if (datesList != null) {
                        activeDates.clear()
                        datesList.filterIsInstance<String>().forEach { activeDates.add(it) }
                    }

                    // Verificar si la racha se rompió desde el último inicio
                    checkStreakValidity()
                }
            }
            .addOnFailureListener {
                // Si falla o no hay conexión, se mantiene el estado local
            }
    }


    private fun checkStreakValidity() {
        val lastDateStr = lastActiveDateStr ?: return
        try {
            val lastDate = LocalDate.parse(lastDateStr)
            val today = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastDate, today)

            if (daysBetween > 1) {
                // Se rompió la constancia (más de 1 día sin lección)
                currentStreak = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Registra una lección completada hoy y actualiza la racha
     */
    fun recordLessonCompleted(userId: String?, onStreakUpdated: ((Int) -> Unit)? = null) {
        val today = LocalDate.now()
        val todayStr = today.toString()

        if (lastActiveDateStr == null) {
            // Primera vez
            currentStreak = 1
            maxStreak = maxOf(maxStreak, currentStreak)
            lastActiveDateStr = todayStr
            if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
        } else {
            try {
                val lastDate = LocalDate.parse(lastActiveDateStr)
                val daysBetween = ChronoUnit.DAYS.between(lastDate, today)

                when {
                    daysBetween == 0L -> {
                        // Ya completó lección hoy, mantener la racha
                        if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
                    }
                    daysBetween == 1L -> {
                        // Día consecutivo perfecto: incrementa la racha
                        currentStreak += 1
                        maxStreak = maxOf(maxStreak, currentStreak)
                        lastActiveDateStr = todayStr
                        if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
                    }
                    else -> {
                        // Se rompió la constancia: reiniciar racha a 1
                        currentStreak = 1
                        lastActiveDateStr = todayStr
                        if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
                    }
                }
            } catch (_: Exception) {
                currentStreak = 1
                lastActiveDateStr = todayStr
                if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
            }
        }

        onStreakUpdated?.invoke(currentStreak)

        // Guardar en Firestore si el usuario está autenticado
        if (!userId.isNullOrBlank()) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "currentStreak" to currentStreak,
                "maxStreak" to maxStreak,
                "lastActiveDate" to lastActiveDateStr,
                "activeDates" to activeDates.toList()
            )
            db.collection("users").document(userId)
                .set(data, SetOptions.merge())
        }
    }

    /**
     * Verifica si una fecha específica tuvo actividad
     */
    fun isDateActive(date: LocalDate): Boolean {
        return activeDates.contains(date.toString())
    }
}
