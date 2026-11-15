package com.example.app_signia

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_signia.ui.theme.APPSIGNIATheme
import com.example.app_signia.ui.theme.ThemeManager
import com.example.app_signia.ui.theme.SigniaBluePrimary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar persistencia local y base de datos de Cloud Firestore (RF08)
        StreakManager.initFromContext(this)
        ProgressManager.initFromContext(this)
        SignDictionary.loadSignsFromFirestore()

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val view = LocalView.current
            val currentBgColor = ThemeManager.backgroundColor

            // ELIMINAR FLASH EN NAVEGACIÓN (Aplica el color de fondo del tema a la ventana nativa)
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = currentBgColor.toArgb()
                    window.navigationBarColor = currentBgColor.toArgb()
                    window.decorView.setBackgroundColor(currentBgColor.toArgb())
                }
            }

            val auth = Firebase.auth
            var isCheckingAuth by remember { mutableStateOf(true) }
            var currentUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(150)
                currentUser = auth.currentUser
                isCheckingAuth = false
            }

            APPSIGNIATheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentBgColor)
                ) {
                    if (isCheckingAuth) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SigniaBluePrimary)
                        }
                    } else {
                        val navController = rememberNavController()
                        val initialRoute = if (currentUser != null) "home" else "login"

                        NavHost(
                            navController = navController,
                            startDestination = initialRoute
                        ) {
                        composable("login") {
                            Login(navController)
                        }
                        composable("register") {
                            RegisterView(navController)
                        }
                        composable("forgot_password") {
                            RecuperarPasswordScreen(navController = navController)
                        }
                        composable("home") {
                            Home(navController = navController)
                        }
                        composable("aprende_lsp") {
                            AprendeLspScreen(navController = navController)
                        }

                        // ==========================================
                        // RUTAS: RF08 y RF09 (Categorías y Detalle)
                        // ==========================================
                        composable(
                            route = "category_signs/{categoryId}",
                            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                            CategorySignsScreen(navController = navController, categoryId = categoryId)
                        }

                        composable(
                            route = "sign_detail/{wordKey}",
                            arguments = listOf(navArgument("wordKey") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val wordKey = backStackEntry.arguments?.getString("wordKey") ?: ""
                            SignDetailScreen(navController = navController, wordKey = wordKey)
                        }
                        // ==========================================

                        // ==========================================
                        // RUTA: RF16 (Práctica con Cámara e IA)
                        // ==========================================
                        composable(
                            route = "aprende_camera_practice/{word}",
                            arguments = listOf(navArgument("word") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val word = backStackEntry.arguments?.getString("word") ?: ""
                                AprendeCameraPracticeScreen(navController = navController, targetWord = word)
                        }
                        // ==========================================

                        composable("perfil") {
                            PerfilScreen(navController = navController)
                        }
                        composable("editar_perfil") {
                            EditarPerfilScreen(navController = navController)
                        }
                        composable("camera_recognition") {
                            CameraRecognitionScreen(navController = navController)
                        }
                        composable("racha") {
                            RachaScreen(navController = navController)
                        }
                        composable("translator") {
                            TranslatorScreen(navController = navController)
                        }
                        composable("translation_mode_selector") {
                            TranslationModeSelectorScreen(navController = navController)
                        }
                        composable("sign_scan_details") {
                            SignScanDetailsScreen(navController = navController)
                        }
                        composable(
                            route = "animation_player/{message}",
                            arguments = listOf(navArgument("message") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val messageParam = backStackEntry.arguments?.getString("message") ?: "Hola"
                            SignAnimationScreen(navController = navController, message = messageParam)
                        }
                    }
                  }
                }
            }
        }
    }
}