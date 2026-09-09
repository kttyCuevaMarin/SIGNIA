package com.example.app_signia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("login") }

            when (currentScreen) {
                "login" -> Login(
                    onLoginSuccess = { currentScreen = "dashboard" },
                    onGoToRegister = { currentScreen = "register" }
                )
                "register" -> RegisterView(
                    onRegisterSuccess = { currentScreen = "dashboard" },
                    onBackToLogin = { currentScreen = "login" }
                )
                "dashboard" -> MainDashboard(
                    onLogout = { currentScreen = "login" }
                )
            }
        }
    }
}

@Composable
fun MainDashboard(onLogout: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¡Bienvenido al Menú Principal de SIGNIA!", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onLogout() }) {
                Text("Cerrar Sesión")
            }
        }
    }
}