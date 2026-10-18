package com.example.app_signia

import android.content.Context
import android.content.SharedPreferences
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
 * STREAK MANAGER (RF17)
 */
object StreakManager {
    var currentStreak by mutableIntStateOf(0)
    var maxStreak by mutableIntStateOf(0)
    var lastActiveDateStr by mutableStateOf<String?>(null)
    val activeDates = mutableStateListOf<String>()

    val rachaActual: Int get() = currentStreak
    val rachaMaxima: Int get() = maxStreak

    private var prefs: SharedPreferences? = null

    fun initFromContext(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("signia_streak_prefs", Context.MODE_PRIVATE)
            currentStreak = prefs?.getInt("currentStreak", 0) ?: 0
            maxStreak = prefs?.getInt("maxStreak", 0) ?: 0
            lastActiveDateStr = prefs?.getString("lastActiveDateStr", null)

            val datesSet = prefs?.getStringSet("activeDatesSet", emptySet()) ?: emptySet()
            activeDates.clear()
            activeDates.addAll(datesSet)

            checkStreakValidity()
        }
    }

    fun inicializarDesdeContexto(context: Context) = initFromContext(context)

    private fun saveToLocalPrefs() {
        prefs?.edit()?.apply {
            putInt("currentStreak", currentStreak)
            putInt("maxStreak", maxStreak)
            putString("lastActiveDateStr", lastActiveDateStr)
            putStringSet("activeDatesSet", activeDates.toSet())
            apply()
        }
    }

    fun resetStreakToZeroForNewUser() {
        currentStreak = 0
        maxStreak = 0
        lastActiveDateStr = null
        activeDates.clear()
        saveToLocalPrefs()
    }

    fun reiniciarRachaAZero() = resetStreakToZeroForNewUser()

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

                    activeDates.clear()
                    if (datesList != null) {
                        datesList.filterIsInstance<String>().forEach { activeDates.add(it) }
                    }

                    checkStreakValidity()
                    saveToLocalPrefs()
                }
            }
    }

    fun cargarRachaDeUsuario(userId: String?) = loadStreakForUser(userId)

    fun checkStreakValidity() {
        val lastDateStr = lastActiveDateStr ?: return
        try {
            val lastDate = LocalDate.parse(lastDateStr)
            val today = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastDate, today)

            if (daysBetween > 1) {
                currentStreak = 0
                saveToLocalPrefs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun recordLessonCompleted(userId: String?, onStreakUpdated: ((Int) -> Unit)? = null) {
        val today = LocalDate.now()
        val todayStr = today.toString()

        if (lastActiveDateStr == null) {
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
                        if (currentStreak == 0) currentStreak = 1
                        if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
                    }
                    daysBetween == 1L -> {
                        currentStreak += 1
                        maxStreak = maxOf(maxStreak, currentStreak)
                        lastActiveDateStr = todayStr
                        if (!activeDates.contains(todayStr)) activeDates.add(todayStr)
                    }
                    else -> {
                        currentStreak = 1
                        maxStreak = maxOf(maxStreak, currentStreak)
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

        saveToLocalPrefs()
        onStreakUpdated?.invoke(currentStreak)

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

    fun registrarLeccionCompletada(userId: String?, onStreakUpdated: ((Int) -> Unit)? = null) =
        recordLessonCompleted(userId, onStreakUpdated)

    fun isDateActive(date: LocalDate): Boolean {
        return activeDates.contains(date.toString())
    }

    fun esFechaActiva(date: LocalDate) = isDateActive(date)
}
