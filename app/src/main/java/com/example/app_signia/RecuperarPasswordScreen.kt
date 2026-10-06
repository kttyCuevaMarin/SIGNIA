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
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * PANTALLA DE RECUPERACIÓN DE CONTRASEÑA (SIGNIA)
 * Permite al usuario ingresar su correo registrado para recibir un enlace oficial de
 * restablecimiento de contraseña enviado directamente por Firebase Auth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(navController: NavController) {
    val auth = remember { Firebase.auth }
    val context = LocalContext.current

    var correoUsuario by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    fun enviarCorreoRecuperacion() {
        if (correoUsuario.isBlank()) {
            Toast.makeText(context, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }

        val correoLimpio = correoUsuario.trim().lowercase()
        cargando = true

        auth.sendPasswordResetEmail(correoLimpio)
            .addOnSuccessListener {
                cargando = false
                Toast.makeText(
                    context,
                    "¡Correo enviado! Revisa tu bandeja de entrada o Spam para restablecer tu contraseña.",
                    Toast.LENGTH_LONG
                ).show()

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
                title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold, color = SigniaDarkPurple) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SigniaDarkPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SigniaLightLavender,
                    titleContentColor = SigniaDarkPurple
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
                color = SigniaDarkPurple
            )

            Text(
                text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace oficial para restablecer tu contraseña de forma segura.",
                fontSize = 14.sp,
                color = SigniaDarkPurple.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = correoUsuario,
                onValueChange = { correoUsuario = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SigniaDarkPurple) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = SigniaBluePrimary,
                    unfocusedBorderColor = SigniaSoftLilac,
                    focusedTextColor = SigniaDarkPurple,
                    unfocusedTextColor = SigniaDarkPurple,
                    cursorColor = SigniaBluePrimary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { enviarCorreoRecuperacion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaDarkPurple),
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