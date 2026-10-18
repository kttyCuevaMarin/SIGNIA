package com.example.app_signia

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

/**
 * RF10 - REGISTRO Y VISUALIZACIÓN DEL PROGRESO REAL
 * Persiste las lecciones y señas completadas por el usuario localmente y en Firestore.
 */
object ProgressManager {
    var completedSignsCount by mutableIntStateOf(0)
    var totalXp by mutableIntStateOf(0)
    val completedSignsList = mutableStateListOf<String>()

    private var prefs: SharedPreferences? = null

    fun initFromContext(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("signia_progress_prefs", Context.MODE_PRIVATE)
            completedSignsCount = prefs?.getInt("completedSignsCount", 0) ?: 0
            totalXp = prefs?.getInt("totalXp", 0) ?: 0

            val listSet = prefs?.getStringSet("completedSignsSet", emptySet()) ?: emptySet()
            completedSignsList.clear()
            completedSignsList.addAll(listSet)
        }
    }

    private fun saveToLocalPrefs() {
        prefs?.edit()?.apply {
            putInt("completedSignsCount", completedSignsCount)
            putInt("totalXp", totalXp)
            putStringSet("completedSignsSet", completedSignsList.toSet())
            apply()
        }
    }

    fun loadProgressForUser(userId: String?) {
        if (userId.isNullOrBlank()) return

        val db = Firebase.firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val count = document.getLong("completedSignsCount")?.toInt() ?: 0
                    val xp = document.getLong("totalXp")?.toInt() ?: 0
                    val savedList = document.get("completedSignsList") as? List<*>

                    completedSignsCount = count
                    totalXp = xp

                    if (savedList != null) {
                        completedSignsList.clear()
                        savedList.filterIsInstance<String>().forEach { sign ->
                            if (!completedSignsList.contains(sign)) {
                                completedSignsList.add(sign)
                            }
                        }
                    }
                    saveToLocalPrefs()
                }
            }
    }

    fun markSignCompleted(userId: String?, signWord: String) {
        val normalized = signWord.lowercase()

        if (!completedSignsList.contains(normalized)) {
            completedSignsList.add(normalized)
            completedSignsCount = completedSignsList.size
            totalXp += 20
            saveToLocalPrefs()
        }

        if (!userId.isNullOrBlank()) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "completedSignsCount" to completedSignsCount,
                "totalXp" to totalXp,
                "completedSignsList" to completedSignsList.toList()
            )
            db.collection("users").document(userId)
                .set(data, SetOptions.merge())
        }
    }

    fun resetProgressToZero(userId: String?) {
        completedSignsCount = 0
        totalXp = 0
        completedSignsList.clear()
        saveToLocalPrefs()

        if (!userId.isNullOrBlank()) {
            val db = Firebase.firestore
            val data = hashMapOf(
                "completedSignsCount" to 0,
                "totalXp" to 0,
                "completedSignsList" to emptyList<String>()
            )
            db.collection("users").document(userId)
                .set(data, SetOptions.merge())
        }
    }
}
