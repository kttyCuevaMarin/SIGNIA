package com.example.app_signia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_signia.ui.theme.APPSIGNIATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            APPSIGNIATheme {
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
                    composable("practice") {
                        PracticeScreen(navController = navController, category = "saludos")
                    }


                    composable(
                        route = "practice/{category}",
                        arguments = listOf(navArgument("category") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val categoryParam = backStackEntry.arguments?.getString("category") ?: "saludos"
                        PracticeScreen(navController = navController, category = categoryParam)
                    }
                    composable("perfil") {
                        PerfilScreen(navController = navController)
                    }
                    composable("racha") {
                        RachaScreen(navController = navController)
                    }
                }
            }
        }
    }
}
