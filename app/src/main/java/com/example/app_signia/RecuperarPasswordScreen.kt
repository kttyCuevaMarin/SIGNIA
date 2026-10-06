package com.example.app_signia

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * PANTALLA DE RECUPERACIÓN DE CONTRASEÑA (SIGNIA)
 * 
 * Permite al usuario ingresar su correo registrado para recibir un enlace oficial de
 * restablecimiento de contraseña enviado directamente por Firebase Auth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(navController: NavController) {
    val auth = remember { Firebase.auth }
    val context = LocalContext.current

    // Variables de estado
    var correoUsuario by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // Función para enviar el correo de recuperación
    fun enviarCorreoRecuperacion() {
        if (correoUsuario.isBlank()) {
            Toast.makeText(context, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }

        val correoLimpio = correoUsuario.trim().lowercase()
        cargando = true

        // Enviar el correo oficial de recuperación mediante Firebase Auth
        auth.sendPasswordResetEmail(correoLimpio)
            .addOnSuccessListener {
                cargando = false
                Toast.makeText(
                    context,
                    "¡Correo enviado! Revisa tu bandeja de entrada o Spam para restablecer tu contraseña.",
                    Toast.LENGTH_LONG
                ).show()

                // Regresar a la pantalla de Inicio de Sesión
                navController.navigate("login") {
                    popUpTo("forgot_password") { inclusive = true }
                }
            }
            .addOnFailureListener { error ->
                cargando = false
                val mensaje = if (error.message?.contains("no user record", ignoreCase = true) == true) {
                    "Este correo no está registrado en SIGNIA."
                } else {
                    "Error al enviar: ${error.localizedMessage}"
                }
                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SigniaLightLavender,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = SigniaLightLavender
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Logo corporativo de SIGNIA
            Image(
                painter = painterResource(id = R.drawable.logo_signia),
                contentDescription = "Logo SIGNIA",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace oficial para restablecer tu contraseña de forma segura.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 32.dp)
            )

            // Campo de texto para ingresar el correo
            OutlinedTextField(
                value = correoUsuario,
                onValueChange = { correoUsuario = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SigniaBluePrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = SigniaBluePrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para enviar el correo
            Button(
                onClick = { enviarCorreoRecuperacion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Enviar Correo de Recuperación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
