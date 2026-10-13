package com.example.app_signia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

/**
 * RF10 - REGISTRO Y VISUALIZACIÓN DEL PROGRESO REAL
 * Persiste las lecciones y señas completadas por el usuario en Firestore.
 */
object ProgressManager {
    var completedSignsCount by mutableIntStateOf(0)
    var totalXp by mutableIntStateOf(0)
    val completedSignsList = mutableStateListOf<String>()

    /**
     * Carga el progreso guardado del usuario desde Firestore
     */
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
                }
            }
    }

    /**
     * Marca una seña como completada y actualiza Firestore en la nube
     */
    fun markSignCompleted(userId: String?, signWord: String) {
        val normalized = signWord.lowercase()

        if (!completedSignsList.contains(normalized)) {
            completedSignsList.add(normalized)
            completedSignsCount = completedSignsList.size
            totalXp += 20 // 20 XP por seña aprendida
        }

        // Siempre sincronizar con Firestore si existe sesión de usuario
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
}