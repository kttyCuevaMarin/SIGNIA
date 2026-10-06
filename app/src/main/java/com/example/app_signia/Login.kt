package com.example.app_signia

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth

val SigniaDarkPurple = Color(0xFF81638B)   // #81638b
val SigniaSoftLilac = Color(0xFFB695C0)    // #b695c0
val SigniaLightLavender = Color(0xFFDAC9DF) // #dac9df
val SigniaBluePrimary = Color(0xFF2196F3)  // #2196f3

val SigniaGreen = SigniaBluePrimary
val SigniaBackground = SigniaLightLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController) {
    // 1. Instancias base de Firebase Auth y contexto
    val auth = remember { Firebase.auth }
    val context = LocalContext.current

    // 2. Estados locales del formulario de Login
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // =========================================================================
    // 1. AUTENTICACIÓN CON GOOGLE SIGN-IN
    // =========================================================================

    // ID de Cliente Web Oficial de tu proyecto en Firebase
    val webClientId = "595956443369-4v6h2e2ltlt96p3vir3uuvqj0ht3u6f8.apps.googleusercontent.com"

    val gso = remember(context) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember(context, gso) {
        GoogleSignIn.getClient(context, gso)
    }

    // Launcher para capturar la respuesta del selector de cuentas de Google
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (!idToken.isNullOrEmpty()) {
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "¡Sesión iniciada con Google!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { error ->
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Error al autenticar en Firebase: ${error.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "No se obtuvo el token de Google", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                val mensajeError = when (e.statusCode) {
                    10 -> "Error 10 (DEVELOPER_ERROR): Falta registrar la huella SHA-1 de tu app en Firebase Console."
                    12500 -> "Error 12500: Verifica que Google Play Services esté actualizado en tu emulador/celular."
                    else -> "Error en Google Sign-In (${e.statusCode}): ${e.localizedMessage}"
                }
                Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
            }
        } else {
            // El usuario canceló o Google rechazó la petición (común cuando falta el SHA-1 en Firebase)
            Toast.makeText(
                context,
                "Inicio de sesión cancelado. Si el selector se cierra de inmediato, debes registrar la huella SHA-1 en Firebase Console.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================================================
    // VISTA PRINCIPAL (SCAFFOLD)
    // =========================================================================
    Scaffold(
        containerColor = SigniaBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_signia),
                contentDescription = "Logo de seña",
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SIGNIA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                letterSpacing = 1.sp
            )

            Text(
                text = "Conectando comunidades a través de\nla lengua de señas.",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo de Correo Electrónico
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Correo electrónico",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("ejemplo@correo.com", color = Color.Black.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Campo de Contraseña y Enlace "¿Olvidé mi contraseña?"
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contraseña",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    TextButton(
                        onClick = { navController.navigate("forgot_password") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "¿Olvidé mi contraseña?",
                            color = SigniaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text("Ingresa tu contraseña", color = Color.Black.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Entrar
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener {
                            isLoading = false
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { error ->
                            isLoading = false
                            Toast.makeText(context, "Error de acceso: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Entrar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Separador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
                Text(
                    text = " O continúa con ",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Google
            OutlinedButton(
                onClick = {
                    isLoading = true
                    val signInIntent = googleSignInClient.signInIntent
                    googleLauncher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "G ",
                        color = SigniaGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Google",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            // Pie de página
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(text = "¿Nuevo en SIGNIA? ", fontSize = 14.sp, color = Color.Gray)
                TextButton(
                    onClick = { navController.navigate("register") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "Regístrate", color = SigniaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}


