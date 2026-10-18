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

            APPSIGNIATheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentBgColor)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
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

                        composable("perfil") {
                            PerfilScreen(navController = navController)
                        }
                        composable("racha") {
                            RachaScreen(navController = navController)
                        }
                        composable("translator") {
                            TranslatorScreen(navController = navController)
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
